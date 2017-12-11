package com.code.generator.config;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
public class DataSourceAspect {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

	@Before(value = "execution(* com.code.generator.service.*.*(..))")
	public void before(JoinPoint point) {
		String dataSourceName = getDataSourceAnnotation(point);
		if (StringUtils.isNotEmpty(dataSourceName)) {
			DataSourceContextHolder.setDataSourceType(dataSourceName);
			logger.debug("Set DataSource : {} > {}", dataSourceName, point.getSignature());
		}
	}

	@After(value = "execution(* com.code.generator.service.*.*(..))")
	public void restoreDataSource(JoinPoint point) {

		String dataSourceName = getDataSourceAnnotation(point);
		if (StringUtils.isNotEmpty(dataSourceName)) {
			logger.debug("Revert DataSource : {} > {}", dataSourceName, point.getSignature());
		}
		DataSourceContextHolder.clearDataSourceType();
	}

	private String getDataSourceAnnotation(JoinPoint point) {

		Method method = ((MethodSignature)point.getSignature()).getMethod();

		String dataSourceName = null;

		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].isAnnotationPresent(Schema.class)) {
				Schema schema = parameters[i].getAnnotation(Schema.class);
				dataSourceName = schema.value();
				if (StringUtils.isEmpty(dataSourceName)) {
					dataSourceName = (String)point.getArgs()[i];
					break;
				}
			}
		}

		if (StringUtils.isEmpty(dataSourceName) && method.isAnnotationPresent(DataSource.class)) {
			dataSourceName = method.getAnnotation(DataSource.class).value();
		}
		if (StringUtils.isEmpty(dataSourceName) && point.getTarget().getClass().isAnnotationPresent(DataSource.class)) {
			dataSourceName = point.getTarget().getClass().getAnnotation(DataSource.class).value();
		}
		if (StringUtils.isNotEmpty(dataSourceName) &&!dataSourceName.toLowerCase().contains("datasource")){
			dataSourceName+="DataSource";
		}
		return dataSourceName;
	}
}
