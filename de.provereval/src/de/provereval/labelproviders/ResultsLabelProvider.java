package de.provereval.labelproviders;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import de.provereval.ProverEvaluationResult;

public class ResultsLabelProvider extends ColumnLabelProvider {
	String name;

	public ResultsLabelProvider(String string) {
		name = string;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getText(Object element) {
		List<ProverEvaluationResult> p = (List<ProverEvaluationResult>) element;

		for (ProverEvaluationResult candidate : p) {
			if (candidate.getProverName().equals(name)) {
				if (candidate.isProven()) {
					return "\u2713";
				} else {
					return "-";
				}
			}
		}

		return "No Result";
	}
}