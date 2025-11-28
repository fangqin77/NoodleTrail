package com.example.noodletrail.user.service;

import com.example.noodletrail.config.WxMaConfig;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.mapper.WxUserMapper;
import com.example.noodletrail.util.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WxUserService {

    private final WxUserMapper wxUserMapper;
    private final WxMaConfig wxConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WxUserService(WxUserMapper wxUserMapper, WxMaConfig wxConfig) {
        this.wxUserMapper = wxUserMapper;
        this.wxConfig = wxConfig;
    }

    /**
     * 微信授权登录：直接调用微信官方 jscode2session 接口，不依赖第三方 SDK
     */
    public WxUser login(String code, WxUser userInfo) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxConfig.getAppid(),
                    wxConfig.getSecret(),
                    code
            );

            String response = HttpUtil.doGet(url);
            JsonNode json = objectMapper.readTree(response);

            // 微信错误处理：返回 errcode 时表示失败
            if (json.has("errcode")) {
                int errCode = json.get("errcode").asInt();
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "";
                throw new RuntimeException("微信授权失败：" + errCode + "，" + errMsg);
            }

            String openid = json.has("openid") ? json.get("openid").asText() : null;
            String unionid = json.has("unionid") ? json.get("unionid").asText() : null;

            if (openid == null || openid.isEmpty()) {
                throw new RuntimeException("微信授权失败：未获取到 openid");
            }

            WxUser existUser = wxUserMapper.selectByOpenid(openid);
            if (existUser != null) {
                existUser.setLastLoginTime(LocalDateTime.now());
                if (userInfo != null) {
                    existUser.setNickname(userInfo.getNickname());
                    existUser.setAvatarUrl(userInfo.getAvatarUrl());
                    existUser.setGender(userInfo.getGender());
                    existUser.setCity(userInfo.getCity());
                    existUser.setProvince(userInfo.getProvince());
                    existUser.setCountry(userInfo.getCountry());
                }
                wxUserMapper.updateById(existUser);
                return existUser;
            } else {
                WxUser newUser = new WxUser();
                newUser.setOpenid(openid);
                newUser.setUnionid(unionid);
                if (userInfo != null) {
                    newUser.setNickname(userInfo.getNickname());
                    newUser.setAvatarUrl(userInfo.getAvatarUrl());
                    newUser.setGender(userInfo.getGender());
                    newUser.setCity(userInfo.getCity());
                    newUser.setProvince(userInfo.getProvince());
                    newUser.setCountry(userInfo.getCountry());
                }
                LocalDateTime now = LocalDateTime.now();
                newUser.setCreateTime(now);
                newUser.setUpdateTime(now);
                newUser.setLastLoginTime(now);
                wxUserMapper.insert(newUser);
                return newUser;
            }
        } catch (Exception e) {
            throw new RuntimeException("微信授权登录失败: " + e.getMessage(), e);
        }
    }

    public WxUser getByOpenid(String openid) {
        return wxUserMapper.selectByOpenid(openid);
    }
}
