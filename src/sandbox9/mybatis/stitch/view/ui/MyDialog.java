package sandbox9.mybatis.stitch.view.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MyDialog extends Dialog {

	private String sql;
	
	public MyDialog(Shell parentShell, String sql) {
		super(parentShell);
		this.sql = sql;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		FillLayout fillLayout = new FillLayout();
		
		container.setLayout(fillLayout);
		fillLayout.marginWidth = 5;
		fillLayout.marginHeight = 5;
		
	    TextViewer textViewer = new TextViewer(container, SWT.BORDER | SWT.V_SCROLL );
	    textViewer.setDocument(new Document(sql));
	    textViewer.setEditable(false);
	    
//		Button button = new Button(container, SWT.PUSH);
//		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//		button.setText("Press me");
//		button.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				System.out.println("Pressed");
//			}
//		});

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Selection dialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 450);
	}

}