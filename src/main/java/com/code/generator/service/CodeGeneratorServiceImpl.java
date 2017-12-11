package com.code.generator.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.code.generator.util.StringUtil;
import com.code.generator.vo.FieldInfo;
import com.code.generator.vo.ModelInfo;
import com.code.generator.vo.TableInfo;

/**
 * Created by WuTing on 2017/12/6.
 */
@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {
	@Autowired
	private DBService dbService;

	@Override
	public void generatorModel() {
		this.generatorModel(dbService.getTableInfos(null));
	}

	private void generatorModel(List<TableInfo> tables) {

		VelocityEngine ve = this.createVelocityEngine();
		Template template = ve.getTemplate("modelBO.vm");
		VelocityContext ctx = new VelocityContext();
		tables.stream().forEach(table -> {

			ModelInfo modelInfo = this.convertTable2Model(table);
			StringWriter sw = new StringWriter();
			ctx.put("model", modelInfo);
			template.merge(ctx, sw);

			System.out.println(sw.toString());
		});
	}

	private ModelInfo convertTable2Model(TableInfo table) {
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setBoPackageName("com.code.generator." + table.getTableSchema().toLowerCase().replace("_", ""));
		modelInfo.setDataImport(null);
		modelInfo.setModelBoName(StringUtil.firstCharToUpper(StringUtil.underlineToCamel(table.getTableName())));
		this.setRowsData(table, modelInfo);
		return modelInfo;
	}

	private void setRowsData(TableInfo table, ModelInfo modelInfo) {
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
			feildInfo.setName(StringUtil.underlineToCamel(key));
			feildInfo.setType(value);
			rows.add(feildInfo);
		});
		modelInfo.setRowsData(rows);
	}

	private VelocityEngine createVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.setProperty("input.encoding", "utf-8");
		ve.setProperty("output.encoding", "utf-8");
		ve.init();
		return ve;
	}
}
