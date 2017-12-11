package com.code.generator.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by WuTing on 2017/12/11.
 */
public class StringUtil {
	public static String underlineToCamel(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		StringBuilder sb = new StringBuilder(param);
		Matcher mc = Pattern.compile("_+|\\s+").matcher(param);
		int i = 0;
		while (mc.find()) {
			int position = mc.end() - (i++);
			sb.setCharAt(position, Character.toUpperCase(sb.charAt(position)));
			sb.deleteCharAt(position - 1);
		}

		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}

	public static String firstCharToUpper(String name) {
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

	public static List<String> stringToList(Object value) {
		return Arrays.stream(value.toString().split(","))
			.map(s -> s.toLowerCase())
			.collect(Collectors.toList());
	}
}
