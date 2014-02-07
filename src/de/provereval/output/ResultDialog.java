package de.provereval.output;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.*;

import de.provereval.ProverEvaluationTask;

public class ResultDialog extends Dialog {
	private final Shell parentShell;
	private final Map<String, List<ProverEvaluationTask>> grouped;

	public ResultDialog(final Shell parentShell,
			Map<String, List<ProverEvaluationTask>> grouped) {
		super(parentShell);
		this.parentShell = parentShell;
		this.grouped = grouped;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite body = (Composite) super.createDialogArea(parent);

		final ResultTableViewer viewer = new ResultTableViewer(body, grouped);

		return body;
	}
}