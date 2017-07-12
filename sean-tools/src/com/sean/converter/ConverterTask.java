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
 * @Title: ConterterTask.java 
 * @Package com.sean.converter 
 * @Description: TODO
 * @author seanjian   
 * @date Oct 24, 2016 7:41:28 PM 
 * @version V1.0   
 */
package com.sean.converter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sean.tool.util.FileCharsetDetector;

/**
 * @ClassName: ConterterTask
 * @Description: TODO
 * @date Oct 24, 2016 7:41:28 PM
 * 
 */
public class ConverterTask {

	private Logger log = Logger.getLogger(ConverterTask.class);

	private List<IFileFilter> filters = new LinkedList<IFileFilter>();

	private FileFilter fileFilter = new FileFilter();

	public void convert() {
		String source = Config.getConfig().getConverterSourcePath();
		String target = Config.getConfig().getConverterTargetPath();
		String charset = Config.getConfig().getConverterTargetCharset();

		if (!StringUtils.isNotBlank(charset)) {
			log.error("converter charset is empty");
			return;
		}

		File sourcef = new File(source);
		File targetf = new File(target);
		if (!sourcef.exists()) {
			log.error("file path [" + source + "] is not existed");
			return;
		}

		if (targetf.exists()) {
			File[] files = targetf.listFiles(fileFilter);
			if (files != null && files.length > 0) {
				log.error("file path [" + target + "] is  existed");
				return;
			}
		} else {

			if (!targetf.mkdirs()) {
				log.error("create target directory path [" + target
						+ "] failed");
				return;
			}
		}
		File[] found = sourcef.listFiles(fileFilter);
		if (found != null) {
			for (int i = 0; i < found.length; i++) {
				if (found[i].isDirectory()) {
					recureConverterFile(found[i], targetf);
				} else {
					converterFile(found[i], targetf);
				}
			}
		}
	}

	private void recureConverterFile(File source, File target) {
		String name = source.getName();
		String subPath = target.getPath() + File.separator + name;
		File subDir = new File(subPath);
		if (!subDir.exists()) {
			subDir.mkdir();
		}
		File[] found = source.listFiles(fileFilter);
		if (found != null) {
			for (int i = 0; i < found.length; i++) {
				if (found[i].isDirectory()) {
					recureConverterFile(found[i], subDir);
				} else {
					converterFile(found[i], subDir);
				}
			}
		}
	}

	private void converterFile(File source, File targetDir) {
		String name = source.getName();
		String suffix = "";
		if (name.indexOf(".") > 0) {
			suffix = name.substring(name.lastIndexOf(".") + 1);
		}
		AbstractConverter converter = null;
		try {
			converter = ConverterFactory.getConverter(suffix);
		} catch (ConverterException e) {
			log.error(e.getMessage());
		}

		String targetPath = targetDir.getPath();
		File targetFile = new File(targetPath + File.separator + name);
		log.info("start converter file[" + source.getPath() + "] to ["
				+ targetPath + File.separator + name + "]");
		if (targetFile.exists()) {
			log.error("create target file [" + targetPath + File.separator
					+ name + "] failed");
			return;
		}

		String charset = null;
		try {
			charset = FileCharsetDetector.getCharsetDetector()
					.guessFileEncoding(source);
			if (!StringUtils.isNotBlank(charset)) {
				charset = "utf8";
			} else {
				if (charset.indexOf(",") > 0) {
					charset = charset.substring(0, charset.indexOf(","));
				}
			}
			// InputStream in= new java.io.FileInputStream(source);
			// byte[] b = new byte[3];
			// in.read(b);
			// in.close();
			// if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
			// // System.out.println(file.getName() + "：编码为UTF-8");
			// charset = "utf8";
			//
			// }
			// else {
			// charset = "gbk";
			// }
			// // System.out.println(file.getName() + "：可能是GBK，也可能是其他编码");
			if ("jsp".equals(suffix) || "jspf".equals(suffix)) {
				charset = "GBK";
			}

			if ("js".equals(suffix) || "html".equals(suffix) || "template".equals(suffix)) {
				charset = "GBK";
			}
			List<String> sourceLines = FileUtils.readLines(source, charset);
			if (sourceLines != null) {

				Collection<String> lines = null;
				try {

					lines = converter.perform(sourceLines);
				} catch (ConverterException e) {
					// TODO Auto-generated catch block
					log.error("tranfer file[" + source.getPath() + " charset "
							+ charset + "] use charset ["
							+ Config.getConfig().getConverterTargetCharset()
							+ "] to [" + targetPath + File.separator + name
							+ "]failed:" + e.getMessage());
				}

				FileUtils.writeLines(targetFile, Config.getConfig()
						.getConverterTargetCharset(), lines);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			log.error("write to file[" + targetPath + File.separator + name
					+ " charset " + charset + "]  use charset ["
					+ Config.getConfig().getConverterTargetCharset()
					+ "] failed:" + e.getMessage());
		}

		log.info("converter file[" + source.getPath() + "] to [" + targetPath
				+ File.separator + name + "] use charset ["
				+ Config.getConfig().getConverterTargetCharset()
				+ "] successfully");
	}

	class FileFilter implements FilenameFilter {
		@Override
		public boolean accept(File arg0, String arg1) {

			if (arg0.isDirectory()) {
				String name = arg1;
				String suffix = "";
				File sub = new File(arg0.getPath() + File.separator + arg1);
				if (sub.isFile()) {
					if (name.indexOf(".") > 0) {
						suffix = name.substring(name.lastIndexOf(".") + 1);
					}
					if (Config.getConfig().getConverterFilters()
							.indexOf(suffix) >= 0)
						return true;
					else
						return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}
}
