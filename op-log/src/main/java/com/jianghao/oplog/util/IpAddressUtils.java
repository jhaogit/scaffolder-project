package com.jianghao.oplog.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证ip地址工具类
 *
 */
public class IpAddressUtils {

    /**
     * 获取请求IP地址
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if(ip!=null){
            if (ip.contains(",")) {
                return ip.split(",")[0];
            } else {
                return ip;
            }
        }
        return null;
    }
}
