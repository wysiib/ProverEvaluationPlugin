package de.provereval.output;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import de.provereval.ProverEvaluationTask;
import de.provereval.labelproviders.ResultsLabelProvider;

public class ResultTableViewer extends TableViewer {
	private final Map<String, List<ProverEvaluationTask>> tasks;

	// mapping columns to provers -> used to sort, etc
	private final Map<TableColumn, String> columnsAndSolvers;

	public String getContentOfColumn(TableColumn t) {
		return columnsAndSolvers.get(t);
	}

	public ResultTableViewer(Composite parent,
			Map<String, List<ProverEvaluationTask>> grouped) {
		// define the TableViewer
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);

		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		getControl().setLayoutData(gd);

		this.tasks = grouped;
		columnsAndSolvers = new HashMap<TableColumn, String>();

		createColumns();

		final Table table = getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		setContentProvider(ArrayContentProvider.getInstance());
		setInput(grouped.values());
	}

	private void createColumns() {
		TableViewerColumn pos = createTableViewerColumn("Proof Obligation",
				200, 0);
		pos.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				List<ProverEvaluationTask> p = (List<ProverEvaluationTask>) element;
				return p.get(0).getProofObligationName();
			}
		});
		columnsAndSolvers.put(pos.getColumn(), "pos");

		Collection<List<ProverEvaluationTask>> values = tasks.values();
		List<ProverEvaluationTask> first = values.iterator().next();
		for (final ProverEvaluationTask t : first) {
			TableViewerColumn col = createTableViewerColumn(t.getProverName(),
					200, 0);
			col.setLabelProvider(new ResultsLabelProvider(t.getProverName()));
			columnsAndSolvers.put(col.getColumn(), t.getProverName());
		}

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(this,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();

		column.addListener(SWT.Selection, new SortListener(this));

		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);

		return viewerColumn;
	}

}
