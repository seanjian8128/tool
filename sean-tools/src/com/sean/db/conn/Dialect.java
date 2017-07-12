package com.sean.db.conn;

import com.sean.db.common.FieldType;

public interface Dialect {

	public FieldType exchangeSqlType(String sqlTypeName);
}
