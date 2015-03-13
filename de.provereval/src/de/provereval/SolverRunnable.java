package de.provereval;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class SolverRunnable implements IRunnableWithProgress {
	private final ProverEvaluationTaskList tasks;
	private final List<ProverEvaluationResult> results;
	private boolean canceled = false;

	public SolverRunnable(ProverEvaluationTaskList tasks) {
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
		ProverEvaluationTask currentTask;

		for (int i = 0; i < tasks.size(); i++) {
			try {
				currentTask = tasks.get(i);
				Future<ProverEvaluationResult> submit = pool
						.submit(currentTask);
				try {
					ProverEvaluationResult result = submit.get(25,
							TimeUnit.SECONDS);
					results.add(result);
				} catch (TimeoutException e) {
					results.add(currentTask.getTimeoutResult());
				}
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
