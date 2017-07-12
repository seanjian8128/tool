package com.sean.db.data;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sean.db.common.FieldType;
import com.sean.db.common.TableBean;
import com.sean.db.common.TableField;
import com.sean.db.conn.SQLUtils;
import com.sean.tool.util.ConfigUtil;

/**
 * sean <a href="mailto:seanjian@163.com">sean</a> Mar 6, 2011
 */

public class DataTransfer extends AbstractDataTask {

	// private Properties dbProp = null;

	private Logger logger = Logger.getLogger(DataTransfer.class);

	private ConfigUtil configUtil = null;

	private int tCmt = 2000;

	private int maxRow = -1;

	private boolean createTable = false;

	private boolean copyData = false;

	private boolean createForeighKey = false;

	private boolean errIgnore = false;

	/**
	 * 
	 */
	public DataTransfer() {
		super(DataTransfer.class);
		PropertyConfigurator.configure("./config/log4j.properties");
	}

	public static void main(String[] str) {
		DataTransfer transfer = new DataTransfer();
		transfer.startTransfer();
	}

	public void startTransfer() {
		try {
			configUtil = new ConfigUtil();
			configUtil.load(FileUtils.openInputStream(new File(System
					.getProperty("user.dir")
					+ File.separator
					+ "config"
					+ File.separator + "db_copy.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
			return;
		}

		Connection sourceConn = null;
		Connection targetConn = null;
		try {
			String sourceDriver = configUtil.getSectionProperty("datasource",
					"db.copy.source.jdbc.driver");
			String sourceURL = configUtil.getSectionProperty("datasource",
					"db.copy.source.jdbc.url");
			String sourceUser = configUtil.getSectionProperty("datasource",
					"db.copy.source.jdbc.user");
			String sourcePass = configUtil.getSectionProperty("datasource",
					"db.copy.source.jdbc.pass");

			String targetDriver = configUtil.getSectionProperty("datasource",
					"db.copy.target.jdbc.driver");
			String targetURL = configUtil.getSectionProperty("datasource",
					"db.copy.target.jdbc.url");
			String targetUser = configUtil.getSectionProperty("datasource",
					"db.copy.target.jdbc.user");
			String targetPass = configUtil.getSectionProperty("datasource",
					"db.copy.target.jdbc.pass");

			try {
				String batch = configUtil.getSectionProperty("datasource",
						"db.copy.batch.count");
				if (StringUtils.isNotBlank(batch))
					tCmt = Integer.parseInt(batch);
			} catch (Exception e) {

			}

			try {
				String ignore = configUtil.getSectionProperty("datasource",
						"db.copy.error.ignore");
				if (StringUtils.isNotBlank(ignore))
					errIgnore = Boolean.parseBoolean(ignore);
			} catch (Exception e) {

			}

			try {
				String max = configUtil.getSectionProperty("datasource",
						"db.copy.max.rows");
				if (StringUtils.isNotBlank(max))
					maxRow = Integer.parseInt(max);
			} catch (Exception e) {

			}

			try {
				String create = configUtil.getSectionProperty("datasource",
						"db.copy.target.create");
				if (StringUtils.isNotBlank(create))
					createTable = Boolean.parseBoolean(create);
			} catch (Exception e) {

			}

			try {
				String data = configUtil.getSectionProperty("datasource",
						"db.copy.target.data");
				if (StringUtils.isNotBlank(data))
					copyData = Boolean.parseBoolean(data);
			} catch (Exception e) {

			}

			try {
				String foreignkey = configUtil.getSectionProperty("datasource",
						"db.copy.target.foreignkey");
				if (StringUtils.isNotBlank(foreignkey))
					createForeighKey = Boolean.parseBoolean(foreignkey);
			} catch (Exception e) {

			}

			Class.forName(sourceDriver);
			Class.forName(targetDriver);
			sourceConn = DriverManager.getConnection(sourceURL, sourceUser,
					sourcePass);
			targetConn = DriverManager.getConnection(targetURL, targetUser,
					targetPass);

			String tables = configUtil.getSectionProperty("copy", "tables");
			if (!StringUtils.isNotBlank(tables)) {
				return;
			}

			String[] ts = tables.split(",");
			List<TableBean> tableList = null;
			if (createTable) {
				tableList = createTables(ts, sourceConn, targetConn, sourceURL,
						targetURL);
			} else {
				tableList = new LinkedList<TableBean>();
				for (String t : ts) {
					try {
						TableBean bean = getTargetTableinfo(t, targetURL,
								targetConn);
						tableList.add(bean);
					} catch (Exception e) {
						logger.error("get table " + t + " info failed:"
								+ e.getMessage());
					}
				}
			}

			if (createForeighKey) {
				createTables4Foreighkey(ts, sourceConn, targetConn, sourceURL,
						targetURL);
			}

			if (copyData) {
				transferFch(tableList, sourceConn, targetConn, sourceURL,
						targetURL, sourceUser, targetUser);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e);
			return;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e);
			return;
		} finally {
			try {
				sourceConn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
			try {
				targetConn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
		}

	}

	private boolean transferFch(List<TableBean> tables, Connection sourceConn,
			Connection targetConn, String sourceURL, String targetURL,
			String sourceSchema, String targetSchema) {
		StringBuffer sql = null;

		try {
			targetConn.setAutoCommit(false);
			for (TableBean table : tables) {
				Map<String, String> map = new HashMap<String, String>();
				PreparedStatement pt = null;
				PreparedStatement ps = null;
				PreparedStatement pt1 = null;
				PreparedStatement ps1 = null;
				ResultSet rs = null;
				ResultSet rt = null;
				ResultSet rc = null;
				try {
//					182D090QJ1707F0000012378E6F92B65
//					182D091BR18G7F000001237877B40A65
					String sourceTable = table.getSourcetableName();
					String targetTable = table.getTargetTableName();
					logger.info("start transfer table:" + sourceTable);
//					String ssql = "select * from " + sourceTable+" where poid='182D091BR18G7F000001237877B40A65'";
					String ssql = "select * from " + sourceTable;
					String tsql = "insert into " + targetTable + "(";
					String checktsql = "select * from " + targetTable
							+ " where 1=2";
					String countsql = "select count(*) from " + sourceTable;

					int tcount = 0;
					long scount = 0;
					// check target table

					// 太大笔数不导入
					pt1 = sourceConn.prepareStatement(countsql);
					rc = pt1.executeQuery();
					if (rc.next())
						scount = rc.getLong(1);
					if (scount >= maxRow) {
						logger.error("ignore table:" + sourceTable + " rows:"
								+ scount + " maxceed max rows:" + maxRow);
						continue;
					}

					pt = targetConn.prepareStatement(checktsql);
					try {
						rt = pt.executeQuery();
						tcount = rt.getMetaData().getColumnCount();
					} catch (Exception e) {
						logger.error("Access Target ERROR! maybe target table not found:"
								+ sourceTable + "-" + targetTable);
						continue;
					}

					ps = sourceConn.prepareStatement(ssql);
					rs = ps.executeQuery();
					int count = rs.getMetaData().getColumnCount();

					if (count != tcount) {
						logger.error("Source and Target table columns mismatch:"
								+ sourceTable + "-" + targetTable);
						continue;
					}

					sql = new StringBuffer(tsql);
					String tStr = "";
					for (int i = 0; i < table.getFields().size(); i++) {
						tStr = table.getFields().get(i).getName();
						if (i < count - 1)
							sql.append(tStr).append(",");
						else
							sql.append(tStr).append(") values(");
					}
					for (int i = 0; i < count; i++) {
						if (i < count - 1)
							sql.append("?").append(",");
						else
							sql.append("?").append(") ");
					}
					ps1 = targetConn.prepareStatement(sql.toString());
					int recCnt = 1;
					while (rs.next()) {
						int index = 1;
//						 String id = rs.getString("id");
//						 if(StringUtils.isNotBlank(id)){
//						 if(!map.containsKey(id)){
//						 map.put(id, id);
//						 }
//						 else{
//						 continue;
//						 }
//						 }
//						 else{
//						 continue;
//						 }

						for (TableField field : table.getFields()) {
							String column = field.getName();
//							Object obj = null;
							if (field.getType() == FieldType.BLOB) {
								byte[] objs = rs.getBytes(column);
								if (objs== null)
									objs = new byte[0];
								ps1.setObject(index, objs);
							} else if (field.getType() == FieldType.CHAR) {
								
								String str = rs.getString(column);
								if (StringUtils.isBlank(str)) {
									if (field.isNotNull()) {
										str = " ";
									} else {
										str = null;
									}
								} 
								ps1.setString(index, str);
							} else if (field.getType() == FieldType.BOOLEAN) {
								Boolean obj = rs.getBoolean(column);
								if (obj == null)
									obj = false;
								ps1.setBoolean(index, obj);
							} else if (field.getType() == FieldType.VARCHAR) {
								String str = rs.getString(column);
								if (StringUtils.isBlank(str)) {
									if (field.isNotNull()) {
										str = " ";
									} else {
										str = null;
									}
								} 
								ps1.setString(index, str);
							} else if (field.getType() == FieldType.CLOB) {
								
								String str = rs.getString(column);
								if (StringUtils.isBlank(str)) {
									if (field.isNotNull()) {
										str = " ";
									} else {
										str = null;
									}
								}
								if(str != null){
//								ByteArrayInputStream input = new ByteArrayInputStream(str.getBytes());
//								InputStreamReader isr = new InputStreamReader(input);
//								BufferedReader bf = new BufferedReader(isr);
									StringReader reader = new StringReader(str);
								ps1.setClob(index, reader,str.length());
								}
								else{
									ps1.setString(index,str);
								}
							} else if (field.getType() == FieldType.DATETIME) {
								Timestamp tt = rs.getTimestamp(column);
								ps1.setTimestamp(index, tt);
							} else if (field.getType() == FieldType.DATE) {
								Date date = rs.getDate(column);
								ps1.setDate(index, date);
							} else if (field.getType() == FieldType.TIME) {
								Time time = rs.getTime(column);
								ps1.setTime(index, time);
							} else if (field.getType() == FieldType.INT) {
								int numb = rs.getInt(column);
								ps1.setInt(index, numb);
							} else if (field.getType() == FieldType.LONG) {
								long obj = rs.getLong(column);
								ps1.setLong(index, obj);
							} else if (field.getType() == FieldType.NUMERIC) {
								float f = rs.getFloat(column);
								ps1.setFloat(index, f);
							} else {
								throw new Exception("unknown column type:"
										+ field.getType());
							}
							
							
							index++;
						}
						
						ps1.addBatch();
						if (recCnt == tCmt) {
							ps1.executeBatch();
							recCnt = 0;
						}
						recCnt++;
					}
					ps1.executeBatch();
					targetConn.commit();
					logger.info("insert table: " + table + " count: " + scount
							+ " successfully");
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("insert table: " + table + " failed:"
							+ e.getMessage());
					try {
						targetConn.rollback();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (!errIgnore) {
						throw e;
					}
				} finally {
					SQLUtils.closeQuetely(pt);
					SQLUtils.closeQuetely(ps);
					SQLUtils.closeQuetely(pt1);
					SQLUtils.closeQuetely(ps1);
					SQLUtils.closeQuetely(rs);
					SQLUtils.closeQuetely(rt);
					SQLUtils.closeQuetely(rc);
				}

			} // for(table:)

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				targetConn.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
		}

		return true;
	}

}
