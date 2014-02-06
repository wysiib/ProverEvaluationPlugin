package de.provereval;

import java.util.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eventb.core.*;
import org.eventb.core.pm.*;
import org.eventb.core.seqprover.*;
import org.eventb.internal.core.seqprover.*;
import org.rodinp.core.*;

public class EvalCommand extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			List<IPOSequent> allProverSequents = getAllProverSequents();
			List<ITactic> allReasoners = getAllReasoners();
			evaluateOnProvers(allProverSequents, allReasoners);
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

	@SuppressWarnings("restriction")
	private void evaluateOnProvers(List<IPOSequent> allProverSequents,
			List<ITactic> allReasoners) throws RodinDBException {

		for (IPOSequent sequent : allProverSequents) {
			ProofTreeNode node = new ProofTree(toProverSequent(sequent), null)
					.getRoot();
			for (ITactic reasoner : allReasoners) {

				try {
					reasoner.apply(node, Util.getNullProofMonitor());

					if (node.isClosed()) {
						System.out.println(sequent.getElementName()
								+ " is open after " + reasoner.toString());
					} else {
						System.out.println(sequent.getElementName()
								+ " is closed after " + reasoner.toString());
					}
				} catch (Exception e) {
					// prover crashed somehow
				}
			}
		}
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

	public static IProverSequent toProverSequent(IPOSequent sequent)
			throws RodinDBException {
		IPORoot poRoot = (IPORoot) sequent.getRoot();
		IProofManager pm = EventBPlugin.getProofManager();
		IProofComponent pc = pm.getProofComponent(poRoot);
		IProofAttempt pa = pc.createProofAttempt(sequent.getElementName(),
				"Translation in Prover Evaluation Plugin", null);
		IProofTree proofTree = pa.getProofTree();

		IProverSequent proverSequent = proofTree.getSequent();
		pa.dispose();

		return proverSequent;
	}
}
