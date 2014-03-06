package de.provereval.output;

import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

import de.provereval.ProverEvaluationTask;

public class Sorter extends ViewerSorter {
	private final TableColumn column;
	private final int dir;

	public Sorter(TableColumn currentColumn, int dir) {
		super();
		this.column = currentColumn;
		this.dir = dir;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int returnValue = 0;

		ResultTableViewer tableViewer = (ResultTableViewer) viewer;
		List<ProverEvaluationTask> tasks1 = (List<ProverEvaluationTask>) e1;
		List<ProverEvaluationTask> tasks2 = (List<ProverEvaluationTask>) e2;

		String content = tableViewer.getContentOfColumn(column);

		/* sort by proof obligation name or by solver name */
		if (content.equals("pos")) {
			returnValue = tasks1.get(0).getProofObligationName()
					.compareTo(tasks2.get(0).getProofObligationName());
		} else {
			// find the belonging solver
			ProverEvaluationTask task1 = findSolver(content, tasks1);
			ProverEvaluationTask task2 = findSolver(content, tasks2);

			returnValue = new Boolean(task1.isProven()).compareTo(new Boolean(
					task2.isProven()));
		}

		if (this.dir == SWT.DOWN) {
			returnValue = returnValue * -1;
		}
		return returnValue;
	}

	private ProverEvaluationTask findSolver(String content,
			List<ProverEvaluationTask> tasks) {
		for (ProverEvaluationTask task : tasks) {
			if (task.getProverName().equals(content)) {
				return task;
			}
		}
		return null;
	}
}
