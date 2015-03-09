package de.provereval;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSequent;
import org.eventb.core.preferences.CachedPreferenceMap;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.preferences.autotactics.TacticPreferenceFactory;
import org.eventb.core.seqprover.ITacticDescriptor;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import de.provereval.dialogs.ResultDialog;
import de.provereval.dialogs.SequentSelectionDialog;
import de.provereval.labelproviders.ReasonersLabelProvider;
import de.provereval.output.CSVExporter;

public class EvalCommand extends AbstractHandler {
	Shell shell;
	private boolean headless;
	private final Map<String, Lock> locks = new HashMap<String, Lock>();

	public EvalCommand() {
		this(false);
	}

	public EvalCommand(boolean skipDialogs) {
		super();
		if (skipDialogs) {
			shell = null;
			headless = true;
		} else {
			headless = false;
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// get all reasoners and ask the user which ones to benchmark
		List<IPrefMapEntry<ITacticDescriptor>> allReasoners = getAllTactics();
		if (!headless) {
			ListSelectionDialog dlg = new ListSelectionDialog(shell,
					allReasoners, new ArrayContentProvider(),
					new ReasonersLabelProvider(),
					"Select the reasoners you want to apply:");
			dlg.setTitle("Select Reasoners");
			dlg.setInitialSelections(allReasoners.toArray());
			dlg.open();

			allReasoners.clear();
			for (Object o : dlg.getResult()) {
				allReasoners.add((IPrefMapEntry<ITacticDescriptor>) o);
			}
		}

		// same for sequents
		List<IPOSequent> allProverSequents;
		try {
			allProverSequents = getAllProverSequents();
		} catch (RodinDBException rdb) {
			MessageDialog
					.openError(
							shell,
							"A RodinDBException Occured",
							"A RodinDBException occured while fetching available tactics. Benchmarks have been aborted.");
			return null;
		}
		if (!headless) {
			SequentSelectionDialog ssDiag = new SequentSelectionDialog(shell,
					allProverSequents);
			ssDiag.open();
			allProverSequents.clear();
			allProverSequents.addAll(ssDiag.getSelectedProverSequents());
		}

		List<ProverEvaluationTask> tasks;
		try {
			// combine selected reasoners / sequents to a list of tasks
			tasks = generateTasks(allProverSequents, allReasoners);
		} catch (RodinDBException rdb) {
			MessageDialog
					.openError(
							shell,
							"A RodinDBException Occured",
							"A RodinDBException occured while fetching sequents. Benchmarks have been aborted.");
			return null;
		}

		SolverRunnable runnable = evaluate(tasks);

		if (!runnable.isCanceled()) {

			Map<String, List<ProverEvaluationResult>> grouped = groupTasksBySequent(runnable
					.getResults());

			if (headless) {
				String[] applicationArgs = Platform.getApplicationArgs();
				CSVExporter.exportToCSVFile(grouped, applicationArgs[0]);
			} else {
				new ResultDialog(shell, grouped).open();
			}
		}

		return null;
	}

	private Map<String, List<ProverEvaluationResult>> groupTasksBySequent(
			List<ProverEvaluationResult> results) {
		Map<String, List<ProverEvaluationResult>> grouped = new HashMap<String, List<ProverEvaluationResult>>();

		for (ProverEvaluationResult result : results) {
			String sequentName = result.getProofObligationName();
			if (!grouped.containsKey(sequentName)) {
				grouped.put(sequentName,
						new ArrayList<ProverEvaluationResult>());
			}
			grouped.get(sequentName).add(result);
		}

		return grouped;
	}

	private SolverRunnable evaluate(final List<ProverEvaluationTask> tasks) {
		SolverRunnable runnable = new SolverRunnable(tasks);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return runnable;
	}

	private List<IPrefMapEntry<ITacticDescriptor>> getAllTactics() {
		IPreferencesService ps = Platform.getPreferencesService();
		String string = ps.getString("org.eventb.core", "Tactics Map",
				"nix nada nothing", null);
		// System.out.println(string);

		CachedPreferenceMap<ITacticDescriptor> preferenceMap = TacticPreferenceFactory
				.makeTacticPreferenceMap();
		preferenceMap.inject(string);

		return preferenceMap.getEntries();
	}

	private List<ProverEvaluationTask> generateTasks(
			List<IPOSequent> allProverSequents,
			List<IPrefMapEntry<ITacticDescriptor>> allReasoners)
			throws RodinDBException {
		List<ProverEvaluationTask> tasks = new ArrayList<ProverEvaluationTask>();

		for (IPOSequent sequent : allProverSequents) {
			for (IPrefMapEntry<ITacticDescriptor> reasoner : allReasoners) {
				tasks.add(new ProverEvaluationTask(this, reasoner, sequent));
			}
		}

		return tasks;
	}

	private List<IPOSequent> getAllProverSequents() throws RodinDBException {
		// 1. Find all Projects
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IRodinDB rodinDB = RodinCore.valueOf(workspace.getRoot());

		IRodinProject[] rodinProjects = rodinDB.getRodinProjects();

		// 2. Find all Machines / Contexts and fetch their PORoots
		// 3. Collect all Sequents
		List<IPOSequent> sequents = new ArrayList<IPOSequent>();
		for (IRodinProject p : rodinProjects) {
			for (IRodinFile m : p.getChildrenOfType(IRodinFile.ELEMENT_TYPE)) {
				IRodinElement e = m.getRoot();
				if (e instanceof IPORoot) {
					sequents.addAll(Arrays.asList(((IPORoot) e).getSequents()));
				}
			}

		}

		return sequents;
	}

	public Lock getLockForTactic(String tacticName) {
		if (locks.containsKey(tacticName)) {
			return locks.get(tacticName);
		} else {
			Lock l = new ReentrantLock();
			locks.put(tacticName, l);
			return l;
		}
	}

}
