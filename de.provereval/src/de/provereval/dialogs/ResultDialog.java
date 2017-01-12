package de.provereval.dialogs;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.provereval.ProverEvaluationResult;
import de.provereval.output.CSVExporter;
import de.provereval.output.LatexExporter;

public class ResultDialog extends Dialog {
	private final FileDialog fd;
	private final Map<String, List<ProverEvaluationResult>> grouped;

	public ResultDialog(final Shell parentShell, Map<String, List<ProverEvaluationResult>> grouped) {
		super(parentShell);
		fd = new FileDialog(parentShell, SWT.SAVE);

		this.grouped = grouped;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite body = (Composite) super.createDialogArea(parent);
		new ResultTableViewer(body, grouped);

		Composite buttons = new Composite(body, SWT.DOUBLE_BUFFERED);
		buttons.setLayout(new RowLayout());

		final Button exportToCSVButton = new Button(buttons, SWT.PUSH);
		exportToCSVButton.setText("Export to CSV");
		exportToCSVButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToCSV(parent, false);
			}
		});

		final Button appendToCSVButton = new Button(buttons, SWT.PUSH);
		appendToCSVButton.setText("Append to CSV");
		appendToCSVButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToCSV(parent, true);
			}
		});

		final Button exportToLatexButton = new Button(buttons, SWT.PUSH);
		exportToLatexButton.setText("Export to LaTeX");
		exportToLatexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToLatex(parent);
			}
		});

		return body;
	}

	private void exportToCSV(Composite parent, boolean append) {
		fd.setText("Export to CSV");
		String[] filterExt = { "*.csv" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		CSVExporter.exportToCSVFile(grouped, path, append);
	}

	private void exportToLatex(Composite parent) {
		fd.setText("Export to LaTeX");
		String[] filterExt = { "*.tex" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		LatexExporter.exportToLatexFile(grouped, path);
	}

}