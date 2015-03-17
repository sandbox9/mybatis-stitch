package sandbox9.mybatis.stitch.view.ui;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

import sandbox9.mybatis.stitch.Activator;
import sandbox9.mybatis.stitch.StitchParameterParsingException;
import sandbox9.mybatis.stitch.StitchQueryException;
import sandbox9.mybatis.stitch.StitchXmlParseException;
import sandbox9.mybatis.stitch.view.util.CheetahXMLParser;
import sandbox9.mybatis.stitch.view.util.QuerySql;
import sandbox9.mybatis.stitch.view.util.ViewUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by SejongPark on 15. 2. 6..
 */
public class StitchView extends ViewPart {

	private TableViewer sqlIdListViewer;

	private TextViewer paramterTextViewer;

	private PageChangeListener pageChangeListener;

	private Map<String, SqlSource> sqlSourceMap;

	private String currentSelectedSqlKey;

	private IJavaProject currentProject;

	private ObjectMapper mapper = new ObjectMapper();

	private CheetahXMLParser cheetahXMLParser = new CheetahXMLParser();

	@Override
	public void createPartControl(Composite parent) {
		setFillLayout(parent);

		createSqlIdListViewer(parent);
		createParamterTextViewer(parent);
		createActionBar();

		createAction();
	}

	private void setFillLayout(Composite composite) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		composite.setLayout(fillLayout);
	}

	private void createSqlIdListViewer(Composite parent) {
		sqlIdListViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		sqlIdListViewer.setContentProvider(new SqlIdListContentProvider(
				sqlSourceMap));
		sqlIdListViewer.setLabelProvider(new ViewLabelProvider());
		sqlIdListViewer.setInput(getViewSite());
	}

	private void createParamterTextViewer(Composite parent) {
		paramterTextViewer = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.CURSOR_IBEAM);
		paramterTextViewer.setEditable(true);
		paramterTextViewer.setDocument(new Document("{\"key\" : \"value\"}"));
	}

	private void createActionBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();

		Action viewSqlAction = new ViewSqlAction();
		viewSqlAction.setText("View SQL");
		viewSqlAction.setImageDescriptor(Activator
				.getImageDescriptor("/icons/run.gif"));

		Action querySqlAction = new QuerySqlAction();
		querySqlAction.setText("Query SQL");
		querySqlAction.setImageDescriptor(Activator
				.getImageDescriptor("/icons/run.gif"));

		toolBar.add(viewSqlAction);
		toolBar.add(querySqlAction);
	}

	private void createAction() {
		// 작업중인 페이지가 변경되었는지 여부를 확인한다.
		this.pageChangeListener = new PageChangeListener();
		getSite().getPage().addPostSelectionListener(pageChangeListener);
		getSite().getPage().addPartListener(new PageOpenAndCloseListener());

		// idList가 변경되었는지 여부를 체크한다.
		SqlIdListChangeListener sqlIdListChangeListener = new SqlIdListChangeListener();
		sqlIdListViewer.addSelectionChangedListener(sqlIdListChangeListener);
	}

	@Override
	public void setFocus() {
		// TODO
	}

	@Override
	public void dispose() {
		// 작업중 페이지 리스너 제거
		getSite().getPage().removePostSelectionListener(pageChangeListener);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getParamMap()
			throws StitchParameterParsingException {
		String param = paramterTextViewer.getDocument().get();
		Map<String, Object> paramMap = null;
		try {
			paramMap = mapper.readValue(param, HashMap.class);
		} catch (IOException e) {
			throw new StitchParameterParsingException(e.getMessage());
		}

		return paramMap;
	}

	private Shell getActiveShell() {
		Shell activeShell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		return activeShell;
	}

	private void generateSqlIdList(IWorkbenchPart part) {

		IDocument xmlDocument = (IDocument) part.getAdapter(IDocument.class);
		currentProject = ViewUtil.getJavaProject(xmlDocument);

		if (xmlDocument == null) {
			sqlIdListViewer.setContentProvider(new SqlIdListContentProvider());
			return;
		} else {
			// 쓰레드를 실행시킵니다.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					String xmlString = xmlDocument.get();
					IContentProvider provider = null;
					try {
						sqlSourceMap = cheetahXMLParser
								.generateCrudSqlSourceMap(xmlString);
						provider = new SqlIdListContentProvider(sqlSourceMap);
					} catch (StitchXmlParseException e) {
						provider = new SqlIdListContentProvider();
					}
					sqlIdListViewer.setContentProvider(provider);
					sqlIdListViewer.refresh();
				}
			});
		}
	}

	private void showErrorMessage(String title, String message) {
		MessageDialog.openError(getActiveShell(), title, message);
	}

	// 일단은 변경작업은 여기서 진행.
	// 리스너는 단순히 작업을 트리거하는 역할만 하여야 한다.
	// 변경행위는 mybatisView에서 진행하여야 한다.
	//
	// TODO 리팩토링
	// 인터페이스를 한번 래핑하여, 가독성 있도록 변경이 필요.
	private class PageChangeListener implements ISelectionListener {

		private final static long BUFFER_MILSECOND = 3000;

		private long lastChangedMillis = 0;

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			// 에디터일 경우에만 데이터를 가지고 온다.
			if (false == part instanceof EditorPart)
				return;

			// 3초의 버퍼링 시간을 둔다.
			if (System.currentTimeMillis() - BUFFER_MILSECOND < lastChangedMillis)
				return;

			lastChangedMillis = System.currentTimeMillis();
			generateSqlIdList(part);
		}
	}

	private class PageOpenAndCloseListener implements IPartListener2 {

		public void genrateSqlIdListByPartRef(IWorkbenchPartReference partRef) {

			IWorkbenchPart part = partRef.getPart(false);
			generateSqlIdList(part);
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			// System.out.println("partActivated");
			// genrateSqlIdListByPartRef(partRef);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			// System.out.println("partBroughtToTop");
			genrateSqlIdListByPartRef(partRef);
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			// System.out.println("partClosed");
			genrateSqlIdListByPartRef(partRef);
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			// System.out.println("partOpened");
			genrateSqlIdListByPartRef(partRef);
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			// System.out.println("partVisible");
			genrateSqlIdListByPartRef(partRef);
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
			// TODO Auto-generated method stub
		}
	}

	private class SqlIdListChangeListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent arg0) {
			IStructuredSelection selection = (IStructuredSelection) sqlIdListViewer
					.getSelection();
			currentSelectedSqlKey = (String) selection.getFirstElement();
		}
	}

	public class ViewSqlAction extends Action implements IWorkbenchAction {
		public void run() {
			Map<String, Object> paramMap = null;
			// 리팩토링 할 것.
			try {
				paramMap = getParamMap();
			} catch (StitchParameterParsingException e1) {
				showErrorMessage("json parsing error!",
						"파라미터 데이터를 json으로 변환할 수 없습니다.");
				return;
			}
			
			if(currentSelectedSqlKey == null){
				showErrorMessage("sql 선택오류!", "먼저 SQL을 선택하세요.");
				return;
			}

			SqlSource sqlSource = sqlSourceMap.get(currentSelectedSqlKey);
			BoundSql boundSql = sqlSource.getBoundSql(paramMap);
			String sql = boundSql.getSql();
			new QueryResultDialog(getActiveShell(), sql).open();
		}

		public void dispose() {
			// TODO do something....
		}
	}

	public class QuerySqlAction extends Action implements IWorkbenchAction {
		public void run() {
			Map<String, Object> paramMap = null;

			try {
				paramMap = getParamMap();
			} catch (StitchParameterParsingException e1) {
				showErrorMessage("json parsing error!",
						"파라미터 데이터를 json으로 변환할 수 없습니다.");
				return;
			}

			if(currentSelectedSqlKey == null){
				showErrorMessage("sql 선택오류!", "먼저 SQL을 선택하세요.");
				return;
			}
			SqlSource sqlSource = sqlSourceMap.get(currentSelectedSqlKey);
			
			BoundSql boundSql = sqlSource.getBoundSql(paramMap);
			Shell activeShell = getActiveShell();
			IPreferenceStore pref = Activator
					.getPreferenceStore(currentProject);

			// url, id, password
			QuerySql querySql = new QuerySql();

			try {
				String url = pref.getString("url");
				String username = pref.getString("username");
				String password = pref.getString("password");
				
				if((url == null || url == "") || (username == null || username == "")){
					showErrorMessage("no database info!",
							"데이터 베이스 정보를 찾을 수 없습니다.\n프로젝트설정에서 데이터베이스 정보를 설정하여 주세요.");
					return;
				}

				ResultSet resultSet = querySql.execute(url, username, password, boundSql, paramMap);

				String resultMessage = "결과 : 성공\n\n";
				resultMessage += "파라미터 :\n";
				resultMessage += paramMap + "\n\n";
				resultMessage += "binding SQL : \n";
				resultMessage += boundSql.getSql();

				new QueryResultDialog(activeShell, resultMessage, resultSet)
						.open();
			} catch (StitchQueryException e) {
				String resultMessage = "결과 : 실패\n\n";
				resultMessage += "에러 메시지 :\n";
				resultMessage += e.getMessage() + "\n\n";
				resultMessage += "파라미터 :\n";
				resultMessage += paramMap + "\n\n";
				resultMessage += "binding SQL : \n";
				resultMessage += boundSql.getSql();

				new QueryResultDialog(activeShell, resultMessage).open();
			}

		}

		public void dispose() {
			// TODO do something....
		}
	}
}