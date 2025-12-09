package com.example.noodletrail.tts;

import com.example.noodletrail.config.BaiduTtsConfig;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 百度 TTS 工具类（陕西方言，表单提交版本）。
 */
@Component
public class BaiduTtsUtil {

    private static final Logger log = LoggerFactory.getLogger(BaiduTtsUtil.class);

    private final BaiduTtsConfig baiduTtsConfig;
    private final BaiduTokenUtil baiduTokenUtil;

    // 百度TTS核心接口（表单提交专用）
    private static final String BAIDU_TTS_FORM_URL = "https://tsn.baidu.com/text2audio";

    public BaiduTtsUtil(BaiduTtsConfig baiduTtsConfig, BaiduTokenUtil baiduTokenUtil) {
        this.baiduTtsConfig = baiduTtsConfig;
        this.baiduTokenUtil = baiduTokenUtil;
    }

    /**
     * 生成陕西方言语音（MP3 二进制）。
     */
    public byte[] synthesizeShaanxiAudio(String text) throws Exception {
        String accessToken = baiduTokenUtil.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("百度Token获取失败");
        }

        // 1. 构建表单参数（所有必填项）
        List<NameValuePair> formParams = new ArrayList<>();
        // 百度 TTS 使用 tok 参数传递 OAuth access_token
        formParams.add(new BasicNameValuePair("tok", accessToken)); // 鉴权Token
        formParams.add(new BasicNameValuePair("tex", text)); // 待合成文本
        formParams.add(new BasicNameValuePair("lan", "zh")); // 语言类型（中文）
        formParams.add(new BasicNameValuePair("per", String.valueOf(baiduTtsConfig.getPer()))); // 陕西方言（1051）
        formParams.add(new BasicNameValuePair("spd", String.valueOf(baiduTtsConfig.getSpd()))); // 语速
        formParams.add(new BasicNameValuePair("pit", String.valueOf(baiduTtsConfig.getPit()))); // 音调
        formParams.add(new BasicNameValuePair("vol", String.valueOf(baiduTtsConfig.getVol()))); // 音量
        formParams.add(new BasicNameValuePair("aue", String.valueOf(baiduTtsConfig.getAue()))); // 音频格式（3=MP3）
        formParams.add(new BasicNameValuePair("cuid", "noodletrail_tts")); // 设备标识
        formParams.add(new BasicNameValuePair("ctp", "1")); // 客户端类型，1=web

        // 2. 创建POST请求，提交表单
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BAIDU_TTS_FORM_URL);
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    // 成功：直接返回MP3二进制数组
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    // 失败：解析错误信息
                    String errorMsg = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.error("百度TTS合成失败，状态码：{}，错误信息：{}", statusCode, errorMsg);
                    throw new RuntimeException("合成失败：" + errorMsg);
                }
            }
        }
    }
}
