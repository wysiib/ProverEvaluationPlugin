package dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import de.provereval.ProverEvaluationTask;
import de.provereval.output.*;

public class ResultDialog extends Dialog {
	private final Shell parentShell;
	private final FileDialog fd;
	private final Map<String, List<ProverEvaluationTask>> grouped;

	public ResultDialog(final Shell parentShell,
			Map<String, List<ProverEvaluationTask>> grouped) {
		super(parentShell);

		this.parentShell = parentShell;
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
				exportToCSV(parent);
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

	private void exportToCSV(Composite parent) {
		fd.setText("Export to CSV");
		String[] filterExt = { "*.csv" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		CSVExporter.exportToCSVFile(grouped, path);
	}

	private void exportToLatex(Composite parent) {
		fd.setText("Export to LaTeX");
		String[] filterExt = { "*.tex" };
		fd.setFilterExtensions(filterExt);
		String path = fd.open();
		LatexExporter.exportToLatexFile(grouped, path);
	}

}