package sandbox9.mybatis.stitch.view.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.sse.core.StructuredModelManager;

import sandbox9.mybatis.stitch.Activator;
import sandbox9.mybatis.stitch.view.util.CheetahXMLParser;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by SejongPark on 15. 2. 6..
 */
public class StitchView extends ViewPart {

	public static final String ID = "sandbox9.mybatis.stitch.view.ui.MybatisHelperView";

	private TableViewer sqlIdListViewer;

	private TextViewer paramterTextViewer;

	private PageChangeListener pageChangeListener;

	private Map<String, SqlSource> sqlSourceMap;

	private String currentSelectedSqlKey;

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
		MybatisBindAction lCustomAction = new MybatisBindAction();
		lCustomAction.setText("Start");
		// FIXME icon 설정안되는 이유 찾기
		lCustomAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/run.gif"));
		getViewSite().getActionBars().getToolBarManager().add(lCustomAction);
	}

	private void createAction() {
		// 작업중인 페이지가 변경되었는지 여부를 확인한다.
		pageChangeListener = new PageChangeListener();
		getSite().getPage().addPostSelectionListener(pageChangeListener);

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

	// 일단은 변경작업은 여기서 진행.
	// 리스너는 단순히 작업을 트리거하는 역할만 하여야 한다.
	// 변경행위는 mybatisView에서 진행하여야 한다.
	//
	// TODO 리팩토링
	// 인터페이스를 한번 래핑하여, 가독성 있도록 변경이 필요.
	private class PageChangeListener implements ISelectionListener {

		private final static long BUFFER_MILSECOND = 2000;

		private long lastChangedMillis = 0;

		private String xmlString;

		// mybatis xml parser
		private CheetahXMLParser cheetahXMLParser = new CheetahXMLParser();

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
					IDocument xmlDocument = (IDocument) part.getAdapter(IDocument.class);
					xmlString = xmlDocument.get();
					sqlSourceMap = cheetahXMLParser.generateCrudSqlSourceMap(xmlString);

					sqlIdListViewer.setContentProvider(new SqlIdListContentProvider(sqlSourceMap));
					sqlIdListViewer.refresh();
				}
			});
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

	public class MybatisBindAction extends Action implements IWorkbenchAction {
		private ObjectMapper mapper = new ObjectMapper();

		public void run() {

			String sql = generateSql();
			Shell activeShell = getActiveShell();
			new MyDialog(activeShell, sql).open();
		}

		private Shell getActiveShell() {
			Shell activeShell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell();
			return activeShell;
		}

		@SuppressWarnings("unchecked")
		private String generateSql() {
			// textarea의 json문자열을 가지고 온다.
			String sql = null;

			try {
				String param = paramterTextViewer.getDocument().get();
				Map<String, Object> paramMap = mapper.readValue(param,
						HashMap.class);
				BoundSql boundSql = sqlSourceMap.get(currentSelectedSqlKey)
						.getBoundSql(paramMap);
				sql = boundSql.getSql();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return sql;

		}

		public void dispose() {

		}
	}
}