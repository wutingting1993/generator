package com.code.generator.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Created by WuTing on 2017/12/5.
 */
@Configuration
@PropertySource("classpath:db.properties")
public class DBConfig {
	@Bean("schemaDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.schema")
	@Primary
	public DataSource schemaDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean("libraryDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.library")
	public DataSource libraryDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean("sakilaDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.sakila")
	public DataSource sakilaDataSource() {

		return DataSourceBuilder.create().build();
	}

	@Bean("dataSource")
	public DynamicDataSource dataSource() {

		return new DynamicDataSource("libraryDataSource");
	}

	@Bean("jdbcTemplate")
	public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DynamicDataSource dataSource) {

		return new JdbcTemplate(dataSource);
	}

	@Bean("jdbcReadTemplate")
	public JdbcTemplate jdbcReadTemplate(@Qualifier("dataSource") DynamicDataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean("transactionManager")
	public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
