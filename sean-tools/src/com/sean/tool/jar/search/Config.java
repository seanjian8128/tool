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
 * @Title: Config.java 
 * @Package com.sean.converter 
 * @Description: TODO
 * @author seanjian   
 * @date Oct 24, 2016 8:38:12 PM 
 * @version V1.0   
 */
package com.sean.tool.jar.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sean.tool.util.ConfigUtil;

/**
 * @ClassName: Config
 * @Description: TODO
 * @date Oct 24, 2016 8:38:12 PM
 * 
 */
public class Config {

	public static Config config = new Config();

	private Logger log = Logger.getLogger(Config.class);

	private String path = null;

	private Config() {
		ConfigUtil configUtil = new ConfigUtil();
		try {
			configUtil.load(new FileInputStream(new File(
					"./config/jar_search.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("file [./config/jar_search.properties] not found");
		}
		path = configUtil.getSectionProperty("search",
				"path");
	}

	public static Config getConfig() {
		return config;
	}

	public String getPath() {
		return path;
	}


}
