package de.provereval;

import java.util.*;

import org.eventb.core.*;
import org.eventb.core.pm.*;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.seqprover.IAutoTacticRegistry.ITacticDescriptor;
import org.eventb.core.seqprover.*;
import org.eventb.internal.core.seqprover.*;
import org.rodinp.core.RodinDBException;

import de.provereval.labelproviders.*;

public class ProverEvaluationTask {
	final private static SequentsLabelProvider sProvider = new SequentsLabelProvider();
	final private static ReasonersLabelProvider rProvider = new ReasonersLabelProvider();

	public enum TaskStatus {
		PROVEN, NOT_PROVEN, CRASHED
	};

	private TaskStatus status;
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

	@SuppressWarnings("restriction")
	public void runTask() {
		try {
			ProofTreeNode node = new ProofTree(toProverSequent(sequent), null)
					.getRoot();

			ITacticDescriptor descriptor = tactic.getValue();
			ITactic instance = descriptor.getTacticInstance();

			instance.apply(node, Util.getNullProofMonitor());

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
