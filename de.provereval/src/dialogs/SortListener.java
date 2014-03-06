package dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class SortListener implements Listener {
	private final Table table;
	private final ResultTableViewer tableViewer;

	SortListener(ResultTableViewer t) {
		tableViewer = t;
		table = t.getTable();
	}

	@Override
	public void handleEvent(Event e) {
		// determine new sort column and direction
		TableColumn sortColumn = table.getSortColumn();
		TableColumn currentColumn = (TableColumn) e.widget;
		int dir = table.getSortDirection();

		if (sortColumn == currentColumn) {
			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
		} else {
			table.setSortColumn(currentColumn);
			dir = SWT.UP;
		}

		table.setSortDirection(dir);

		tableViewer.setSorter(new Sorter(currentColumn, dir));
	}
}
