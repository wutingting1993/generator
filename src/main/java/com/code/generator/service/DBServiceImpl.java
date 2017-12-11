package com.code.generator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.code.generator.config.DataSource;
import com.code.generator.config.Schema;
import com.code.generator.config.SpringContextUtil;
import com.code.generator.util.PropertyUtil;
import com.code.generator.util.StringUtil;
import com.code.generator.vo.GeneratorConfig;
import com.code.generator.vo.TableInfo;

/**
 * Created by WuTing on 2017/12/11.
 */
@Service
public class DBServiceImpl implements DBService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static Map<String, GeneratorConfig> configs;

	private static List<String> schemas;

	static {
		initConfigs();
	}

	@DataSource("schemaDataSource")
	@Override
	public List<TableInfo> getTableInfos(List<String> schema) {

		String sql = "select * from information_schema.tables where table_schema in (" + this.getSchemas(schema) + ")";
		List<TableInfo> tables = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TableInfo.class));

		Map<String, List<TableInfo>> map = tables.stream()
			.filter(tableInfo -> {
				GeneratorConfig config = configs.get(tableInfo.getTableSchema().toLowerCase());
				String tableName = tableInfo.getTableName().toLowerCase();
				return !config.getExcludeTables().contains(tableName);
			}).collect(Collectors.groupingBy(TableInfo::getTableSchema));

		tables.clear();
		map.forEach((schemaName, tableInfos) -> {
			tableInfos.forEach(
				table -> {
					Map<String, String> columns = this.getService().getTablesColumns(table.getTableSchema(), table.getTableName());
					if (MapUtils.isNotEmpty(columns)){
						table.setColumns(columns);
						tables.add(table);
					}
				});
		});

		return tables;
	}

	@DataSource
	public Map<String, String> getTablesColumns(@Schema String schema, String tableName) {
		tableName = schema + "." + tableName;
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " limit 0");
		SqlRowSetMetaData metaData = rowSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, String> columns = new HashMap<>();
		if (columnCount > 0) {
			for (int i = 1; i <= columnCount; i++) {
				String columnClassName = metaData.getColumnClassName(i);
				columns.put(metaData.getColumnName(i), columnClassName.substring(columnClassName.lastIndexOf(".") +
					1));
			}
		}
		return columns;
	}

	private String getSchemas(List<String> schema) {
		if (CollectionUtils.isEmpty(schema)) {
			schema = this.schemas;

		}
		if (CollectionUtils.isEmpty(schema)) {
			throw new RuntimeException("schema不能为空");
		}
		return "'" + schema.stream().collect(Collectors.joining("','")) + "'";
	}

	private static void initConfigs() {
		Properties properties = PropertyUtil.props;
		configs = new HashMap<>();
		schemas = new ArrayList<>();
		properties.forEach((key, value) -> {
			String name = key.toString();
			if (name.startsWith("code.generator.schema")) {
				name = name.replace("code.generator.schema.", "");
				String substring = name.substring(0, name.indexOf("."));
				schemas.add(substring);
				String schema = substring.toLowerCase();
				if (configs.containsKey(schema)) {
					String field = name.substring(name.lastIndexOf(".") + 1);
					switch (field) {
						case "excludeTables":
							configs.get(schema).setExcludeTables(StringUtil.stringToList(value));
							break;
						case "includeTables":
							configs.get(schema).setIncludeTables(StringUtil.stringToList(value));
							break;
					}

				} else {
					GeneratorConfig config = new GeneratorConfig();
					config.setSchema(schema);
					configs.put(schema, config);
				}
			}

		});
	}

	private DBService getService() {
		return SpringContextUtil.getBean(DBService.class);
	}
}
