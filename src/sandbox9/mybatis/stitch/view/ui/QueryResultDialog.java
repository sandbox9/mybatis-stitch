package sandbox9.mybatis.stitch.view.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class QueryResultDialog extends Dialog {

	private String resultString;
	private ResultSet resultSet;
	private static final int DIALOG_WIDTH = 700;
	private static final int QUERY_RESULT_EXIST_DIALOG_HEIGHT = 700;
	private static final int DEFUALT_DIALOG_HEIGHT = 400;

	public QueryResultDialog(Shell parentShell, String resultString, ResultSet resultSet) {
		super(parentShell);

		this.resultSet = resultSet;
		this.resultString = resultString;
	}

	public QueryResultDialog(Shell parentShell, String sql) {
		super(parentShell);
		this.resultString = sql;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);

		container.setLayout(fillLayout);
		fillLayout.marginWidth = 5;
		fillLayout.marginHeight = 5;

		TextViewer textViewer = new TextViewer(container, SWT.BORDER
				| SWT.V_SCROLL| SWT.WRAP);
		textViewer.setDocument(new Document(resultString));
		textViewer.setEditable(false);
		
		
		if(resultSet != null){
			setQueryResultTable(container, resultSet);	
		}
		

		return container;
	}

	private void setQueryResultTable(Composite container, ResultSet resultSet) {
		TableViewer tableViewer = new TableViewer(container, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		

		generateTableLabels(tableViewer, resultSet);
		generateTableContents(table, resultSet);
	}

	private void generateTableContents(Table table, ResultSet resultSet) {
		try {
			int columnCount = resultSet.getMetaData().getColumnCount();

			while (resultSet.next() != false) {
				TableItem item = new TableItem(table, SWT.NONE);
				for (int idx = 0; idx < columnCount; idx++) 
					item.setText(idx, resultSet.getString(idx + 1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void generateTableLabels(TableViewer tableViewer, ResultSet resultSet) {
		try {
			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int idx = 0; idx < metaData.getColumnCount(); idx++) {
				TableViewerColumn col = new TableViewerColumn(tableViewer,
						SWT.NONE);
				col.getColumn().setWidth(150);
				col.getColumn().setText(metaData.getColumnLabel(idx + 1));
				col.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return "";
					}
				});
			}
		} catch (SQLException e) {
			// TODO 에러처리
			e.printStackTrace();
		}
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Query Result dialog");
	}

	@Override
	protected Point getInitialSize() {
		if(resultSet == null){
			return new Point(DIALOG_WIDTH, DEFUALT_DIALOG_HEIGHT);	
		}else{
			return new Point(DIALOG_WIDTH, QUERY_RESULT_EXIST_DIALOG_HEIGHT);
		}		
	}

}