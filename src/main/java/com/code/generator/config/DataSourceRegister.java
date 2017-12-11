package com.code.generator.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Created by WuTing on 2017/12/8.
 */
public class DataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRegister.class);
	private static final BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
	private static final String[] DATA_SOURCE_TYPE_NAMES = {"org.apache.tomcat.jdbc.pool.DataSource",
		"com.zaxxer.hikari.HikariDataSource", "org.apache.commons.dbcp.BasicDataSource",
		"org.apache.commons.dbcp2.BasicDataSource"};
	private static Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();
	private static Map<String, Object> commProperties = new HashMap<>();
	private static Map<String, Object> poolProperties = new HashMap<>();
	private static String primary;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		this.registerDataSources(registry);

		this.registerDynamicDataSource(registry);
	}

	private void registerDataSources(BeanDefinitionRegistry registry) {
		dataSourceMap.forEach((dataSourceName, properties) -> {
			maybeGetDriverClassName(properties);
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setSynthetic(true);
			beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

			MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
			propertyValues.addPropertyValues(poolProperties);
			propertyValues.addPropertyValues(properties);

			dataSourceName = this.getDataSourceName(registry, dataSourceName, beanDefinition);
			if (primary.equals(dataSourceName)) {
				beanDefinition.setPrimary(true);
			}

			this.printDataSourceConfigInfo(dataSourceName, beanDefinition);
			beanDefinition.setBeanClass(this.findType(MapUtils.getString(properties, "type")));
			registry.registerBeanDefinition(dataSourceName, beanDefinition);

		});
	}

	private void registerDynamicDataSource(BeanDefinitionRegistry registry) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setSynthetic(true);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		beanDefinition.setBeanClass(DynamicDataSource.class);
		ConstructorArgumentValues args = new ConstructorArgumentValues();
		args.addIndexedArgumentValue(0, primary);
		beanDefinition.setConstructorArgumentValues(args);
		registry.registerBeanDefinition("dataSource", beanDefinition);
	}

	private void printDataSourceConfigInfo(String dataSourceName, GenericBeanDefinition beanDefinition) {
		System.out.println("\n");
		LOGGER.warn(dataSourceName);
		Arrays.stream(beanDefinition.getPropertyValues().getPropertyValues())
			.forEach((property) -> LOGGER.warn(property.getName() + " --> " + property.getValue()));
		LOGGER.warn("primary --> " + beanDefinition.isPrimary());
	}

	private String getDataSourceName(BeanDefinitionRegistry registry, String dataSourceName,
		GenericBeanDefinition beanDefinition) {
		if (StringUtils.isEmpty(dataSourceName)) {
			dataSourceName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		}
		return dataSourceName;
	}

	@Override
	public void setEnvironment(Environment environment) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "spring.datasource.");
		commProperties.putAll(propertyResolver.getSubProperties("comm."));
		poolProperties.putAll(propertyResolver.getSubProperties("pool."));
		this.initDataSourceMap(propertyResolver);
		this.initPrimary();
	}

	private void initDataSourceMap(RelaxedPropertyResolver propertyResolver) {
		String names = MapUtils.getString(commProperties, "names");
		if (StringUtils.isNotEmpty(names)) {
			Arrays.stream(names.trim().split(","))
				.forEach(name -> {
					String dataSourceName = name;
					if (!name.toLowerCase().contains("datasource")) {
						dataSourceName += "DataSource";
					}
					dataSourceMap.put(dataSourceName, propertyResolver.getSubProperties(name + "."));
				});
		}

		if (dataSourceMap.isEmpty()) {
			LOGGER.warn("DataSource Name is Empty...");
		}
	}

	private void initPrimary() {
		primary = MapUtils.getString(commProperties, "primary");
		if (StringUtils.isEmpty(primary)) {
			primary = dataSourceMap.keySet().stream().findAny().orElse("");
		}
		if (!primary.toLowerCase().contains("datasource")) {
			primary += "DataSource";
		}
		LOGGER.warn("Primary DataSource bean name is " + primary);
	}

	private void maybeGetDriverClassName(Map<String, Object> properties) {
		if (!properties.containsKey("driverClassName") && properties.containsKey("url")) {
			String url = (String)properties.get("url");
			String driverClass = DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
			properties.put("driverClassName", driverClass);
		}
	}

	private Class<? extends DataSource> findType(String typeName) {
		Class<? extends DataSource> clazz = null;
		if (StringUtils.isNotEmpty(typeName)) {
			try {
				clazz = (Class<? extends DataSource>)Class.forName(typeName);
			} catch (Exception e) {
				LOGGER.debug("can not find class: " + typeName);
			}
		}
		if (clazz == null) {
			int index = 0;
			while (index < DATA_SOURCE_TYPE_NAMES.length) {
				String name = DATA_SOURCE_TYPE_NAMES[index];
				try {
					clazz = (Class<? extends DataSource>)Class.forName(name);
					break;
				} catch (Exception e) {
					LOGGER.debug("can not find class: " + name);
					++index;
				}
			}
		}
		if (clazz != null) {
			LOGGER.warn("dataSourceType --> " + clazz.getName());
		} else {
			LOGGER.warn("Can not find DataSource Type ", DATA_SOURCE_TYPE_NAMES);
		}

		return clazz;

	}
}