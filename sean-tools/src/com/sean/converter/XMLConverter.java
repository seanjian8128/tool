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
* @Title: XMLConverter.java 
* @Package com.sean.converter 
* @Description: TODO
* @author seanjian   
* @date Oct 24, 2016 10:16:50 PM 
* @version V1.0   
*/
package com.sean.converter;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;


/** 
 * @ClassName: XMLConverter 
 * @Description: TODO
 * @date Oct 24, 2016 10:16:50 PM 
 *  
 */
public class XMLConverter implements AbstractConverter {

	@Override
	public Collection<String> perform(Collection<String> lines) throws ConverterException {
		Collection<String> tranLines = new LinkedList<String>();
		if (lines != null) {

			for (String line : lines) {
				try {
					String tranLine = new String(line.getBytes(), Config
							.getConfig().getConverterTargetCharset());
					tranLine = tranLine.replaceAll("\\b[Gg][Bb][Kk]\\b", Config
							.getConfig().getConverterTargetCharset());
					tranLine = tranLine.replaceAll("\\b[Gg][Bb]2312\\b", Config
							.getConfig().getConverterTargetCharset());
					tranLine = tranLine.replaceAll("\\b[Gg][Bb]18030\\b", Config
							.getConfig().getConverterTargetCharset());
					tranLines.add(tranLine);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					throw new ConverterException("transfer [" + line + "] to ["
							+ Config.getConfig().getConverterTargetCharset()
							+ "] failed:" + e.getMessage());
				}
			}

		}
		return tranLines;
	}

	
}


