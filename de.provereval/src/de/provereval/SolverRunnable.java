package de.provereval;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class SolverRunnable implements IRunnableWithProgress {
	private final List<ProverEvaluationTask> tasks;
	private boolean canceled = false;

	public SolverRunnable(List<ProverEvaluationTask> tasks) {
		this.tasks = tasks;
	}

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		monitor.beginTask("Running Provers", tasks.size());
		for (int i = 0; i < tasks.size(); i++) {
			ProverEvaluationTask task = tasks.get(i);
			task.runTask();
			monitor.worked(1);
			monitor.setTaskName("Proof Obligation " + i + " of " + tasks.size());
			if (monitor.isCanceled()) {
				canceled = true;
				monitor.done();
				return;
			}
		}
		monitor.done();

	}

}
