package com.example.noodletrail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度语音合成配置
 */
@Component
@ConfigurationProperties(prefix = "baidu.tts")
public class BaiduTtsConfig {

    /** 百度智能云 API Key */
    private String apiKey;

    /** 百度智能云 Secret Key */
    private String secretKey;

    /**
     * 百度 TTS 发音人：
     * 默认 4 = 度小宇（通用方言，所有账号都支持）；
     * 如账号已开通专用陕西方言，可将配置 baidu.tts.per 改为 1051。
     */
    private int per = 4;

    /** 语速（0-9） */
    private int spd = 5;

    /** 音调（0-9） */
    private int pit = 5;

    /** 音量（0-15） */
    private int vol = 7;

    /** 音频格式（3=MP3） */
    private int aue = 3;

    /** AccessToken 获取地址 */
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";

    /** 语音合成 API 地址 */
    private String ttsApiUrl = "https://tsn.baidu.com/text2audio";

    /** AccessToken 有效期（秒），默认 30 天 */
    private long tokenExpireSeconds = 2592000L;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getPer() {
        return per;
    }

    public void setPer(int per) {
        this.per = per;
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }

    public int getPit() {
        return pit;
    }

    public void setPit(int pit) {
        this.pit = pit;
    }

    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public int getAue() {
        return aue;
    }

    public void setAue(int aue) {
        this.aue = aue;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getTtsApiUrl() {
        return ttsApiUrl;
    }

    public void setTtsApiUrl(String ttsApiUrl) {
        this.ttsApiUrl = ttsApiUrl;
    }

    public long getTokenExpireSeconds() {
        return tokenExpireSeconds;
    }

    public void setTokenExpireSeconds(long tokenExpireSeconds) {
        this.tokenExpireSeconds = tokenExpireSeconds;
    }
}
