package com.sean.db.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sean.db.common.Constants;
import com.sean.db.common.DAOException;
import com.sean.db.common.FieldType;
import com.sean.db.common.TableBean;
import com.sean.db.common.TableField;
import com.sean.db.common.TableFk;
import com.sean.db.common.TableFkField;
import com.sean.db.common.TableIndex;
import com.sean.db.conn.Dialect;
import com.sean.db.conn.SQLUtils;
import com.sean.tool.util.Utils;

public class TableTask {

	public final static int CREATE_TABLE_ONLY = 1;

	public final static int CREATE_FOREIGNKEY_ONLY = 2;

	public final static int CREATE_TABLE_FOREIGHKEY = 3;

	public final static int TABLE = 1;

	public final static int VIEW = 2;

	private Logger logger = null;

	public TableTask(Class logKey) {
		logger = Logger.getLogger(logKey);
	}

	public TableBean getTableInfo(Connection conn, String jdbcUrl, String table)
			throws Exception {

		DatabaseMetaData dbmetadata = conn.getMetaData();
		TableBean bean = new TableBean();
		bean.setCurrTableName(table);
		int existed = checkTable(dbmetadata, table);
		if (existed > 0) {
			// getFields(dbmetadata, bean);
			getFields(conn, jdbcUrl, table, bean);
			getKeyInfo(dbmetadata, bean);
			getIndexInfo(dbmetadata, bean);
			getFKeyInfo(dbmetadata, bean);
		}
		return bean;
	}

	private void getFields(Connection conn, String jdbcUrl, String table,
			TableBean bean) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<TableField> fields = new ArrayList<TableField>();
		bean.setFields(fields);

		try {
			Dialect dialect = SQLUtils.getDialect(jdbcUrl);
			ps = conn.prepareStatement("select * from " + table + " where 1=2");
			rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int count = meta.getColumnCount();
			for (int i = 1; i <= count; i++) {
				String column = meta.getColumnName(i);
				String typeName = meta.getColumnTypeName(i);
				FieldType type = SQLUtils.getType(meta.getColumnType(i),
						typeName, dialect);
				int precision = meta.getPrecision(i);
				int scale = meta.getScale(i);
				boolean nullable = meta.isNullable(i) == ResultSetMetaData.columnNoNulls ? true
						: false;

				TableField field = new TableField();
				field.setName(column);
				field.setNotNull(nullable);
				field.setType(type);
				field.setDescription(column);
				// field.setDefaultValue(defaultv);
				field.setLength(precision);
				field.setDecimal(scale);
				fields.add(field);
			}

			DatabaseMetaData dmd = conn.getMetaData();
			fillMoreInfo(dmd, bean);
		} finally {
			SQLUtils.closeQuetely(ps);
			SQLUtils.closeQuetely(rs);
		}
	}

	/**
	 * @param dbmetadata
	 * @param bean
	 */
	private void fillMoreInfo(DatabaseMetaData dbmetadata, TableBean bean)
			throws DAOException {
		ResultSet rs = null;
		try {
			rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
					.toLowerCase(), null);
			List<TableField> fields = wrapperFieldsFromDBMetaData(rs);
			if (fields.size() == 0) {
				SQLUtils.closeQuetely(rs);
				rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
						.toLowerCase(), null);
				fields = wrapperFieldsFromDBMetaData(rs);
				if (fields.size() == 0) {
					SQLUtils.closeQuetely(rs);
					rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
							.toUpperCase(), null);
					fields = wrapperFieldsFromDBMetaData(rs);
				}
			}

			Map<String, TableField> map = new HashMap<String, TableField>();
			for (TableField tf : bean.getFields()) {
				map.put(tf.getName(), tf);
			}

			for (TableField tf : fields) {
				if (map.containsKey(tf.getName())) {
					TableField f = map.get(tf.getName());
					f.setDefaultValue(tf.getDefaultValue());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	private List<TableField> wrapperFieldsFromDBMetaData(ResultSet rs)
			throws SQLException, DAOException {
		List<TableField> fields = new ArrayList<TableField>();
		while (rs.next()) {
			String column = rs.getString("COLUMN_NAME");
			String defaultv = rs.getString("COLUMN_DEF");
			TableField field = new TableField();
			if(StringUtils.isNotBlank(defaultv))
			{
				if(defaultv.charAt(0) == '\''){
					defaultv = defaultv.substring(1,defaultv.length());
				}
				if(defaultv.charAt(defaultv.length() - 1) == '\''){
					defaultv = defaultv.substring(0,defaultv.length()-1);
				}
			}
			field.setName(column);
			field.setDefaultValue(defaultv);
			fields.add(field);

		}
		return fields;
	}

	// /**
	// * @param dbmetadata
	// * @param bean
	// */
	// private void getFields(DatabaseMetaData dbmetadata, TableBean bean)
	// throws DAOException {
	// ResultSet rs = null;
	// try {
	// // String schema = getSchema(dbmetadata);
	// // String catalog = getCatalog(dbmetadata);
	// rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
	// .toLowerCase(), null);
	//
	// List<TableField> fields = wrapperFields(rs);
	// if (fields.size() == 0) {
	// SQLUtils.closeQuetely(rs);
	// rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
	// .toLowerCase(), null);
	// fields = wrapperFields(rs);
	// if (fields.size() == 0) {
	// SQLUtils.closeQuetely(rs);
	// rs = dbmetadata.getColumns(null, null, bean.getCurrTableName()
	// .toUpperCase(), null);
	// fields = wrapperFields(rs);
	// }
	// }
	// bean.setFields(fields);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// throw new DAOException(e);
	// } finally {
	// SQLUtils.closeQuetely(rs);
	// }
	// }
	//
	// private List<TableField> wrapperFields(ResultSet rs) throws SQLException,
	// DAOException {
	// List<TableField> fields = new ArrayList<TableField>();
	// while (rs.next()) {
	// String column = rs.getString("COLUMN_NAME");
	// String defaultv = rs.getString("COLUMN_DEF");
	// int type = rs.getInt("DATA_TYPE");
	//
	// int len = rs.getInt("COLUMN_SIZE");
	// String nullable = rs.getString("IS_NULLABLE");
	// TableField field = new TableField();
	// field.setName(column);
	// field.setNotNull(nullable.equals("YES") ? false : true);
	// field.setType(SQLUtils.getType(type));
	// field.setDescription(column);
	// field.setDefaultValue(defaultv);
	// field.setLength(len);
	// fields.add(field);
	//
	// }
	// return fields;
	// }

	private String getSchema(DatabaseMetaData dbmetadata) throws DAOException {
		ResultSet rs = null;
		try {
			rs = dbmetadata.getSchemas();
			if (rs.next()) {
				return rs.getString("TABLE_SCHEM");
			}
			return null;
		} catch (SQLException e) {
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	private String getCatalog(DatabaseMetaData dbmetadata) throws DAOException {
		ResultSet rs = null;
		try {
			rs = dbmetadata.getCatalogs();
			if (rs.next()) {
				return rs.getString("TABLE_CAT");
			}
			return null;
		} catch (SQLException e) {
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	/**
	 * @param bean
	 */
	private void getFKeyInfo(DatabaseMetaData dbmetadata, TableBean bean)
			throws DAOException {
		ResultSet rs = null;
		try {
			// String schema = getSchema(dbmetadata);
			// String catalog = getCatalog(dbmetadata);
			rs = dbmetadata.getImportedKeys(null, null, bean.getCurrTableName());
			List<TableFk> fks = wrapperFKeyinfo(bean, rs);
			if (fks.size() == 0) {
				SQLUtils.closeQuetely(rs);
				rs = dbmetadata.getImportedKeys(null, null, bean.getCurrTableName()
						.toLowerCase());
				fks = wrapperFKeyinfo(bean, rs);
				if (fks.size() == 0) {
					SQLUtils.closeQuetely(rs);
					rs = dbmetadata.getImportedKeys(null, null, bean
							.getCurrTableName().toUpperCase());
					fks = wrapperFKeyinfo(bean, rs);
				}
			}

			bean.setForeignKeys(fks);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	private List<TableFk> wrapperFKeyinfo(TableBean bean, ResultSet rs)
			throws SQLException, DAOException {
		List<TableFk> fks = new ArrayList<TableFk>();
		while (rs.next()) {
			String ptable = rs.getString("PKTABLE_NAME");
			String ftable = rs.getString("FKTABLE_NAME");
			String pkcolumn = rs.getString("PKCOLUMN_NAME");
			String fkcolumn = rs.getString("FKCOLUMN_NAME");
			String pkeyname = rs.getString("PK_NAME");
			String fkeyname = rs.getString("FK_NAME");

			boolean existed = false;
			if (bean.getForeignKeys() != null)
				for (TableFk fk : bean.getForeignKeys()) {
					if (StringUtils.equals(pkeyname, fk.getName())
							|| StringUtils.equals(fkeyname, fk.getName())) {
						if (StringUtils.isNotBlank(pkcolumn)
								&& StringUtils.isNotBlank(fkcolumn)) {
							TableFkField fkf = new TableFkField();
							fkf.setFrom(fkcolumn);
							fkf.setTo(pkcolumn);
							fk.getFields().add(fkf);
						}
						existed = true;
					}
				}
			if (!existed && StringUtils.isNotBlank(pkeyname)
					&& StringUtils.isNotBlank(fkeyname)) {
				if (StringUtils.isNotBlank(pkcolumn)
						&& StringUtils.isNotBlank(fkcolumn)) {
					TableFk fk = new TableFk();
					fk.setName(fkeyname);
					fk.setReferences(ptable);
					TableFkField fkf = new TableFkField();
					fkf.setFrom(fkcolumn);
					fkf.setTo(pkcolumn);
					if (fk.getFields() == null)
						fk.setFields(new LinkedList<TableFkField>());
					fk.getFields().add(fkf);
					fks.add(fk);
				}
			}
		}
		return fks;
	}

	/**
	 * @param bean
	 */
	private void getKeyInfo(DatabaseMetaData dbmetadata, TableBean bean)
			throws DAOException {
		ResultSet rs = null;
		try {
			// String schema = getSchema(dbmetadata);
			// String catalog = getCatalog(dbmetadata);
			rs = dbmetadata.getPrimaryKeys(null, null, bean.getCurrTableName());
			List<String> keys = wrapperKey(rs);
			if (keys.size() == 0) {
				SQLUtils.closeQuetely(rs);
				rs = dbmetadata.getPrimaryKeys(null, null, bean.getCurrTableName()
						.toLowerCase());
				keys = wrapperKey(rs);
				if (keys.size() == 0) {
					SQLUtils.closeQuetely(rs);
					rs = dbmetadata.getPrimaryKeys(null, null, bean
							.getCurrTableName().toUpperCase());
					keys = wrapperKey(rs);
				}
			}

			bean.setKey(keys);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	private List<String> wrapperKey(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		List<String> keys = new ArrayList<String>();
		while (rs.next()) {
			String key = rs.getString("COLUMN_NAME");
			keys.add(key);
		}
		return keys;
	}

	/**
	 * @param bean
	 */
	private void getIndexInfo(DatabaseMetaData dbmetadata, TableBean bean)
			throws DAOException {
		ResultSet rs = null;
		try {
			// String schema = getSchema(dbmetadata);
			// String catalog = getCatalog(dbmetadata);

			rs = dbmetadata.getIndexInfo(null, null, bean.getCurrTableName(),
					false, true);
			List<TableIndex> indexes = wrapperIndex(bean, rs, bean.getKey());
			if (indexes.size() == 0) {
				SQLUtils.closeQuetely(rs);
				rs = dbmetadata.getIndexInfo(null, null, bean.getCurrTableName()
						.toLowerCase(), false, true);
				indexes = wrapperIndex(bean, rs, bean.getKey());
				if (indexes.size() == 0) {
					SQLUtils.closeQuetely(rs);
					rs = dbmetadata.getIndexInfo(null, null, bean
							.getCurrTableName().toUpperCase(), false, true);
					indexes = wrapperIndex(bean, rs, bean.getKey());
				}
			}

			bean.setIndexes(indexes);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	private List<TableIndex> wrapperIndex(TableBean bean, ResultSet rs,
			List<String> keys) throws SQLException, DAOException {
		// TODO Auto-generated method stub
		List<TableIndex> indexes = new ArrayList<TableIndex>();
		while (rs.next()) {
			String indexName = rs.getString("INDEX_NAME");
			boolean nonunique = rs.getBoolean("NON_UNIQUE");
			String column = rs.getString("COLUMN_NAME");
			int type = rs.getInt("TYPE");
			if (type != DatabaseMetaData.tableIndexStatistic) {
				boolean existed = false;
				if (indexes != null)
					for (TableIndex index : indexes) {
						if (StringUtils.equals(indexName, index.getName())) {
							if (StringUtils.isNotBlank(column))
								index.getFields().add(column);
							existed = true;
						}
					}

				if (!existed && StringUtils.isNotBlank(indexName)) {
					TableIndex index = new TableIndex();
					if (StringUtils.isNotBlank(column)) {
						List<String> list = new LinkedList<String>();
						list.add(column);
						index.setFields(list);
					}
					index.setUnique(!nonunique);
					index.setName(indexName);
					indexes.add(index);
				}
			}
		}

		List<TableIndex> removes = new ArrayList<TableIndex>();
		if (keys != null) {
			for (TableIndex index : indexes) {
				if (index.getFields() != null) {
					boolean conflict = true;
					for (String field : index.getFields()) {
						if (keys.indexOf(field) < 0) {
							conflict = false;
							break;
						}
					}
					if (conflict) {
						removes.add(index);
					}
				}
			}
		}
		indexes.removeAll(removes);
		return indexes;
	}

	public int checkTable(DatabaseMetaData dbmetadata, String tableName)
			throws DAOException {
		// TODO Auto-generated method stub
		ResultSet rs = null;
		try {
			String[] tstr = { "TABLE" };
			// String schema = getSchema(dbmetadata);
			// String catalog = getCatalog(dbmetadata);
			rs = dbmetadata.getTables(null, null, tableName, tstr);
			if (rs.next()) {
				return TABLE;
			}
			SQLUtils.closeQuetely(rs);

			rs = dbmetadata
					.getTables(null, null, tableName.toLowerCase(), tstr);
			if (rs.next()) {
				return TABLE;
			}
			SQLUtils.closeQuetely(rs);
			rs = dbmetadata
					.getTables(null, null, tableName.toUpperCase(), tstr);
			if (rs.next()) {
				return TABLE;
			}
			SQLUtils.closeQuetely(rs);
			String[] vstr = { "VIEW" };
			// schema = getSchema(dbmetadata);
			// catalog = getCatalog(dbmetadata);
			rs = dbmetadata.getTables(null, null, tableName, vstr);
			if (rs.next()) {
				return VIEW;
			}
			SQLUtils.closeQuetely(rs);

			rs = dbmetadata
					.getTables(null, null, tableName.toLowerCase(), vstr);
			if (rs.next()) {
				return VIEW;
			}
			SQLUtils.closeQuetely(rs);
			rs = dbmetadata
					.getTables(null, null, tableName.toUpperCase(), vstr);
			if (rs.next()) {
				return VIEW;
			}
			SQLUtils.closeQuetely(rs);
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e.getMessage());
		} finally {
			SQLUtils.closeQuetely(rs);
		}
	}

	public void createTable(String jdbcUrl, Connection conn, TableBean table,
			int createType) throws Exception {
		try {
			// 视图表达式为不为空，表示执行自定义的视图表达式
			if (createType == CREATE_TABLE_ONLY
					|| createType == CREATE_TABLE_FOREIGHKEY) {
				if (StringUtils.isNotBlank(table.getView())) {
					PreparedStatement ps = null;
					try {
						ps = conn.prepareStatement(table.getView());
						ps.executeUpdate();
					} finally {
						SQLUtils.closeQuetely(ps);
					}
				} else {
					if (checkTable(conn.getMetaData(), table.getCurrTableName()) > 0)
						return;
					StringBuilder sql = new StringBuilder();
					int type = SQLUtils.getDBType(jdbcUrl);
					if (type == Constants.MYSQL) {
						buildMysqlTable(conn, sql, table);
						// buildOracleTable(conn, sql, table);
					} else if (type == Constants.DERBY) {
						buildDerbyTable(conn, sql, table);
					} else if (type == Constants.ORACLE) {
						buildOracleTable(conn, sql, table);
					} else if (type == Constants.POSTGRES) {
						buildPostgresTable(conn, sql, table);
					} else if (type == Constants.SYBASE) {
						buildSybaseTable(conn, sql, table);
					} else if (type == Constants.DB2) {
						buildDb2Table(conn, sql, table);
					} else if (type == Constants.SQLSERVER) {
						buildSqlServerTable(conn, sql, table);
					}
				}
			} else if (createType == CREATE_FOREIGNKEY_ONLY
					|| createType == CREATE_TABLE_FOREIGHKEY) {
				createForeignkey(conn, table, table.getCurrTableName());
			}
		} catch (Exception e) {
			// try {
			// dropTable(table);
			// } catch (Exception e1) {
			//
			// }
			throw e;
		}
	}

	private void createForeignkey(Connection conn, TableBean table,
			String tableName) throws Exception {
		List<TableFk> fkeylist = table.getForeignKeys();
		if (fkeylist != null) {
			for (TableFk fkey : fkeylist) {
				createForeignKey(conn, fkey, table.getCurrTableName());
			}
		}
	}

	private void createIndex(Connection conn, TableIndex tableIndex,
			String tableName) throws DAOException {
		if (tableIndex.getFields() == null
				|| tableIndex.getFields().size() == 0)
			return;
		StringBuffer sql = new StringBuffer();
		sql.setLength(0);
		sql.append("create ");
		if (tableIndex.isUnique()) {
			sql.append("unique index ");
		} else {
			sql.append(" index ");
		}
		sql.append(tableName + Utils.getRandomString(5));
		sql.append(" on ").append(tableName).append(" (");
		for (String fields : tableIndex.getFields()) {
			sql.append(fields.toLowerCase()).append(",");
		}
		sql = sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		PreparedStatement ps = null;
		try {
			logger.info(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(ps);
		}
	}

	private void createForeignKey(Connection conn, TableFk fkey,
			String tableName) throws Exception {
		if (fkey.getFields() == null || fkey.getFields().size() == 0)
			return;
		StringBuffer sql = new StringBuffer();
		sql.setLength(0);
		sql.append("ALTER TABLE  ").append(tableName)
				.append(" ADD CONSTRAINT  ");
		sql.append(fkey.getName()).append("  foreign key (");

		StringBuffer temp = new StringBuffer();
		StringBuffer temp1 = new StringBuffer();
		for (TableFkField field : fkey.getFields()) {
			temp.append(field.getFrom()).append(",");
			temp1.append(field.getTo()).append(",");
		}

		if (StringUtils.isBlank(temp.toString())
				|| StringUtils.isBlank(temp1.toString()))
			return;

		String s = temp.toString().substring(0, temp.length() - 1);
		String ss = temp1.toString().substring(0, temp1.length() - 1);
		sql.append(s).append(") REFERENCES ").append(fkey.getReferences())
				.append(" (").append(ss).append(") ");
		// if (fkey.getAction() != null) {
		// sql.append(fkey.getAction());
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			logger.info(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			throw new DAOException(e);
		} finally {
			SQLUtils.closeQuetely(ps);
		}
	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildSqlServerTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getSqlServerFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}
	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildDb2Table(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getDb2FieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}
	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildSybaseTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName().toLowerCase()).append(" ");
			String fieldsql = getSybaseFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}
	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildPostgresTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getPostgresFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}

	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildOracleTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getOracleFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}
	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildDerbyTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {
		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getDerbyFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
			logger.info(sql.toString() + " successfully");
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null)
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}

	}

	private String getKeyString(TableBean table) {
		List<String> keys = table.getKey();
		StringBuilder sb = new StringBuilder();
		if (keys != null && keys.size() > 0) {
			for (String key : keys) {
				if (StringUtils.isNotBlank(key))
					sb.append(key.toLowerCase()).append(",");
			}

			sb.deleteCharAt(sb.length() - 1);

			return sb.toString();
		} else {
			return null;
		}

	}

	/**
	 * @param sql
	 * @param table
	 */
	private void buildMysqlTable(Connection conn, StringBuilder sql,
			TableBean table) throws Exception {

		sql.append("create table ");
		sql.append(table.getCurrTableName()).append(" (");
		for (TableField field : table.getFields()) {
			sql.append(field.getName()).append(" ");
			String fieldsql = getMysqlFieldSql(field, table.getKey());
			sql.append(fieldsql);
			sql.append(",");
		}
		String key = getKeyString(table);
		if (StringUtils.isNotBlank(key))
			sql.append("primary key(").append(key).append(")");
		else
			sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		// if (table.getForeignKeys() != null && table.getForeignKeys().size() >
		// 0) {
		// appendForeighKey(sql, table);
		// } else {
		// sql.append(")");
		// }
		logger.info(sql.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			ps.executeUpdate();
		} finally {
			SQLUtils.closeQuetely(ps);
		}
		logger.info(sql.toString() + " successfully");
		List<TableIndex> indexList = table.getIndexes();

		if (indexList != null) {
			for (TableIndex tableIndex : indexList) {
				createIndex(conn, tableIndex, table.getCurrTableName());
			}

		}

	}

	private void appendForeighKey(StringBuilder sql, TableBean table) {

		for (TableFk fk : table.getForeignKeys()) {
			sql.append(", FOREIGN KEY ");
			String action = fk.getAction();
			StringBuilder fromsb = new StringBuilder();
			StringBuilder tosb = new StringBuilder();
			String ref = fk.getReferences();
			if (fk.getFields() != null) {
				for (TableFkField field : fk.getFields()) {
					fromsb.append(field.getFrom()).append(",");
					tosb.append(field.getTo()).append(",");
				}
				if (fromsb.length() > 0)
					fromsb.deleteCharAt(fromsb.length() - 1);
				if (tosb.length() > 0)
					tosb.deleteCharAt(tosb.length() - 1);
			}
			sql.append(" (").append(fromsb).append(") REFERENCES  ")
					.append(ref).append(" (").append(tosb).append(") ")
					.append(action);
		}
		if (table.getForeignKeys() != null && table.getForeignKeys().size() > 0)
			sql.append(")");
	}

	private String getPostgresFieldSql(TableField field, List<String> key)
			throws DAOException {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			sql.append(" varchar(");
			sql.append(field.getLength()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" bytea ");
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" text ");
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" decimal(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" int");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" bigint");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" timestamp(14) ");
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" time ");
		} else if (field.getType() == FieldType.DATE) {
			sql.append(" date ");
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" boolean");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}

		return sql.toString();
	}

	private String getSqlServerFieldSql(TableField field, List<String> key)
			throws DAOException {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			sql.append(" varchar(");
			sql.append(field.getLength()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" varbinary(max) ");
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" nvarchar(max) ");
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" decimal(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" bigint ");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" decimal(").append(field.getLength()).append(",0)");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" timestamp(14) ");
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" time ");
		} else if (field.getType() == FieldType.DATE) {
			sql.append(" date ");
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" bit ");
		} else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}
		return sql.toString();
	}

	private String getMysqlFieldSql(TableField field, List<String> key)
			throws DAOException {
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			sql.append(" varchar(");
			sql.append(field.getLength()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" MEDIUMBLOB ");
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" MEDIUMTEXT ");
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" decimal(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" int");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" bigint");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" timestamp ");
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" time ");
		}
		if (field.getType() == FieldType.DATE) {
			sql.append(" date ");
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" boolean");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}
		return sql.toString();
	}

	private String getDerbyFieldSql(TableField field, List<String> key)
			throws DAOException {
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			sql.append(" varchar(");
			sql.append(field.getLength()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" blob ");
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" clob ");
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" decimal(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" int");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" bigint");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" timestamp ");
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" time ");
		} else if (field.getType() == FieldType.DATE) {
			sql.append(" date ");
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" boolean");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}

		return sql.toString();
	}

	private String getSybaseFieldSql(TableField field, List<String> key)
			throws DAOException {
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			if (field.getLength() <= 6000) {
				sql.append(" varchar(");
				sql.append(field.getLength()).append(")");
			}
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			} else {
				if (field.isNotNull()
						|| (key != null && key.indexOf(field.getName()) >= 0)) {
					sql.append(" not null ");
				} else {
					sql.append(" null ");
				}
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" image ");
			if (field.isNotNull()
					|| (key != null && key.indexOf(field.getName()) >= 0)) {
				sql.append(" not null ");
			} else {
				sql.append(" null ");
			}
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" text ");
			if (field.isNotNull()
					|| (key != null && key.indexOf(field.getName()) >= 0)) {
				sql.append(" not null ");
			} else {
				sql.append(" null ");
			}
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" numeric(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			} else {
				if (field.isNotNull()
						|| (key != null && key.indexOf(field.getName()) >= 0)) {
					sql.append(" not null ");
				} else {
					sql.append(" null ");
				}
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" int ");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			} else {
				if (field.isNotNull()
						|| (key != null && key.indexOf(field.getName()) >= 0)) {
					sql.append(" not null ");
				} else {
					sql.append(" null ");
				}
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" numeric(").append(field.getLength()).append(",")
					.append("0)");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			} else {
				if (field.isNotNull()
						|| (key != null && key.indexOf(field.getName()) >= 0)) {
					sql.append(" not null ");
				} else {
					sql.append(" null ");
				}
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" datetime ");
			if (field.isNotNull()
					|| (key != null && key.indexOf(field.getName()) >= 0)) {
				sql.append(" not null ");
			} else {
				sql.append(" null ");
			}
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" datetime ");
			if (field.isNotNull()
					|| (key != null && key.indexOf(field.getName()) >= 0)) {
				sql.append(" not null ");
			} else {
				sql.append(" null ");
			}
		} else if (field.getType() == FieldType.DATE) {
			sql.append(" datetime ");
			if (field.isNotNull()
					|| (key != null && key.indexOf(field.getName()) >= 0)) {
				sql.append(" not null ");
			} else {
				sql.append(" null ");
			}
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			} else {
				if (field.isNotNull()
						|| (key != null && key.indexOf(field.getName()) >= 0)) {
					sql.append(" not null ");
				} else {
					sql.append(" null ");
				}
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" bit ");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				if (field.getDefaultValue().equalsIgnoreCase("false"))
					sql.append(" default 0 ");
				else if (field.getDefaultValue().equalsIgnoreCase("true"))
					sql.append(" default 1 ");
			}
		}

		else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}

		return sql.toString();
	}

	private String getOracleFieldSql(TableField field, List<String> key)
			throws DAOException {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer();
		if (field.getType() == FieldType.VARCHAR) {
			if (field.getLength() <= 0) {
				throw new DAOException("字符栏位[" + field.getName() + "]长度不能为0");
			}
			if (field.getLength() <= 6000) {
				sql.append(" varchar2(");
				sql.append(field.getLength()).append(")");
			} else {
				sql.append(" long ");
			}
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BLOB
				|| field.getType() == FieldType.OBJECT) {
			sql.append(" blob ");
		} else if (field.getType() == FieldType.CLOB) {
			sql.append(" clob ");
		} else if (field.getType() == FieldType.NUMERIC) {
			sql.append(" decimal(").append(field.getLength()).append(",")
					.append(field.getDecimal()).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.INT) {
			sql.append(" number(14) ");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.LONG) {
			sql.append(" number ");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default ").append(field.getDefaultValue())
						.append(" ");
			}
		} else if (field.getType() == FieldType.DATETIME) {
			sql.append(" timestamp ");
		} else if (field.getType() == FieldType.TIME) {
			sql.append(" timestamp ");
		} else if (field.getType() == FieldType.DATE) {
			sql.append(" date ");
		} else if (field.getType() == FieldType.CHAR) {
			int len = field.getLength();
			if (len <= 0)
				len = 1;
			sql.append(" char(").append(len).append(")");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				sql.append(" default '").append(field.getDefaultValue())
						.append("' ");
			}
		} else if (field.getType() == FieldType.BOOLEAN) {
			sql.append(" NUMBER(1,0) CHECK (" + field.getName() + " IN (1,0))");
			if (StringUtils.isNotBlank(field.getDefaultValue())) {
				if (field.getDefaultValue().equalsIgnoreCase("false"))
					sql.append(" default 0 ");
				else if (field.getDefaultValue().equalsIgnoreCase("true"))
					sql.append(" default 1 ");
			}
		} else {
			throw new DAOException("unsupport file type:" + field.getType());
		}

		if (field.isNotNull()) {
			sql.append(" not null ");
		}
		return sql.toString();
	}

	private String getDb2FieldSql(TableField field, List<String> key) {

		StringBuffer sql = new StringBuffer();
		return sql.toString();
	}

}
