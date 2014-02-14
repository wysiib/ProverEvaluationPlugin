package de.provereval.labelproviders;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import de.provereval.ProverEvaluationTask;

public class ResultsLabelProvider extends ColumnLabelProvider {
	ProverEvaluationTask task;

	public ResultsLabelProvider(ProverEvaluationTask t) {
		task = t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getText(Object element) {
		List<ProverEvaluationTask> p = (List<ProverEvaluationTask>) element;

		for (ProverEvaluationTask task : p) {
			if (task.getProverName().equals(task.getProverName())) {
				if (task.isProven()) {
					return "\u2713";
				} else {
					return "-";
				}
			}
		}

		return "No Result";
	}
}