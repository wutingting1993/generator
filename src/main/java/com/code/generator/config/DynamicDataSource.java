package com.code.generator.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.code.generator.util.SpringContextUtil;

/**
 * Created by WuTing on 2017/12/5.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	public DynamicDataSource(String defaultDataSourceName) {
		Map<Object, Object> dataSourcs = SpringContextUtil.getBeans(DataSource.class, DynamicDataSource.class);
		List dataSourceIds = dataSourcs.keySet().stream().collect(Collectors.toList());
		DataSourceContextHolder.dataSourceIds.addAll(dataSourceIds);
		this.setTargetDataSources(dataSourcs);
		this.setDefaultTargetDataSource(dataSourcs.get(defaultDataSourceName));
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceContextHolder.getDataSourceType();
	}

}
