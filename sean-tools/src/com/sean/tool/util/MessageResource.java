/*
 *
 * Copyright 2011 by HyLandTec Corporation.
 * GuanYinShan PEAK Building 12F, XiaMen, FuJian, PRC 361005
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * HyLandTec Corporation ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with HyLandTec.
 *
 */
/**   
 * @Title: MessageFormat.java 
 * @Package com.sean.tool.util 
 * @Description: TODO
 * @author seanjian   
 * @date Oct 24, 2016 9:03:06 PM 
 * @version V1.0   
 */
package com.sean.tool.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @ClassName: MessageFormat
 * @Description: TODO
 * @date Oct 24, 2016 9:03:06 PM
 * 
 */
public class MessageResource {

	private ResourceBundle bundle = null;
	
	private Logger log = Logger.getLogger(MessageResource.class);

	public MessageResource(ResourceBundle bundle) {
		this.bundle = ResourceBundle.getBundle("message.properties");
	}

	public String getErr(String key, Object... params) {
		String msg = bundle.getString(key);
		if (msg == null)
			msg = "";
		msg = MessageFormat.format(msg, params);
		return msg;
	}

}
