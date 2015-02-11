package de.provereval;

public class ProverEvaluationResult {
	public enum TaskStatus {
		PROVEN, NOT_PROVEN, CRASHED
	};

	private TaskStatus status;
	private long took;
	private final String sequentName;
	private final String proverName;

	public ProverEvaluationResult(String proverName, String sequentName,
			long took2, TaskStatus status2) {
		this.proverName = proverName;
		this.sequentName = sequentName;
	}

	public boolean isProven() {
		return status == TaskStatus.PROVEN;
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
