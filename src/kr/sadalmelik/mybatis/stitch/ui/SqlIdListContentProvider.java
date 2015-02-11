package kr.sadalmelik.mybatis.stitch.ui;

import java.util.Map;

import org.apache.ibatis.mapping.SqlSource;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SqlIdListContentProvider implements IStructuredContentProvider {
	private Map<String, SqlSource> sqlSourceMap;

	public SqlIdListContentProvider(Map<String, SqlSource> sqlSourceMap) {
		super();
		this.sqlSourceMap = sqlSourceMap;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		if (sqlSourceMap == null) {
			return new String[] {};
		} else {
			return sqlSourceMap.keySet().toArray();
		}

	}

}