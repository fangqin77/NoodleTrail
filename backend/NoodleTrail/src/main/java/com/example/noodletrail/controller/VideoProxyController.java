package com.example.noodletrail.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频流代理：支持 B 站 / 抖音 / 本地同域视频直链。
 * 仅做流式转发，不落盘。
 */
@RestController
@RequestMapping("/api")
public class VideoProxyController {

    private static final Logger log = LoggerFactory.getLogger(VideoProxyController.class);

    /**
     * 旧接口，兼容使用：GET /api/videoProxy?url=
     */
    @GetMapping("/videoProxy")
    public void proxyYoutube(@RequestParam("url") String encodedUrl,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        if (encodedUrl == null || encodedUrl.isBlank()) {
            throw new RuntimeException("url 不能为空");
        }

        String targetUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
        URL pageUrl = new URL(targetUrl);
        String pageHost = pageUrl.getHost();
        String lowerHost = pageHost != null ? pageHost.toLowerCase() : "";

        // 如果是 YouTube 链接，当前环境不支持，直接提示前端
        if (lowerHost.endsWith("youtube.com") || lowerHost.endsWith("youtu.be")) {
            throw new RuntimeException("当前环境不支持播放 YouTube 视频，请使用国内平台链接");
        }

        // 对 B站 / 抖音的网页或分享链接，先通过 yt-dlp 解析出真实视频流地址
        String realUrl = targetUrl;
        if (lowerHost.contains("bilibili.com") || lowerHost.endsWith("b23.tv")
                || lowerHost.contains("douyin.com") || lowerHost.contains("iesdouyin.com")) {
            realUrl = resolveYoutubeStreamUrl(targetUrl);
        } else if (lowerHost.contains("sxfycc.com")) {
            // 陕西非遗网视频详情页：先解析出页面中的真实视频地址
            realUrl = resolveSxfyVideoUrl(targetUrl);
        }

        URL url = new URL(realUrl);
        String host = url.getHost();

        // 只允许代理特定视频平台，避免变成通用代理
        if (!isSupportedVideoHost(host, request)) {
            throw new RuntimeException("暂不支持该平台视频（仅支持 B站 / 抖音 / 本地直链）");
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        // 透传 Range 头，支持拖动进度条
        String range = request.getHeader("Range");
        if (range != null && !range.isBlank()) {
            conn.setRequestProperty("Range", range);
        }
        // 设置一个常见的 User-Agent，避免部分源站拒绝请求
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");

        int status = conn.getResponseCode();
        if (status == HttpServletResponse.SC_OK || status == HttpServletResponse.SC_PARTIAL_CONTENT) {
            if (status == HttpServletResponse.SC_PARTIAL_CONTENT) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }

            String contentType = conn.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "video/mp4";
            }
            response.setContentType(contentType);

            // 透传与视频播放相关的关键头部
            String contentLength = conn.getHeaderField("Content-Length");
            if (contentLength != null) {
                response.setHeader("Content-Length", contentLength);
            }
            String contentRange = conn.getHeaderField("Content-Range");
            if (contentRange != null) {
                response.setHeader("Content-Range", contentRange);
            }
            String acceptRanges = conn.getHeaderField("Accept-Ranges");
            if (acceptRanges != null) {
                response.setHeader("Accept-Ranges", acceptRanges);
            }

            try {
                try (InputStream in = conn.getInputStream();
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                }
            } catch (org.apache.catalina.connector.ClientAbortException e) {
                log.warn("客户端主动断开视频流连接: {}", e.getMessage());
            }
        } else {
            // 简单抛错，由全局异常处理返回 JSON 错误信息
            throw new RuntimeException("上游视频源返回状态码: " + status);
        }
    }

    /**
     * 新统一入口：GET /api/video/proxy?url=
     * 内部复用 proxyYoutube 逻辑。
     */
    @GetMapping("/video/proxy")
    public void proxyVideo(@RequestParam("url") String encodedUrl,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        proxyYoutube(encodedUrl, request, response);
    }

    /**
     * 解析陕西非遗网视频详情页中的视频真实地址。
     * 做法：请求 HTML，再用正则从源码中提取第一个 mp4/m3u8 链接。
     */
    private String resolveSxfyVideoUrl(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");

            int status = conn.getResponseCode();
            if (status != HttpServletResponse.SC_OK) {
                throw new RuntimeException("获取陕西非遗网页面失败，状态码: " + status);
            }

            StringBuilder html = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    html.append(line);
                }
            }

            // 从 HTML 中提取第一个 mp4 或 m3u8 链接，优先选择包含 sxfycc.com 的链接
            Pattern pattern = Pattern.compile("(https?://[^\"'\\s>]+\\.(mp4|m3u8))", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html.toString());
            String candidate = null;
            while (matcher.find()) {
                String urlStr = matcher.group(1);
                String lower = urlStr.toLowerCase();
                if (lower.contains("sxfycc.com")) {
                    return urlStr;
                }
                if (candidate == null) {
                    candidate = urlStr;
                }
            }
            if (candidate != null) {
                return candidate;
            }

            throw new RuntimeException("无法从陕西非遗网页面解析出视频地址");
        } catch (Exception e) {
            throw new RuntimeException("解析陕西非遗网页面视频地址失败", e);
        }
    }

    /**
     * 调用本机 yt-dlp 解析视频真实流地址（用于 B站 / 抖音 等）。
     * 需要服务器已安装 yt-dlp 并在 PATH 中可执行。
     */
    private String resolveYoutubeStreamUrl(String pageUrl) {
        // 优先选择 mp4 格式，其次退回 best 可用格式
        ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-g", "-f", "best[ext=mp4]/best", pageUrl);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                int exit = process.waitFor();
                if (exit != 0 || line == null || line.isBlank()) {
                    throw new RuntimeException("解析视频地址失败，请检查 yt-dlp 是否已安装或网络是否可用");
                }
                // 可能返回多行 URL，这里只取第一行主流
                return line.trim();
            }
        } catch (Exception e) {
            throw new RuntimeException("调用 yt-dlp 解析视频链接失败，请确认服务器已安装 yt-dlp", e);
        }
    }

    /**
     * 判断 host 是否属于允许代理的视频平台。
     */
    private boolean isSupportedVideoHost(String host, HttpServletRequest request) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String h = host.toLowerCase();
        // Bilibili 页面 / 分享域名
        if (h.endsWith("bilibili.com") || h.endsWith("b23.tv")) {
            return true;
        }
        // Bilibili 视频 CDN
        if (h.contains("bilivideo.com")) {
            return true;
        }
        // Douyin 页面 / 分享 / API 域名
        if (h.contains("douyin.com") || h.contains("iesdouyin.com") || h.contains("snssdk.com")) {
            return true;
        }
        // Douyin 视频 CDN
        if (h.contains("douyinvod.com")) {
            return true;
        }
        // 陕西非遗网及其视频资源域名
        if (h.contains("sxfycc.com")) {
            return true;
        }
        // 本地视频：与当前服务同域
        String serverName = request.getServerName();
        if (serverName != null && !serverName.isBlank() && h.equals(serverName.toLowerCase())) {
            return true;
        }
        return false;
    }
}
