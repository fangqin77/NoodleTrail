package com.example.noodletrail.tts;

import com.example.noodletrail.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

/**
 * 语音合成接口：使用百度 TTS 将文本转换为陕西方言语音。
 */
@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private static final Logger log = LoggerFactory.getLogger(TtsController.class);

    private final BaiduTtsUtil baiduTtsUtil;

    public TtsController(BaiduTtsUtil baiduTtsUtil) {
        this.baiduTtsUtil = baiduTtsUtil;
    }

    /**
     * 文本转语音（陕西方言），返回 Base64 音频字符串。
     *
     * 前端可以直接 POST 一个 JSON：{"text":"xxx"}，
     * 也兼容直接传字符串（"xxx"），这里做了兼容解析。
     */
    @PostMapping("/synthesize")
    public ApiResponse<String> synthesize(@RequestBody(required = false) Object body) {
        try {
            String text = extractText(body);
            if (text == null || text.isBlank()) {
                return ApiResponse.fail("请输入待合成文本", "BadRequest");
            }

            String trimText = text.trim();
            if (trimText.length() > 1024) {
                return ApiResponse.fail("文本长度不能超过 1024 字", "BadRequest");
            }

            byte[] audioBytes = baiduTtsUtil.synthesizeShaanxiAudio(trimText);
            String base64 = Base64.getEncoder().encodeToString(audioBytes);
            return ApiResponse.ok(base64);
        } catch (Exception e) {
            log.error("语音合成失败", e);
            String msg = "语音合成失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误");
            return ApiResponse.fail(msg, "TtsError");
        }
    }

    private String extractText(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof String s) {
            return s;
        }
        if (body instanceof Map<?, ?> map) {
            Object t = map.get("text");
            if (t == null) {
                // 兼容前端直接传 {"content":"xxx"}
                t = map.get("content");
            }
            return t != null ? t.toString() : null;
        }
        // 其它情况退化为 toString
        return body.toString();
    }
}
