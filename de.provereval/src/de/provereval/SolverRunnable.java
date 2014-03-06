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
		for (ProverEvaluationTask task : tasks) {
			task.runTask();
			monitor.worked(1);

			if (monitor.isCanceled()) {
				canceled = true;
				monitor.done();
				return;
			}
		}
		monitor.done();

	}

}
