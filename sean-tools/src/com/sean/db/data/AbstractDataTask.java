package com.sean.db.data;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sean.db.common.TableBean;
import com.sean.db.table.TableTask;

public abstract class AbstractDataTask {

	private Logger logger = null;
	
	private Class logKey = null;
	
	
	public AbstractDataTask(Class logKey) {
		this.logKey = logKey;
		logger = Logger.getLogger(logKey);
	}

	protected String[] getTables(String table){
		String[] ts = new String[2];
		table = table.trim();
		TableBean bean = null;
		String targetTable = table;
		String sourceTable = table;
		if(table.indexOf(":") > 0){
			sourceTable = table.substring(0,table.indexOf(":"));
			targetTable = table.substring(table.indexOf(":")+1,table.length());
		}
		ts[0] = sourceTable;
		ts[1] = targetTable;
		return ts;
	}
	
	
	protected List<TableBean> createTables(String[] ts, Connection sourceConn,
			Connection targetConn, String sourceURL, String targetURL) {
		List<TableBean> tables = new LinkedList<TableBean>();
		for (String table : ts) {
			try {
				
				TableBean bean = null;
				TableTask tableTask = new TableTask(logKey);
				int rtn = tableTask.checkTable(targetConn.getMetaData(), table);
				if (rtn == -1) {
					bean = createTable(table,sourceConn, targetConn, sourceURL,
							targetURL, TableTask.CREATE_TABLE_ONLY);
					tables.add(bean);
				}
			} catch (Exception e) {
                logger.error("create table "+table+" failed:"+e.getMessage());
                e.printStackTrace();
			}

		}
		return tables;
	}

	protected List<TableBean> createTables4Foreighkey(String[] ts,
			Connection sourceConn, Connection targetConn, String sourceURL,
			String targetURL) {
		List<TableBean> tables = new LinkedList<TableBean>();
		for (String table : ts) {
			try {
				TableBean bean = null;
				TableTask tableTask = new TableTask(logKey);
				int rtn = tableTask.checkTable(targetConn.getMetaData(), table);
				if (rtn != -1) {
					bean = createTable(table,sourceConn, targetConn, sourceURL,
							targetURL, TableTask.CREATE_FOREIGNKEY_ONLY);
					tables.add(bean);
				}
				
			} catch (Exception e) {
				logger.error("create table foreignkey "+table+" failed:"+e.getMessage());
				e.printStackTrace();
			}

		}
		return tables;
	}

	
	protected TableBean getTargetTableinfo(String table, String jdbcUrl,Connection conn) throws Exception {
		String[] t = getTables(table);
		String sourceTable = t[0];
		String targetTable = t[1];
		TableTask tableTask = new TableTask(logKey);
		TableBean bean = tableTask.getTableInfo(conn,jdbcUrl, targetTable);
		bean.setTargetTableName(targetTable);
		bean.setSourcetableName(sourceTable);
		return bean;
	}
	
	
	private TableBean createTable(String table,Connection sourceConn,
			Connection targetConn, String sourceURL, String targetURL,
			int createType) throws Exception {
		TableTask tableTask = new TableTask(logKey);
		String[] t = getTables(table);
		String sourceTable = t[0];
		String targetTable = t[1];
		TableBean bean = tableTask.getTableInfo(sourceConn,sourceURL, sourceTable);
		bean.setTargetTableName(targetTable);
		bean.setSourcetableName(sourceTable);
		bean.setCurrTableName(targetTable);
		tableTask.createTable(targetURL, targetConn, bean, createType);
		bean = tableTask.getTableInfo(targetConn,targetURL, targetTable);
		bean.setTargetTableName(targetTable);
		bean.setSourcetableName(sourceTable);
		return bean;
	}

}
