package sandbox9.mybatis.stitch.view.ui;

import java.util.Map;

import org.apache.ibatis.mapping.SqlSource;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SqlIdListContentProvider implements IStructuredContentProvider {
	private boolean isSqlSourceMapExist;
	private Map<String, SqlSource> sqlSourceMap;

	public SqlIdListContentProvider(Map<String, SqlSource> sqlSourceMap) {
		super();
		if(sqlSourceMap != null){
			this.isSqlSourceMapExist = true;
			this.sqlSourceMap = sqlSourceMap;
		}else{
			this.isSqlSourceMapExist = false;
		}
	}
	
	public SqlIdListContentProvider() {
		super();
		this.isSqlSourceMapExist = false;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		if (isSqlSourceMapExist) {
			return sqlSourceMap.keySet().toArray();
		} else {
			return new String[] {"mapper파일이 아니거나 변환 중 에러가 발생하였습니다."};
		}

	}

}