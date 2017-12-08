package com.code.generator.vo;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by WuTing on 2017/12/6.
 */
@Getter
@Setter
public class TableInfo {
	private String tableCatalog;
	private String tableSchema;
	private String tableName;
	private String tableType;
	private String engine;
	private String version;
	private String rowFormat;
	private String tableComment;
	private Map<String, String> columns;
}
