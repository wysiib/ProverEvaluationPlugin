package de.provereval;

import java.util.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eventb.core.*;
import org.eventb.core.seqprover.ITactic;
import org.rodinp.core.*;

public class EvalCommand extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			List<ITactic> allReasoners = getAllReasoners();
			List<IPOSequent> allProverSequents = getAllProverSequents();

			List<ProverEvaluationTask> tasks = generateTasks(allProverSequents,
					allReasoners);

			evaluate(tasks);

		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void evaluate(List<ProverEvaluationTask> tasks) {
		for (ProverEvaluationTask task : tasks) {
			task.runTask();
		}
	}

	private List<ITactic> getAllReasoners() {
		List<ITactic> reasoners = new ArrayList<ITactic>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint("org.eventb.core.seqprover.autoTactics");
		for (IExtension extension : extensionPoint.getExtensions()) {
			for (IConfigurationElement configurationElement : extension
					.getConfigurationElements()) {

				if ("autoTactic".equals(configurationElement.getName())) {
					try {
						if (configurationElement.getAttribute("class")
								.startsWith("org.eventb.core.seqprover")
								|| configurationElement.getAttribute("class")
										.startsWith("org.eventb.core.tests")) {
							break;
						}
						Object x = configurationElement
								.createExecutableExtension("class");

						if (x instanceof ITactic) {
							reasoners.add((ITactic) x);
						}

					} catch (final CoreException e) {
						// this happens for some incomplete reasoners that can
						// not be instantiated
					}

				}

			}

		}
		return reasoners;
	}

	private List<ProverEvaluationTask> generateTasks(
			List<IPOSequent> allProverSequents, List<ITactic> allReasoners)
			throws RodinDBException {
		List<ProverEvaluationTask> tasks = new ArrayList<ProverEvaluationTask>();

		for (IPOSequent sequent : allProverSequents) {
			for (ITactic reasoner : allReasoners) {
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
