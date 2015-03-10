package de.provereval;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class SolverRunnable implements IRunnableWithProgress {
	private final List<ProverEvaluationTask> tasks;
	private final List<ProverEvaluationResult> results;
	private boolean canceled = false;

	public SolverRunnable(List<ProverEvaluationTask> tasks) {
		this.tasks = tasks;
		results = new ArrayList<ProverEvaluationResult>(tasks.size());
	}

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		monitor.beginTask("Running Provers", tasks.size());

		ExecutorService pool = Executors.newSingleThreadExecutor();

		for (int i = 0; i < tasks.size(); i++) {
			try {
				ProverEvaluationTask currentTask = tasks.get(i);
				Future<ProverEvaluationResult> submit = pool
						.submit(currentTask);
				results.add(submit.get());
			} catch (ExecutionException e) {
				System.out
						.println("Execution Exception when evaluating provers:");
				e.printStackTrace();
				monitor.setCanceled(true);
			}

			monitor.worked(1);
			monitor.setTaskName("Proof Obligation " + (i + 1) + " of "
					+ tasks.size());

			if (monitor.isCanceled()) {
				pool.shutdownNow();
				canceled = true;
				monitor.done();
				return;
			}
		}
		monitor.done();
	}

	public List<ProverEvaluationResult> getResults() {
		return results;
	}
}
