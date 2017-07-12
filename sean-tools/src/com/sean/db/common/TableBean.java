package com.sean.db.common;



import java.io.Serializable;
import java.util.List;

public class TableBean  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2864336272611289633L;


	private String sourcetableName = null;// 表名
	
	private String targetTableName = null;
	
	//当前要处理的表
	private String currTableName = null;

	private boolean autoKey;// 键值自动生成

	private String dbId;// 所对应的数据源id

	private String view;// 视图

	private List<TableField> fields;


	private List<String> key;

	private List<TableIndex> indexes;

	private List<TableFk> foreignKeys;

	/**
	 * @return
	 */
	public String getDbId() {
		return dbId;
	}

	/**
	 * @param dbId
	 */
	public void setDbId(String dbId) {
		this.dbId = dbId;
	}

	/**
	 * @return the view
	 */
	public String getView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(String view) {
		this.view = view;
	}

	/**
	 * @return the fields
	 */
	public List<TableField> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(List<TableField> fields) {
		this.fields = fields;
	}

	/**
	 * @return the key
	 */
	public List<String> getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(List<String> key) {
		this.key = key;
	}

	/**
	 * @return the indexes
	 */
	public List<TableIndex> getIndexes() {
		return indexes;
	}

	/**
	 * @param indexes
	 *            the indexes to set
	 */
	public void setIndexes(List<TableIndex> indexes) {
		this.indexes = indexes;
	}

	/**
	 * @return the foreignKeys
	 */
	public List<TableFk> getForeignKeys() {
		return foreignKeys;
	}

	/**
	 * @param foreignKeys
	 *            the foreignKeys to set
	 */
	public void setForeignKeys(List<TableFk> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}
	
//	@Override
//	public String toString() {
//		return tableName;
//	}

	@Override
	public String toString() {
		return sourcetableName+"-to-"+targetTableName;
	}

	

	public String getSourcetableName() {
		return sourcetableName;
	}

	public void setSourcetableName(String sourcetableName) {
		this.sourcetableName = sourcetableName;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}
	
	

	public boolean isAutoKey() {
		return autoKey;
	}

	

	public String getCurrTableName() {
		return currTableName;
	}

	public void setCurrTableName(String currTableName) {
		this.currTableName = currTableName;
	}

	public void setAutoKey(boolean autoKey) {
		this.autoKey = autoKey;
	}

}
