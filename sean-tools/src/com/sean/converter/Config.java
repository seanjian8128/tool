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
package com.sean.converter;

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

	private List<String> converterFilters = new ArrayList<String>();

	private String converterTargetPath = null;

	private String converterSourcePath = null;

	private String converterTargetCharset = null;

	private Config() {
		ConfigUtil configUtil = new ConfigUtil();
		try {
			configUtil.load(new FileInputStream(new File(
					"./config/converter.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("file [./config/converter.properties] not found");
		}
		converterSourcePath = configUtil.getSectionProperty("converter",
				"converter.source");
		converterTargetPath = configUtil.getSectionProperty("converter",
				"converter.target");
		converterTargetCharset = configUtil.getSectionProperty("converter",
				"converter.target.charset");
		String filter = configUtil.getSectionProperty("converter",
				"converter.source.filter");

		String[] filters = filter.split(",");
		for (String f : filters) {
			converterFilters.add(f);
		}
	}

	public static Config getConfig() {
		return config;
	}

	public List<String> getConverterFilters() {
		return converterFilters;
	}

	public String getConverterTargetPath() {
		return converterTargetPath;
	}

	public String getConverterSourcePath() {
		return converterSourcePath;
	}

	public String getConverterTargetCharset() {
		return converterTargetCharset;
	}

}
