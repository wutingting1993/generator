package com.code.generator.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by WuTing on 2017/12/6.
 */
@Getter
@Setter
public class ModelInfo {
	private String modelBoName;
	private String boPackageName;
	private List<String> dataImport;
	private List<FieldInfo> rowsData;
}
