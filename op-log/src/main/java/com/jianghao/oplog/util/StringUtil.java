package com.jianghao.oplog.util;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 13:56
 * @description：
 */

public class StringUtil {

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';

    /**
     * 驼峰式命名法
     * 例如：user_name->userName  user_nameInfo->userNameInfo
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /***
     * 驼峰命名转为下划线命名
     * @param para 驼峰命名的字符串
     */
    public static String humpToUnderline(String para) {

        StringBuffer sb = new StringBuffer();
        char[] chars = para.toCharArray();
        for (int i=0;i<chars.length;i++) {
            if(i==0){
                sb.append(chars[i]);
            }else{
                char pre = chars[i-1];
                if(Character.isLowerCase(pre)&&Character.isUpperCase(chars[i])){
                    sb.append("_").append(String.valueOf(chars[i]).toLowerCase());
                }else{
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

}