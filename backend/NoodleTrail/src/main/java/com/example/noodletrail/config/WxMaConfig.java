package com.example.noodletrail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置（仅保存 appid 和 secret），不依赖任何 SDK
 */
@Configuration
public class WxMaConfig {

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    public String getAppid() {
        return appid;
    }

    public String getSecret() {
        return secret;
    }
}
