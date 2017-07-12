package com.sean.db.conn;

import com.sean.db.common.FieldType;

public class Db2Dialect implements Dialect {

	@Override
	public FieldType exchangeSqlType(String sqlTypeName) {
		// TODO Auto-generated method stub
		if("decfloat".equalsIgnoreCase(sqlTypeName))
			return FieldType.NUMERIC;
		if("VARCHAR FOR BIT DATA".equals(sqlTypeName)){
			return FieldType.BLOB;
		}
		return null;
	}

}
