package com.zw.builder.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字符串处理类
 *
 * @author 01369611
 */
public class StringUtil {

    private StringUtil() {

    }

    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * 空串判断
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {

        if (str == null || str.length() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断是否是数字
     *
     * @param str
     * @return
     */
	public static boolean isNumber(String str) {
		return str.trim().matches(
				"^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

    /**
     * 是否为纯数字
     *
     * @param str
     * @return
     */
    public static boolean isDigital(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否是数字，并设置最大值
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str, int min, int max) {

        if (str.isEmpty()) return false;
        try {
            int num = Integer.parseInt(str);
            if (num <= max && num >= min) return true;
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            return false;
        }
        return false;
    }

    /**
     * 获取特定连接符连接的字符串
     *
     * @return
     */
    public static String getConcatString(String connector, String... args) {

        String result = args[0];
        for (int i = 1; i < args.length; i++) {
            result = result.concat(connector).concat(args[i]);
        }

        return result;
    }

    /**
     * 判断多个参数是否为空，全部为空返回true
     * <pre>
     * StringUtils.isAnyBlank(null)      = true
     * StringUtils.isAnyBlank("")        = true
     * StringUtils.isAnyBlank("","")     = true
     * StringUtils.isAnyBlank("a")     = false
     * StringUtils.isAnyBlank("","a")     = false
     * </pre>
     *
     * @param css
     * @return
     */
    public static boolean isAnyBlank(CharSequence... css) {

        if (ArrayUtils.isEmpty(css)) {
            return true;
        }
        for (CharSequence cs : css) {
            if (StringUtils.isNotBlank(cs)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isListStringEmpty(List<String> strings) {
        if (strings == null) {
            return true;
        }
        for (String str : strings) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    public static <T> String CollectionToString(Collection<T> t) {
        StringBuilder sb = new StringBuilder();
        for (T item : t) {
            sb.append(item.toString() + "\n");
        }
        return sb.toString();
    }


    /**
     * 格式化errors
     *
     * @param i
     * @param errors
     */
    public static List<String> generateErrorMsg(int i, List<String> errors) {
        List<String> result = new ArrayList<>();
        for (String str : errors) {
            result.add("第" + i + "行," + str);
        }
        return result;
    }
}