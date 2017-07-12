package com.sean.db.common;

public enum FieldType {

    /**
     * 字符
     */
    CHAR,

    /**
     * 布尔值
     */
    BOOLEAN,

    /**
     * 字符串
     */
    VARCHAR,

    /**
     * 字符大对象
     */
    CLOB,

    /**
     * 二进制大对象
     */
    BLOB,

    /**
     * 带时间的日期
     */
    DATETIME,

    /**
     * 日期
     */
    DATE,

    /**
     * 时间
     */
    TIME,

    /**
     * 整型,可定义长度
     */
    INT,

    /**
     * 长整型
     */
    LONG,
    
    /**
     * 带小数位的数字
     */
    NUMERIC,
    
    /**
     * JAVA对象
     */
    OBJECT,
    /**
     * 空的对象
     */
    NULL
}
