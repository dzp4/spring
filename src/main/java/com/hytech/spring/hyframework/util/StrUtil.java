package com.hytech.spring.hyframework.util;

/**
 * @author dzp 2021/4/1
 */
public class StrUtil {

    /**
     * 将字符串首字母大写 a-z:97-122;A-Z:65-90;0-9:48-57
     *
     * @param str string
     * @return String
     */
    public static String lowerCaseFist(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        if (chars[0] < 65 || chars[0] > 90) {
            return str;
        }

        chars[0] += 32;
        return String.valueOf(chars);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static String makeStringForRegexp(String str) {
        return str;
    }
}
