package com.exam.utils;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * 网络工具类
 */
@Slf4j
public class NetUtils {

    /**
     * 获取客户端 IP 地址
     *
     * @param request HTTP请求
     * @return IP地址
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
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                ip = getLocalIpAddress();
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip == null ? "127.0.0.1" : ip;
    }

    /**
     * 获取本地内网IP地址
     *
     * @return 内网IP地址
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 排除回环接口，点对点接口等
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("获取本地IP地址失败", e);
        }
        return "127.0.0.1";
    }

    /**
     * 获取公网IP地址
     * 通过访问公共API获取
     *
     * @return 公网IP地址
     */
    public static String getPublicIpAddress() {
        String publicIp = "";
        try {
            // 可以尝试多个公共API
            String[] apiUrls = {
                "http://checkip.amazonaws.com",
                "http://icanhazip.com",
                "http://ipecho.net/plain",
                "http://whatismyip.akamai.com"
            };

            for (String apiUrl : apiUrls) {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        publicIp = reader.readLine().trim();
                        reader.close();
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("获取公网IP地址失败", e);
        }
        return publicIp.isEmpty() ? null : publicIp;
    }

    /**
     * 判断是否是内网IP地址
     *
     * @param ip IP地址
     * @return 是否是内网IP
     */
    public static boolean isInternalIp(String ip) {
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            return isInternalIp(addr);
        } catch (Exception e) {
            log.error("判断IP地址类型失败", e);
            return false;
        }
    }

    /**
     * 判断是否是内网IP地址
     *
     * @param addr IP地址字节数组
     * @return 是否是内网IP
     */
    public static boolean isInternalIp(byte[] addr) {
        if (addr == null || addr.length < 2) {
            return false;
        }
        // 10.x.x.x/8
        final byte b0 = addr[0];
        if (b0 == 10) {
            return true;
        }
        // 172.16.x.x/12
        final byte b1 = addr[1];
        if (b0 == (byte) 172 && (b1 & 0xF0) == 16) {
            return true;
        }
        // 192.168.x.x/16
        return b0 == (byte) 192 && b1 == (byte) 168;
    }
}
