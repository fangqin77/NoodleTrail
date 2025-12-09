package com.example.noodletrail.tts;

import com.example.noodletrail.config.BaiduTtsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 百度 AccessToken 工具类：缓存 + 定时刷新。
 */
@Component
public class BaiduTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(BaiduTokenUtil.class);

    private final BaiduTtsConfig config;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /** 当前 token */
    private volatile String accessToken;
    /** 过期时间戳（毫秒） */
    private volatile long expireTime;

    public BaiduTokenUtil(BaiduTtsConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        refreshToken();
        long interval = config.getTokenExpireSeconds() * 1000L * 2 / 3;
        scheduler.scheduleAtFixedRate(this::safeRefresh, interval, interval, TimeUnit.MILLISECONDS);
        log.info("Baidu AccessToken 定时刷新任务已启动, 间隔 {} ms", interval);
    }

    private void safeRefresh() {
        try {
            refreshToken();
        } catch (Exception e) {
            log.error("定时刷新 AccessToken 失败", e);
        }
    }

    /**
     * 主动刷新 token（同步执行）。
     */
    public synchronized void refreshToken() {
        try {
            String body = "grant_type=client_credentials" +
                    "&client_id=" + URLEncoder.encode(config.getApiKey(), StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(config.getSecretKey(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("获取 AccessToken 失败, 状态码: {}, body: {}", response.statusCode(), response.body());
                return;
            }

            String resp = response.body();
            // 非严格 JSON 解析，简单从字符串中截取 access_token 字段，避免额外依赖
            String tokenKey = "\"access_token\"";
            int idx = resp.indexOf(tokenKey);
            if (idx < 0) {
                log.error("AccessToken 响应中未找到 access_token 字段: {}", resp);
                return;
            }
            int colon = resp.indexOf(':', idx);
            int startQuote = resp.indexOf('"', colon + 1);
            int endQuote = resp.indexOf('"', startQuote + 1);
            if (startQuote < 0 || endQuote < 0) {
                log.error("AccessToken 解析失败: {}", resp);
                return;
            }
            String token = resp.substring(startQuote + 1, endQuote);
            this.accessToken = token;
            this.expireTime = System.currentTimeMillis() + (config.getTokenExpireSeconds() - 60) * 1000L;
            log.info("AccessToken 刷新成功");
        } catch (IOException | InterruptedException e) {
            log.error("获取 AccessToken 异常", e);
        }
    }

    /**
     * 获取可用 token，必要时强制刷新。
     */
    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= expireTime) {
            log.warn("AccessToken 为空或已过期，准备刷新");
            refreshToken();
        }
        return accessToken;
    }
}
