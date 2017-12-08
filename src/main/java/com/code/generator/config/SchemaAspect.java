package com.code.generator.config;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by WuTing on 2017/12/5.
 */
@Component
@Aspect
@Order(1)
public class SchemaAspect {
	private static final Logger logger = LoggerFactory.getLogger(SchemaAspect.class);

	@Before(value = "@annotation(com.code.generator.config.Schema)")
	public void before(JoinPoint point) {
		String dataSourceName = getDataSourceName(point);
		if (StringUtils.isNotEmpty(dataSourceName)) {
			DataSourceContextHolder.setDataSourceType(dataSourceName);
			logger.debug("Set DataSource : {} > {}", dataSourceName, point.getSignature());
		}
	}

	@After(value = "@annotation(com.code.generator.config.Schema)")
	public void restoreDataSource(JoinPoint point) {

		String dataSourceName = getDataSourceName(point);
		if (StringUtils.isNotEmpty(dataSourceName)) {
			logger.debug("Revert DataSource : {} > {}", dataSourceName, point.getSignature());
		}
		DataSourceContextHolder.clearDataSourceType();
	}

	private String getDataSourceName(JoinPoint point) {
		Parameter[] parameters = ((MethodSignature)point.getSignature()).getMethod().getParameters();
		Optional<Parameter> parameterTemp = Arrays.stream(parameters)
			.filter(parameter -> parameter.isAnnotationPresent(Schema.class))
			.findAny();

		String dataSourceName = null;
		if (parameterTemp.isPresent()) {
			Schema schema = parameterTemp.get().getAnnotation(Schema.class);
			dataSourceName = schema.value();
			if (StringUtils.isEmpty(dataSourceName)) {

			}
		}

		return dataSourceName;
	}
}
