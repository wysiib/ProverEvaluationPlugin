package de.provereval;

public class ProverEvaluationResult {
	private final ProverEvaluationTaskStatus status;
	private final long took;
	private final String sequentName;
	private final String proverName;

	public ProverEvaluationResult(String proverName, String sequentName,
			long took, ProverEvaluationTaskStatus status) {
		this.proverName = proverName;
		this.sequentName = sequentName;
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
		return sequentName;
	}

	public String getProverName() {
		return proverName;
	}
}
