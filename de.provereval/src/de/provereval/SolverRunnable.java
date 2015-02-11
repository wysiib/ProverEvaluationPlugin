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

		int cores = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(cores);

		List<Future<ProverEvaluationResult>> invokeAll = pool.invokeAll(tasks);

		for (int i = 0; i < tasks.size(); i++) {
			try {
				results.add(invokeAll.get(i).get());
			} catch (ExecutionException e) {
				canceled = true;
				monitor.done();
				return;
			}

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

	public List<ProverEvaluationResult> getResults() {
		return results;
	}
}
