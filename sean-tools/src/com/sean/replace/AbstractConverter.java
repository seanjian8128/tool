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
* @Title: AbstractConverterTask.java 
* @Package com.sean.converter 
* @Description: TODO
* @author seanjian   
* @date Oct 24, 2016 9:42:34 PM 
* @version V1.0   
*/
package com.sean.replace;

import java.util.Collection;

/** 
 * @ClassName: AbstractConverterTask 
 * @Description: TODO
 * @date Oct 24, 2016 9:42:34 PM 
 *  
 */
public interface AbstractConverter {
	
	public Collection<String> perform(Collection<String> lines)  throws ConverterException;

}


