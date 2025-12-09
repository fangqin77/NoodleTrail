package com.example.noodletrail.tts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 调用百度 TTS 的 HTTP 工具。
 */
@Component
public class HttpTtsUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpTtsUtil.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 调用百度语音合成接口，返回音频二进制。
     */
    public byte[] synthesize(String apiUrl, String token, String text) throws IOException, InterruptedException {
        StringBuilder form = new StringBuilder();
        form.append("tex=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
        form.append("&tok=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
        form.append("&lan=zh");
        form.append("&ctp=1");
        form.append("&cuid=").append(URLEncoder.encode("noodletrail_tts", StandardCharsets.UTF_8));
        form.append("&spd=5");
        form.append("&pit=5");
        form.append("&vol=7");
        // sx: 陕西方言（具体以百度官方文档为准）
        form.append("&per=sx");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        int status = response.statusCode();
        if (status != 200) {
            log.error("调用百度 TTS 失败, 状态码: {}", status);
            throw new IOException("调用百度 TTS 失败, status=" + status);
        }

        // 百度错误时返回 JSON 文本而不是音频，这里简单检查一下 Content-Type
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (contentType.contains("application/json") || contentType.contains("application/javascript")) {
            String err = new String(response.body(), StandardCharsets.UTF_8);
            log.error("百度 TTS 返回错误: {}", err);
            throw new IOException("百度 TTS 返回错误: " + err);
        }

        return response.body();
    }
}
