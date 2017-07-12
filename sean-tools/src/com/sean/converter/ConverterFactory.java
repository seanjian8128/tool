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
* @Title: ConverterFactory.java 
* @Package com.sean.converter 
* @Description: TODO
* @author seanjian   
* @date Oct 24, 2016 10:23:05 PM 
* @version V1.0   
*/
package com.sean.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/** 
 * @ClassName: ConverterFactory 
 * @Description: TODO
 * @date Oct 24, 2016 10:23:05 PM 
 *  
 */
public class ConverterFactory {

	private static Logger logger = Logger.getLogger(ConverterFactory.class);
	
	public final static String JSP = "jsp";
	
	public final static String JSPF = "jspf";
	
	public final static String JAVA = "java";
	
	public final static String PROPERTIES = "properties";

	public final static String PROP = "prop";
	
	public final static String XML = "xml";
	
	public final static String CSS = "css";
	
	public final static String HTML = "html";
	
	public final static String JAVASCRIPT = "js";
	
	public final static String TEMPLATE = "template";
	
	public static AbstractConverter getConverter(String filter) throws ConverterException{
		if(!StringUtils.isNotBlank(filter)){
			throw new ConverterException("filter is null");
		}
		if(JSP.equalsIgnoreCase(filter) || JSPF.equalsIgnoreCase(filter)){
			return new JSPConverter();
		}
		else if(JAVA.equalsIgnoreCase(filter)){
			return new JavaConverter();
		}
		else if(PROPERTIES.equalsIgnoreCase(filter) ||PROP.equalsIgnoreCase(filter) ||JAVASCRIPT.equalsIgnoreCase(filter) ||HTML.equalsIgnoreCase(filter)||TEMPLATE.equalsIgnoreCase(filter) ){
			return new PropertiesConverter();
		}
		else if(CSS.equalsIgnoreCase(filter)){
			return new CSSConverter();
		}
		else if(XML.equalsIgnoreCase(filter)){
			return new XMLConverter();
		}
		else{
			throw new ConverterException("unsupport converter type:"+filter);
		}
	}
 }


