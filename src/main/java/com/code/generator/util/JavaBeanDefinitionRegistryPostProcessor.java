package com.code.generator.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.Environment;

/**
 * Created by WuTing on 2017/12/7.
 */
@Configuration
public class JavaBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
	private static final Logger logger = LoggerFactory.getLogger(JavaBeanDefinitionRegistryPostProcessor.class);

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
	private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		logger.info("Invoke Method postProcessBeanDefinitionRegistry ...");

		dataSourceMap.forEach((key, value) -> {
			try {
				String type = MapUtils.getString(value, "type");
				if (StringUtils.isEmpty(type)) {
					type = "org.apache.commons.dbcp.BasicDataSource";
				}
				registerBean(registry, key, (Class<? extends DataSource>)Class.forName(type));
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		});
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		logger.info("Invoke Method postProcessBeanFactory ...");

		beanFactory.getBeanDefinition("dataSource").setPrimary(true);
		dataSourceMap.forEach((key, value) -> {
			BeanDefinition bd = beanFactory.getBeanDefinition(key);
			MutablePropertyValues mpv = bd.getPropertyValues();
			mpv.addPropertyValues(value);
		});

	}

	private void registerBean(BeanDefinitionRegistry registry, String name, Class<? extends DataSource> beanClass) {
		AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(beanClass);

		ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(beanDefinition);
		beanDefinition.setScope(scopeMetadata.getScopeName());
		String beanName =
			StringUtils.isNotEmpty(name) ? name : beanNameGenerator.generateBeanName(beanDefinition, registry);

		AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition);

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}

	@Override
	public void setEnvironment(Environment environment) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "spring.datasource.");
		String names = propertyResolver.getProperty("names");
		Arrays.stream(names.trim().split(","))
			.forEach(name -> dataSourceMap.put(name + "DataSource", propertyResolver.getSubProperties(name + ".")));
	}
}
