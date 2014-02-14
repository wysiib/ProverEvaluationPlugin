package de.provereval.output;

import java.io.*;
import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import de.provereval.ProverEvaluationTask;

public class ResultDialog extends Dialog {
	private final Shell parentShell;
	private final Map<String, List<ProverEvaluationTask>> grouped;
	private ResultTableViewer viewer;

	public ResultDialog(final Shell parentShell,
			Map<String, List<ProverEvaluationTask>> grouped) {
		super(parentShell);
		this.parentShell = parentShell;
		this.grouped = grouped;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite body = (Composite) super.createDialogArea(parent);

		viewer = new ResultTableViewer(body, grouped);

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
		exportToCSVFile(path);
	}

	private void exportToCSVFile(String path) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path)));
			Table table = viewer.getTable();

			final int[] columnOrder = table.getColumnOrder();
			for (int i = 0; i < columnOrder.length; i++) {
				int columnIndex = columnOrder[i];
				TableColumn tableColumn = table.getColumn(columnIndex);

				writer.write(tableColumn.getText());
				if (i + 1 != columnOrder.length) {
					writer.write(",");
				}
			}
			writer.write("\n");

			final int itemCount = table.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				TableItem item = table.getItem(i);

				for (int j = 0; j < columnOrder.length; j++) {
					int columnIndex = columnOrder[j];

					if ("\u2713".equals(item.getText(columnIndex))) {
						writer.write("proven");
					} else {
						writer.write(item.getText(columnIndex));
					}

					if (j + 1 != columnOrder.length) {
						writer.write(",");
					}
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}

	}
}