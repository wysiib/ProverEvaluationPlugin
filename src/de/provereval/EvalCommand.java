package de.provereval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSequent;
import org.eventb.core.pm.IProofAttempt;
import org.eventb.core.pm.IProofComponent;
import org.eventb.core.pm.IProofManager;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProverSequent;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class EvalCommand extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			evaluateSolvers();
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void evaluateSolvers() throws RodinDBException {
		// 1. Find all Projects
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IRodinDB rodinDB = RodinCore.valueOf(workspace.getRoot());

		IRodinProject[] rodinProjects = rodinDB.getRodinProjects();

		// 2. Find all Machines / Contexts and fetch their PORoots
		List<IPORoot> poRoots = new ArrayList<IPORoot>();

		for (IRodinProject p : rodinProjects) {
			for (IMachineRoot m : p
					.getChildrenOfType(IMachineRoot.ELEMENT_TYPE)) {
				poRoots.add(m.getPORoot());

			}
			for (IContextRoot c : p
					.getChildrenOfType(IContextRoot.ELEMENT_TYPE)) {
				poRoots.add(c.getPORoot());
			}
		}

		// 3. Collect all Sequents
		List<IPOSequent> sequents = new ArrayList<IPOSequent>();
		for (IPORoot r : poRoots) {
			sequents.addAll(Arrays.asList(r.getSequents()));
		}

		// 4. However, we need IProoverSequents
		List<IProverSequent> proverSequents = new ArrayList<IProverSequent>();
		for (IPOSequent s : sequents) {
			proverSequents.add(toProverSequent(s));
		}
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
