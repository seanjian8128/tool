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
* @Title: IFileFilter.java 
* @Package com.sean.converter 
* @Description: TODO
* @author seanjian   
* @date Oct 24, 2016 7:41:51 PM 
* @version V1.0   
*/
package com.sean.converter;

import java.io.File;

/** 
 * @ClassName: IFileFilter 
 * @Description: TODO
 * @date Oct 24, 2016 7:41:51 PM 
 *  
 */
public interface IFileFilter {

	public boolean isFilter(File f);
}


