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
* @Title: Converter.java 
* @Package com.sean.converter 
* @Description: TODO
* @author seanjian   
* @date Oct 24, 2016 7:39:06 PM 
* @version V1.0   
*/
package com.sean.replace;

import org.apache.log4j.PropertyConfigurator;

/** 
 * @ClassName: Converter 
 * @Description: TODO
 * @date Oct 24, 2016 7:39:06 PM 
 *  
 */
public class Converter {

	/** 
	 * @Title: main 
	 * @Description: TODO
	 * @param @param args     
	 * @return void  
	 * @throws 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure ( "config/log4j.properties" ) ;
        ConverterTask task = new ConverterTask();
        task.convert();
		
	}

}


