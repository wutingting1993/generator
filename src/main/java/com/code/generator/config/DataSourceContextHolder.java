package com.code.generator.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WuTing on 2017/12/5.
 */
public class DataSourceContextHolder {
	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
	public static List<String> dataSourceIds = new ArrayList<>();

	public static void setDataSourceType(String dataSourceType) {
		contextHolder.set(dataSourceType);
	}

	public static String getDataSourceType() {
		return contextHolder.get();
	}

	public static void clearDataSourceType() {
		contextHolder.remove();
	}

	public static boolean containsDataSource(String dataSourceId) {
		return dataSourceIds.contains(dataSourceId);
	}
}
