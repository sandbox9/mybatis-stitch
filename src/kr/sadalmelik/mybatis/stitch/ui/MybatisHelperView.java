package kr.sadalmelik.mybatis.stitch.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kr.sadalmelik.mybatis.stitch.Activator;
import kr.sadalmelik.mybatis.stitch.util.CheetahXMLParser;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by SejongPark on 15. 2. 6..
 */
public class MybatisHelperView extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "kr.sadalmelik.mybatis.helper.SimpleView";

	// UI 객체
	private TableViewer sqlIdListViewer;
	private TextViewer paramterTextViewer;

	// 이벤트 리스너
	private SelectionListener selectionListener;

	// mybatis xml parser
	private CheetahXMLParser cheetahXMLParser;

	// sqlMapViewer
	private Map<String, SqlSource> sqlSourceMap;
	
	private ObjectMapper mapper = new ObjectMapper();
	private String selectedSqlKey;

	@Override
	public void createPartControl(Composite parent) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		parent.setLayout(fillLayout);

		Composite sqlIdListComposite = new Composite(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		// TODO ID찾기 작업할 것
		// Text idSearchText = new Text(sqlIdListComposite, SWT.BORDER);

		sqlIdListViewer = new TableViewer(sqlIdListComposite, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		sqlIdListViewer.setContentProvider(new SqlIdListContentProvider(
				sqlSourceMap));
		sqlIdListViewer.setLabelProvider(new ViewLabelProvider());
		sqlIdListViewer.setSorter(new ViewerSorter() {
		});
		sqlIdListViewer.setInput(getViewSite());

		sqlIdListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent arg0) {
						IStructuredSelection selection = (IStructuredSelection) sqlIdListViewer
								.getSelection();
						selectedSqlKey = (String)selection.getFirstElement();
					}

				});

		sqlIdListComposite.setLayout(fillLayout);

		// 여기에는 파라미터 데이터가 들어갈 예정.
		paramterTextViewer = new TextViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER | SWT.CURSOR_IBEAM);

		paramterTextViewer.setEditable(true);
		paramterTextViewer.setDocument(new Document("test"));

		// Custom Action for the View's Menu
		CustomAction lCustomAction = new CustomAction();
		lCustomAction.setText("Start");
		lCustomAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/run.gif"));
		getViewSite().getActionBars().getToolBarManager().add(lCustomAction);

		initializeSelectionListener();
		cheetahXMLParser = new CheetahXMLParser();
	}

	@Override
	public void setFocus() {
		// ?????
	}

	private void initializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addPostSelectionListener(selectionListener);
	}

	// 일단은 변경작업은 여기서 진행.
	// 리스너는 단순히 작업을 트리거하는 역할만 하여야 한다.
	// 변경행위는 mybatisView에서 진행하여야 한다.
	//
	// TODO 리팩토링
	// 인터페이스를 한번 래핑하여, 가독성 있도록 변경이 필요.
	private class SelectionListener implements ISelectionListener {

		private final static long BUFFER_MILSECOND = 2000;
		private long lastChangedMillis = 0;
		private String xmlString;

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			// 에디터일 경우에만 데이터를 가지고 온다.
			if (false == part instanceof EditorPart)
				return;

			// 2초의 버퍼링 시간을 둔다.
			if (System.currentTimeMillis() - BUFFER_MILSECOND < lastChangedMillis)
				return;

			lastChangedMillis = System.currentTimeMillis();

			// 쓰레드를 실행시킵니다.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IDocument xmlDocument = (IDocument) part
							.getAdapter(IDocument.class);
					xmlString = xmlDocument.get();

					sqlSourceMap = cheetahXMLParser
							.generateCrudSqlSourceMap(xmlString);
					sqlIdListViewer
							.setContentProvider(new SqlIdListContentProvider(
									sqlSourceMap));
					sqlIdListViewer.refresh();
				}
			});
		}
	}

	public class CustomAction extends Action implements IWorkbenchAction {
		@SuppressWarnings("unchecked")
		public void run() {
			//textarea의 json문자열을 가지고 온다.
			String param = paramterTextViewer.getDocument().get();
			
			Map<String, Object> paramMap = null;
			try {
				paramMap = mapper.readValue(param, HashMap.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BoundSql boundSql = sqlSourceMap.get(selectedSqlKey).getBoundSql(paramMap);
			
			String sql = boundSql.getSql();
			
			Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			Dialog dialog = new MyDialog(activeShell, sql);
			dialog.open();
		}

		public void dispose() {
			
		}
	}

}