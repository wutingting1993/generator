package com.code.generator.vo;

import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by WuTing on 2017/12/7.
 */

@Getter
@Setter
public class GeneratorConfig {

	private String schema;
	private List<String> excludeTables;
	private List<String> includeTables;

	public List<String> getExcludeTables() {
		if (CollectionUtils.isEmpty(excludeTables)) {
			excludeTables = Collections.emptyList();
		}
		return excludeTables;
	}

	public List<String> getIncludeTables() {
		if (CollectionUtils.isEmpty(includeTables)) {
			includeTables = Collections.emptyList();
		}
		return includeTables;
	}
}
