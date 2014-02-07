package de.provereval;

import org.eventb.core.*;
import org.eventb.core.pm.*;
import org.eventb.core.seqprover.*;
import org.eventb.internal.core.seqprover.*;
import org.rodinp.core.RodinDBException;

public class ProverEvaluationTask {
	public enum TaskStatus {
		PROVEN, NOT_PROVEN, CRASHED
	};

	private TaskStatus status;
	private final ITactic tactic;
	private final IPOSequent sequent;

	public ProverEvaluationTask(ITactic tactic, IPOSequent sequent) {
		super();
		this.tactic = tactic;
		this.sequent = sequent;
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

	public boolean isProven() {
		return status == TaskStatus.PROVEN;
	}

	@SuppressWarnings("restriction")
	public void runTask() {
		try {
			ProofTreeNode node = new ProofTree(toProverSequent(sequent), null)
					.getRoot();

			tactic.apply(node, Util.getNullProofMonitor());

			if (node.getConfidence() > IConfidence.REVIEWED_MAX) {
				status = TaskStatus.PROVEN;
			} else {
				status = TaskStatus.NOT_PROVEN;
			}
		} catch (Exception e) {
			// prover crashed somehow
			status = TaskStatus.CRASHED;
		}
	}

	public String getProverName() {
		String tacticName = tactic.toString();

		int index = tacticName.lastIndexOf(".");
		tacticName = tacticName.substring(index + 1, tacticName.length());

		index = tacticName.lastIndexOf("@");
		tacticName = tacticName.substring(0, index);

		return tacticName;
	}

	public String getProofObligationName() {
		return sequent.getElementName();
	}
}
