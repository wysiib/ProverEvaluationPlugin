package de.provereval;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.Callable;

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

import de.provereval.ProverEvaluationResult.TaskStatus;
import de.provereval.labelproviders.ReasonersLabelProvider;
import de.provereval.labelproviders.SequentsLabelProvider;

@SuppressWarnings("restriction")
public class ProverEvaluationTask implements Callable<ProverEvaluationResult> {
	final private static SequentsLabelProvider sProvider = new SequentsLabelProvider();
	final private static ReasonersLabelProvider rProvider = new ReasonersLabelProvider();
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

	@Override
	public ProverEvaluationResult call() {
		long took = 0;
		try {
			ProofTreeNode node = new ProofTree(toProverSequent(sequent), null)
					.getRoot();

			ITacticDescriptor descriptor = tactic.getValue();
			ITactic instance = descriptor.getTacticInstance();

			long start = System.currentTimeMillis();
			instance.apply(node, Util.getNullProofMonitor());
			took = System.currentTimeMillis() - start;

			Stack<ProofTreeNode> childNodes = new Stack<ProofTreeNode>();
			childNodes.add(node);

			while (!childNodes.isEmpty()) {
				// add all sub-nodes to stack
				ProofTreeNode cur = childNodes.pop();

				if (cur.hasChildren()) {
					childNodes.addAll(Arrays.asList(cur.getChildNodes()));
				} else {
					if (cur.getConfidence() < IConfidence.DISCHARGED_MAX) {
						if (cur.getRule() != null
								&& cur.getRule().getDisplayName()
										.startsWith("Counter-Example found")) {
							return new ProverEvaluationResult(
									rProvider.getText(tactic),
									sProvider.getText(sequent), took,
									TaskStatus.DISPROVEN);
						} else {
							return new ProverEvaluationResult(
									rProvider.getText(tactic),
									sProvider.getText(sequent), took,
									TaskStatus.NOT_PROVEN);
						}
					}
				}
			}
		} catch (IllegalStateException e) {
			return new ProverEvaluationResult(rProvider.getText(tactic),
					sProvider.getText(sequent), took, TaskStatus.CRASHED);
		} catch (RodinDBException e) {
			return new ProverEvaluationResult(rProvider.getText(tactic),
					sProvider.getText(sequent), took, TaskStatus.CRASHED);
		} catch (NullPointerException e) {
			// these are caused by an error in the provers, so this is a bug!
			// maybe the result should be reported differently
			return new ProverEvaluationResult(rProvider.getText(tactic),
					sProvider.getText(sequent), took, TaskStatus.CRASHED);
		}

		return new ProverEvaluationResult(rProvider.getText(tactic),
				sProvider.getText(sequent), took, TaskStatus.PROVEN);
	}
}
