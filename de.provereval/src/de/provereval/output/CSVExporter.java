package de.provereval.output;

import java.io.*;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.*;

public class CSVExporter {
	public static void exportToCSVFile(TableViewer viewer, String path) {
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
