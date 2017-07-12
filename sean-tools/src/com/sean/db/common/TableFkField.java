package com.sean.db.common;


import java.io.Serializable;

public class TableFkField implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6244652862102948812L;

	private String from;

	private String to;

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to
	 *            the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "from-to:" + from + ":" + to;
	}

}
