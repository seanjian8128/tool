package com.sean.db.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class GrepErrTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GrepErrTable err = new GrepErrTable();
		
		//新建表时抓取错误信息
//		err.grepErrTable();
//		err.grepCreateTable();
		
		//写入数据时的错误信息
//		err.grepInsertTable();
		
		//写入新建外键时的错误信息
		err.grepErrCreateForeighkeyTable();
	}
	
	public void grepErrCreateForeighkeyTable(){
		try {
			List<String> foreighErr = new LinkedList<String>();
			List<String> foreighErrSql = new LinkedList<String>();
			List<String> source = FileUtils.readLines(new File("./log/copy.log"));
			Map<String, String> map = new HashMap<String, String>();
			for(int i = 0; i < source.size(); i++){
				String line = source.get(i);
				if(line.trim().indexOf("[ERROR]") == 0){
					foreighErr.add(source.get(i-1));
					foreighErr.add(source.get(i));
					foreighErr.add(source.get(i+1));
					foreighErrSql.add(source.get(i - 1));
				}
			}
			
			FileUtils.writeLines(new File("./log/foreighkeyErr.log"), foreighErr);
			FileUtils.writeLines(new File("./log/foreighkeyErrTable.log"), foreighErrSql);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void grepInsertTable(){
		try {
			List<String> insertErr = new LinkedList<String>();
			List<String> insertErrTable = new LinkedList<String>();
			List<String> source = FileUtils.readLines(new File("./log/copy.log"));
			Map<String, String> map = new HashMap<String, String>();
			for(int i = 0; i < source.size(); i++){
				String line = source.get(i);
				if(line.trim().indexOf("[ERROR]") == 0){
					insertErr.add(source.get(i-1));
					insertErr.add(source.get(i));
					insertErr.add(source.get(i+1));
					String[] ss = source.get(i-1).split(" ");
					String t = ss[2];
					insertErrTable.add(StringUtils.substringAfter(t, ":")+",\\");
				}
			}
			
			FileUtils.writeLines(new File("./log/insertErr.log"), insertErr);
			FileUtils.writeLines(new File("./log/insertErrTable.log"), insertErrTable);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void grepCreateTable(){
		try {
			List<String> filtersTable = new LinkedList<String>();
			List<String> target = FileUtils.readLines(new File("./log/exportTable.txt"));
			List<String> source = FileUtils.readLines(new File("./log/errCreateTablename.log"));
			Map<String, String> map = new HashMap<String, String>();
			for(int i = 0; i < source.size(); i++){
				String line = source.get(i);
				map.put(line, line);
			}
			
			for(int i = 0; i < target.size(); i++){
				String line = target.get(i);
				if(!map.containsKey(line)){
					filtersTable.add(line);
				}
			}
			
			FileUtils.writeLines(new File("./log/hasCreate.log"), filtersTable);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void grepErrTable(){
		try {
			List<String> errTables = new LinkedList<String>();
			List<String> errTablenames = new LinkedList<String>();
			List<String> errTablesql = new LinkedList<String>();
			List<String> contents = FileUtils.readLines(new File("./log/copy.log"));
			for(int i = 0; i < contents.size(); i++){
				String line = contents.get(i);
				if(line.trim().indexOf("[ERROR]") == 0){
					errTables.add(contents.get(i-1));
					errTables.add(contents.get(i));
					errTables.add(contents.get(i+1));
					String[] ss = contents.get(i+1).split(" ");
					String t = ss[2];
					errTablenames.add(t);
					errTablesql.add(contents.get(i-1));
					
				}
			}
			
			FileUtils.writeLines(new File("./log/errCreateTable.log"), errTables);
			FileUtils.writeLines(new File("./log/errCreateTablename.log"), errTablenames);
			FileUtils.writeLines(new File("./log/errCreateTablesql.log"), errTablesql);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
