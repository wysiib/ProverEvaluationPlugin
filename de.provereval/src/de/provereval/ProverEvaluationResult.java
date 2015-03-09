package de.provereval;

public class ProverEvaluationResult {
	public enum TaskStatus {
		PROVEN, NOT_PROVEN, CRASHED, DISPROVEN
	};

	private final TaskStatus status;
	private final long took;
	private final String sequentName;
	private final String proverName;

	public ProverEvaluationResult(String proverName, String sequentName,
			long took, TaskStatus status) {
		this.proverName = proverName;
		this.sequentName = sequentName;
		this.took = took;
		this.status = status;
	}

	public boolean isProven() {
		return status == TaskStatus.PROVEN;
	}

	public boolean isDisproven() {
		return status == TaskStatus.DISPROVEN;
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
