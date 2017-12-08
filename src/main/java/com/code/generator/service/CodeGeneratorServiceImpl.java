package com.code.generator.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.code.generator.config.DataSource;
import com.code.generator.config.Schema;
import com.code.generator.util.SpringContextUtil;
import com.code.generator.util.PropertyUtil;
import com.code.generator.vo.FieldInfo;
import com.code.generator.vo.GeneratorConfig;
import com.code.generator.vo.ModelInfo;
import com.code.generator.vo.TableInfo;

/**
 * Created by WuTing on 2017/12/6.
 */
@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static Map<String, GeneratorConfig> configs;
	private static List<String> schemas;

	static {
		initConfigs();
	}

	@Override
	public void generatorModel() {
		configs.keySet();
		List<TableInfo> tables = getCodeGeneratorService().getTableInfos(schemas);

		List<ModelInfo> modelInfos = getModelInfos(tables);

		generatorModel(modelInfos);
	}

	private void generatorModel(List<ModelInfo> modelInfos) {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.setProperty("input.encoding", "utf-8");
		ve.setProperty("output.encoding", "utf-8");
		ve.init();
		Template template = ve.getTemplate("modelBO.vm");
		VelocityContext ctx = new VelocityContext();
		modelInfos.forEach(model -> {
			ctx.put("model", model);
			StringWriter sw = new StringWriter();
			template.merge(ctx, sw);
			System.out.println(sw.toString());
		});
	}

	public CodeGeneratorService getCodeGeneratorService() {
		return SpringContextUtil.getBean(CodeGeneratorService.class);
	}

	private List<ModelInfo> getModelInfos(List<TableInfo> tables) {
		List<ModelInfo> modelInfos = new ArrayList<>(tables.size());
		tables.stream().forEach(table -> {
			ModelInfo modelInfo = new ModelInfo();
			modelInfo.setBoPackageName("com.code.generator." + table.getTableSchema().toLowerCase().replace("_", ""));
			modelInfo.setDataImport(null);
			modelInfo.setModelBoName(firstCharacterToUpper(underlineToCamel(table.getTableName())));
			List<FieldInfo> rows = new ArrayList<>();
			table.getColumns().forEach((key, value) -> {
				switch (value.toLowerCase()) {
					case "timestamp":
						value = "Date";
						break;
					default:
						break;
				}
				FieldInfo feildInfo = new FieldInfo();
				feildInfo.setName(underlineToCamel(key));
				feildInfo.setType(value);
				rows.add(feildInfo);
			});
			modelInfo.setRowsData(rows);
			modelInfos.add(modelInfo);
		});

		return modelInfos;
	}

	@DataSource("schemaDataSource")
	@Override
	public List<TableInfo> getTableInfos(List<String> schema) {

		if (CollectionUtils.isEmpty(schema)) {
			throw new RuntimeException("schema不能为空");
		}
		String schemas = "'" + schema.stream().collect(Collectors.joining("','")) + "'";

		RowMapper<TableInfo> rm = BeanPropertyRowMapper.newInstance(TableInfo.class);
		String sql = "select * from information_schema.tables where table_schema in (" + schemas + ")";
		List<TableInfo> tables = jdbcTemplate.query(sql, rm);

		Map<String, List<TableInfo>> map = tables.stream()
			.filter(tableInfo -> {
				GeneratorConfig config = configs.get(tableInfo.getTableSchema().toLowerCase());
				String tableName = tableInfo.getTableName().toLowerCase();
				return !config.getExcludeTables().contains(tableName);
			}).collect(Collectors.groupingBy(TableInfo::getTableSchema));
		tables.clear();
		map.forEach((schemaName, value) -> {
			getCodeGeneratorService().getTablesBySchema(schemaName, value);
			tables.addAll(value);
		});
		return tables;
	}

	@DataSource
	@Override
	public void getTablesBySchema(@Schema String schema, List<TableInfo> tableInfos) {
		tableInfos.forEach(tableInfo -> {
			String tableName = schema + "." + tableInfo.getTableName();
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " limit 0");
			SqlRowSetMetaData metaData = rowSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			if (columnCount > 0) {
				Map<String, String> columns = new HashMap<>(columnCount);
				tableInfo.setColumns(columns);
				for (int i = 1; i <= columnCount; i++) {
					String columnClassName = metaData.getColumnClassName(i);
					columns.put(metaData.getColumnName(i),
						columnClassName.substring(columnClassName.lastIndexOf(".") + 1));
				}
			}
		});
	}

	public static String underlineToCamel(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		StringBuilder sb = new StringBuilder(param);
		Matcher mc = Pattern.compile("_+|\\s+").matcher(param);
		int i = 0;
		while (mc.find()) {
			int position = mc.end() - (i++);
			sb.setCharAt(position, Character.toUpperCase(sb.charAt(position)));
			sb.deleteCharAt(position - 1);
		}

		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}

	public static String firstCharacterToUpper(String name) {
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
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
							configs.get(schema).setExcludeTables(stringToList(value));
							break;
						case "includeTables":
							configs.get(schema).setIncludeTables(stringToList(value));
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

	private static List<String> stringToList(Object value) {
		return Arrays.stream(value.toString().split(","))
			.map(s -> s.toLowerCase())
			.collect(Collectors.toList());
	}
}
