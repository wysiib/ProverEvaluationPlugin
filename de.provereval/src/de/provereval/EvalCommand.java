package de.provereval;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eventb.core.*;
import org.eventb.core.preferences.*;
import org.eventb.core.preferences.autotactics.TacticPreferenceFactory;
import org.eventb.core.seqprover.IAutoTacticRegistry.ITacticDescriptor;
import org.rodinp.core.*;

import de.provereval.labelproviders.ReasonersLabelProvider;
import de.provereval.output.CSVExporter;
import dialogs.*;

public class EvalCommand extends AbstractHandler {
	Shell shell;
	private boolean headless;

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

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
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
			List<IPOSequent> allProverSequents = getAllProverSequents();
			if (!headless) {
				SequentSelectionDialog ssDiag = new SequentSelectionDialog(
						shell, allProverSequents);
				ssDiag.open();
				allProverSequents.clear();
				allProverSequents.addAll(ssDiag.getSelectedProverSequents());
			}

			// combine selected reasoners / sequents to a list of tasks
			List<ProverEvaluationTask> tasks = generateTasks(allProverSequents,
					allReasoners);

			boolean canceled = evaluate(tasks);

			if (!canceled) {

				Map<String, List<ProverEvaluationTask>> grouped = groupTasksBySequent(tasks);

				if (headless) {
					String[] applicationArgs = Platform.getApplicationArgs();
					CSVExporter.exportToCSVFile(grouped, applicationArgs[0]);
				} else {
					new ResultDialog(shell, grouped).open();
				}
			}
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, List<ProverEvaluationTask>> groupTasksBySequent(
			List<ProverEvaluationTask> tasks) {
		Map<String, List<ProverEvaluationTask>> grouped = new HashMap<String, List<ProverEvaluationTask>>();

		for (ProverEvaluationTask task : tasks) {
			String sequentName = task.getProofObligationName();
			if (!grouped.containsKey(sequentName)) {
				grouped.put(sequentName, new ArrayList<ProverEvaluationTask>());
			}
			grouped.get(sequentName).add(task);
		}

		return grouped;
	}

	private boolean evaluate(final List<ProverEvaluationTask> tasks) {
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
		return runnable.isCanceled();
	}

	private List<IPrefMapEntry<ITacticDescriptor>> getAllTactics() {
		IPreferencesService ps = Platform.getPreferencesService();
		String string = ps.getString("org.eventb.ui", "Tactics Map",
				"nix nada nothing", null);
		System.out.println(string);

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
				tasks.add(new ProverEvaluationTask(reasoner, sequent));
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

}
