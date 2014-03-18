package de.provereval;

import java.util.Arrays;
import java.util.Stack;

import org.eventb.core.EventBPlugin;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSequent;
import org.eventb.core.pm.IProofAttempt;
import org.eventb.core.pm.IProofComponent;
import org.eventb.core.pm.IProofManager;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.ITacticDescriptor;
import org.eventb.internal.core.seqprover.ProofTree;
import org.eventb.internal.core.seqprover.ProofTreeNode;
import org.eventb.internal.core.seqprover.Util;
import org.rodinp.core.RodinDBException;

import de.provereval.labelproviders.ReasonersLabelProvider;
import de.provereval.labelproviders.SequentsLabelProvider;

public class ProverEvaluationTask {
	final private static SequentsLabelProvider sProvider = new SequentsLabelProvider();
	final private static ReasonersLabelProvider rProvider = new ReasonersLabelProvider();

	public enum TaskStatus {
		PROVEN, NOT_PROVEN, CRASHED
	};

	private TaskStatus status;
	private long took;
	private final IPrefMapEntry<ITacticDescriptor> tactic;
	private final IPOSequent sequent;

	public ProverEvaluationTask(IPrefMapEntry<ITacticDescriptor> reasoner,
			IPOSequent sequent) {
		super();
		this.tactic = reasoner;
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

	public long getTook() {
		return took;
	}

	@SuppressWarnings("restriction")
	public void runTask() {
		try {
			ProofTreeNode node = new ProofTree(toProverSequent(sequent), null)
					.getRoot();

			ITacticDescriptor descriptor = tactic.getValue();
			ITactic instance = descriptor.getTacticInstance();

			long start = System.currentTimeMillis();
			instance.apply(node, Util.getNullProofMonitor());
			took = System.currentTimeMillis() - start;

			// check for all child nodes if they are discharged
			status = TaskStatus.PROVEN;

			Stack<ProofTreeNode> childNodes = new Stack<ProofTreeNode>();
			childNodes.add(node);

			while (!childNodes.isEmpty()) {
				// add all sub-nodes to stack
				ProofTreeNode cur = childNodes.pop();
				childNodes.addAll(Arrays.asList(cur.getChildNodes()));

				if (cur.getConfidence() < IConfidence.DISCHARGED_MAX) {
					status = TaskStatus.NOT_PROVEN;
					break;
				}
			}
		} catch (Exception e) {
			// prover crashed somehow
			status = TaskStatus.CRASHED;
		}
	}

	public String getProverName() {
		return rProvider.getText(tactic);
	}

	public String getProofObligationName() {
		return sProvider.getText(sequent);
	}
}
