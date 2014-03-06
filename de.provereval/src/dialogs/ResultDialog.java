package dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import de.provereval.ProverEvaluationTask;
import de.provereval.output.CSVExporter;

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

		new ResultTableViewer(body, grouped);

		final Button exportButton = new Button(body, SWT.PUSH);
		exportButton.setText("Export to CSV");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				export(parent);
			}
		});

		return body;
	}

	private void export(Composite parent) {
		FileDialog fd = new FileDialog(parentShell, SWT.SAVE);
		fd.setText("Export to CSV");
		String[] filterExt = { "*.csv" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		CSVExporter.exportToCSVFile(grouped, path);
	}

}