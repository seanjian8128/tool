/**
 * ©2014 融建信息技术(厦门)有限公司 版权所有
 */
package com.sean.db.conn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;

import com.sean.db.common.Constants;
import com.sean.db.common.DAOException;
import com.sean.db.common.FieldType;

/**
 * @author sean <a href="mailto:seanjian@rj-it.com">sean</a> 2014-4-10 下午4:51:53
 */

public class SQLUtils {
	/**
	 * @param name
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	public static FieldType getType(int sqlType, String typeName,
			Dialect dialect) throws DAOException {
		switch (sqlType) {

		case Types.TINYINT:
		case Types.INTEGER:
		case Types.SMALLINT:
			return FieldType.INT;
		case Types.BIGINT:
			return FieldType.LONG;
		case Types.CLOB:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCLOB:
			return FieldType.CLOB;
		case Types.VARCHAR:
		case Types.NCHAR:
			return FieldType.VARCHAR;
		case Types.CHAR:
			return FieldType.CHAR;
		case Types.BOOLEAN:
		case Types.BIT:
			return FieldType.BOOLEAN;
		case Types.BLOB:
		case Types.LONGVARBINARY:
			return FieldType.BLOB;
		case Types.DATE:
			return FieldType.DATE;
		case Types.TIME:
			return FieldType.TIME;
		case Types.TIMESTAMP:
			return FieldType.DATETIME;
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.REAL:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return FieldType.NUMERIC;
		}
		FieldType type = dialect.exchangeSqlType(typeName);
		if (type == null){
			throw new DAOException("dao.access.getType");
		}
		else{
			return type;
		}
	}

	public static void closeQuetely(PreparedStatement ps) {
		try {
			ps.close();
			ps = null;
		} catch (Exception e) {
		}
	}

	public static Dialect getDialect(String jdbcUrl) throws Exception {
		Dialect dialect = null;
		String type = jdbcUrl.substring(5).toLowerCase();
		if (StringUtils.isBlank(type)) {
			throw new Exception("JDBC URL格式有问题,无法匹配数据库类型");
		} else {
			if (type.startsWith("mysql")) {
				dialect = new Db2Dialect();
				return dialect;
			} else if (type.startsWith("derby")) {
				dialect = new DerbyDialect();
				return dialect;
			} else if (type.startsWith("oracle")) {
				dialect = new OracleDialect();
				return dialect;
			} else if (type.startsWith("postgres")) {
				dialect = new PostgresDialect();
				return dialect;
			} else if (type.startsWith("sybase")) {
				dialect = new SybaseDialect();
				return dialect;
			} else if (type.startsWith("db2")) {
				dialect = new Db2Dialect();
				return dialect;
			} else if (type.startsWith("sqlserver")) {
				dialect = new SqlserverDialect();
				return dialect;
			} else
				throw new Exception("JDBC URL类型有问题,无法匹配数据库类型");
		}
	}

	public static int getDBType(String jdbcUrl) throws Exception {
		String type = jdbcUrl.substring(5).toLowerCase();
		if (StringUtils.isBlank(type)) {
			throw new Exception("JDBC URL格式有问题,无法匹配数据库类型");
		} else {
			if (type.startsWith("mysql")) {
				return Constants.MYSQL;
			} else if (type.startsWith("derby")) {
				return Constants.DERBY;
			} else if (type.startsWith("oracle")) {
				return Constants.ORACLE;
			} else if (type.startsWith("postgres")) {
				return Constants.POSTGRES;
			} else if (type.startsWith("sybase")) {
				return Constants.SYBASE;
			} else if (type.startsWith("db2")) {
				return Constants.DB2;
			} else if (type.startsWith("sqlserver")) {
				return Constants.SQLSERVER;
			} else
				throw new Exception("JDBC URL类型有问题,无法匹配数据库类型");
		}
	}

	public static void closeQuetely(ResultSet rs) {
		try {
			rs.close();
			rs = null;
		} catch (Exception e) {
		}
	}
}
