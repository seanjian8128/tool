package com.sean.db.common;


import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:shiny_vc@163.com">陈云亮</a> <br/>
 * @since 5.0.0 <br/>
 */
public class TableFk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7976120342768910834L;

	private String name;

	private String action;

	private String references;

	private List<TableFkField> fields;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the references
	 */
	public String getReferences() {
		return references;
	}

	/**
	 * @param references
	 *            the references to set
	 */
	public void setReferences(String references) {
		this.references = references;
	}

	/**
	 * @return the fields
	 */
	public List<TableFkField> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(List<TableFkField> fields) {
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "foreign-key:" + name;
	}

}
