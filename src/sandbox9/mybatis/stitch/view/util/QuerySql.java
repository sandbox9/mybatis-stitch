package sandbox9.mybatis.stitch.view.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import sandbox9.mybatis.stitch.StitchQueryException;

public class QuerySql {
	private static Configuration configuration;
	static {
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		configuration = new Configuration();
	}

	public ResultSet execute(String url, String username, String password,
			BoundSql boundSql, Object parameterObject) throws StitchQueryException{
		ResultSet resultSet = null;
		try {
			Connection connection = getConnection(url, username, password);
			PreparedStatement ps = generatePreparedStatement(
					connection.prepareStatement(boundSql.getSql()), boundSql,
					parameterObject);
			
			resultSet = ps.executeQuery();
		} catch (SQLException e) {
			throw new StitchQueryException(e.getMessage());
		}
		
		return resultSet;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private PreparedStatement generatePreparedStatement(PreparedStatement ps,
			BoundSql boundSql, Object parameterObject) throws SQLException {
		List<ParameterMapping> parameterMappings = boundSql
				.getParameterMappings();
		if (parameterMappings != null) {
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				Object value;
				String propertyName = parameterMapping.getProperty();
				if (parameterObject == null) {
					value = null;
				} else {
					MetaObject metaObject = configuration
							.newMetaObject(parameterObject);
					value = metaObject.getValue(propertyName);
				}
				TypeHandler typeHandler = parameterMapping.getTypeHandler();
				JdbcType jdbcType = parameterMapping.getJdbcType();
				if (value == null && jdbcType == null) {
					jdbcType = configuration.getJdbcTypeForNull();
				}
				
				typeHandler.setParameter(ps, i + 1, value, jdbcType);
			}
		}

		return ps;
	}

	private Connection getConnection(String url, String user, String password)
			throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
}
