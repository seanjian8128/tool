package com.sean.tool.jar.search;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.PropertyConfigurator;

public class JarSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure ( "config/log4j.properties" ) ;
		try {
			Collection<File> files= FileUtils.listFiles(new File(Config.getConfig().getPath()),new IOFileFilter() {
				
				@Override
				public boolean accept(File paramFile, String paramString) {
					// TODO Auto-generated method stub
					return true;
				}
				
				@Override
				public boolean accept(File paramFile) {
					// TODO Auto-generated method stub
					return true;
				}
			},null);
			Iterator<File> itr = files.iterator();
			for(;itr.hasNext();){
			  File file = 	itr.next();
			  ZipFile f = new ZipFile(file);
			  Enumeration<? extends ZipEntry> e = f.entries();
			  for(;e.hasMoreElements();){
				ZipEntry entry =   e.nextElement();
				String name = entry.getName();
				if(name.toLowerCase().indexOf("workflow-sqlmap") >= 0){
				System.out.println(f.getName());
//				 StringBuilder content = new StringBuilder();  
				Scanner scanner = new Scanner(f.getInputStream(entry));  
	            while (scanner.hasNextLine()) {  
	                String line = scanner.nextLine();
	            	if( line.toLowerCase().indexOf("findBizcategoryByBizcode") >= 0)
	            		System.out.println(line);
//	                content.append(line).append("\r\n");  
	            }  
	            scanner.close();  
				}
			  }
			}
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
