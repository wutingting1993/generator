package com.code.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by WuTing on 2017/12/7.
 */
public class PropertyUtil {
	private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
	public static Properties props;

	static {
		loadProps("generator-config.properties");
	}

	synchronized static private void loadProps(String fileName) {
		props = new Properties();
		InputStream in = null;
		try {
			in = PropertyUtil.class.getClassLoader().getResourceAsStream(fileName);
			props.load(in);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		logger.info("properties文件内容：" + props);
	}

	public static String getProperty(String key) {
		if (null == props) {
			loadProps("generator-config.properties");
		}
		return props.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		if (null == props) {
			loadProps("generator-config.properties");
		}
		return props.getProperty(key, defaultValue);
	}
}
