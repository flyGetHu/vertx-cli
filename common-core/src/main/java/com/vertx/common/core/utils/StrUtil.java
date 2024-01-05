package com.vertx.common.core.utils;

public class StrUtil extends cn.hutool.core.util.StrUtil {

    /**
     * 将传入的字符串中的大写字母替换为下划线加小写字母的形式，返回转换后的字符串。
     *
     * @param str 要进行转换的字符串
     * @return 转换后的字符串
     */
    public static String underlineName(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        if (sb.charAt(0) == '_') {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }
}
