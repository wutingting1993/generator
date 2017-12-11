package com.code.generator.service;

import java.util.List;
import java.util.Map;

import com.code.generator.annotation.Schema;
import com.code.generator.vo.TableInfo;

/**
 * Created by WuTing on 2017/12/11.
 */
public interface DBService {

	List<TableInfo> getTableInfos(List<String> schemas);

	Map<String, String> getTablesColumns(@Schema String schema, String tableName);
}
