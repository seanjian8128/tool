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
* @Title: CharsetChangeTest.java 
* @Package com.sean.tool.util 
* @Description: TODO
* @author seanjian   
* @date Oct 25, 2016 3:31:41 PM 
* @version V1.0   
*/
package com.sean.tool.util;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

/** 
 * @ClassName: CharsetChangeTest 
 * @Description: TODO
 * @date Oct 25, 2016 3:31:41 PM 
 *  
 */
public class CharsetChangeTest {

	/** 
	 * @Title: main 
	 * @Description: TODO
	 * @param @param args     
	 * @return void  
	 * @throws 
	 */
	public static void main(String[] argv) throws Exception {
        File file1 = new File("F:\\乌鲁木齐国际结算系统\\tfb_src\\tfb-web\\src\\main\\resources\\config\\gateway\\KNLRespCode.properties");
        
        System.out.println("文件编码:" + FileCharsetDetector.getCharsetDetector().guessFileEncoding(file1));
        
        List<String> sourceLines = FileUtils.readLines(file1, "UTF8");
        FileUtils.writeLines(new File("e:\\c.properties"),"UTF8", sourceLines,"");
    }

}


