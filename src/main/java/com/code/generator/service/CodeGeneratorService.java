package com.code.generator.service;

import java.util.List;

import com.code.generator.vo.TableInfo;

/**
 * Created by WuTing on 2017/12/6.
 */
public interface CodeGeneratorService {
	void generatorModel();
	List<TableInfo> getTableInfos(List<String> schemas);
	void getTablesBySchema(String schema, List<TableInfo> tableInfos);
}
