package de.provereval;

import org.eventb.core.IPOSequent;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.seqprover.ITacticDescriptor;

import de.provereval.labelproviders.ReasonersLabelProvider;
import de.provereval.labelproviders.SequentsLabelProvider;

public class ProverEvaluationResult {
	final private static SequentsLabelProvider sProvider = new SequentsLabelProvider();
	final private static ReasonersLabelProvider rProvider = new ReasonersLabelProvider();

	private final ProverEvaluationTaskStatus status;
	private final long took;
	private final IPOSequent sequent;
	private final IPrefMapEntry<ITacticDescriptor> prover;

	public ProverEvaluationResult(IPrefMapEntry<ITacticDescriptor> tactic, IPOSequent sequent, long took,
			ProverEvaluationTaskStatus status) {
		this.prover = tactic;
		this.sequent = sequent;
		this.took = took;
		this.status = status;
	}

	public boolean isProven() {
		return status == ProverEvaluationTaskStatus.PROVEN;
	}

	public boolean isDisproven() {
		return status == ProverEvaluationTaskStatus.DISPROVEN;
	}

	public long getTook() {
		return took;
	}

	public String getProofObligationName() {
		return sProvider.getText(sequent);
	}

	public String getProverName() {
		return rProvider.getText(prover);
	}
}
