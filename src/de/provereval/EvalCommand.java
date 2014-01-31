package de.provereval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSequent;
import org.eventb.core.pm.IProofAttempt;
import org.eventb.core.pm.IProofComponent;
import org.eventb.core.pm.IProofManager;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ITactic;
import org.eventb.internal.core.seqprover.ProofTree;
import org.eventb.internal.core.seqprover.ProofTreeNode;
import org.eventb.internal.core.seqprover.Util;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class EvalCommand extends AbstractHandler {

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
				reasoner.apply(node, Util.getNullProofMonitor());
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
