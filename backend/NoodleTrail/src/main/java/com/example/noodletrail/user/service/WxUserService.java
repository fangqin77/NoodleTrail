package com.example.noodletrail.user.service;

import com.example.noodletrail.config.WxMaConfig;
import com.example.noodletrail.user.dto.UserUpdateDTO;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.mapper.WxUserMapper;
import com.example.noodletrail.util.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class WxUserService {

    private static final Logger log = LoggerFactory.getLogger(WxUserService.class);

    private final WxUserMapper wxUserMapper;
    private final WxMaConfig wxConfig;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WxUserService(WxUserMapper wxUserMapper, WxMaConfig wxConfig, Environment environment) {
        this.wxUserMapper = wxUserMapper;
        this.wxConfig = wxConfig;
        this.environment = environment;
    }

    /**
     * 微信授权登录：优先调用微信官方 jscode2session，
     * - 对 40029（invalid code）、40163（code been used）抛出明确业务异常，提示前端重新获取 code
     * - 其他调用异常在 dev/test 环境降级为本地模拟登录（使用 code 作为 mock openid），生产环境则返回统一错误。
     */
    public WxUser login(String code, WxUser userInfo) {
        try {
            // 前置校验：code 不能为空
            if (code == null || code.trim().isEmpty()) {
                log.error("调用微信授权接口失败：code 为空");
                throw new IllegalArgumentException("登录凭证不能为空，请重新获取 code");
            }

            log.info("调用微信 jscode2session 接口，code: {}", code);
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxConfig.getAppid(),
                    wxConfig.getSecret(),
                    code
            );
            log.info("微信 jscode2session 请求地址: {}", url);

            String response = HttpUtil.doGet(url);
            JsonNode json = objectMapper.readTree(response);

            // 微信错误处理：返回 errcode 时表示失败
            if (json.has("errcode")) {
                int errCode = json.get("errcode").asInt();
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "";
                String rid = json.has("rid") ? json.get("rid").asText() : null;
                log.error("微信授权失败，code: {}, errCode: {}, errMsg: {}, rid: {}", code, errCode, errMsg, rid);

                // 40029: invalid code（code 无效）
                if (errCode == 40029) {
                    throw new IllegalArgumentException("登录凭证无效，请重新获取 code 再登录");
                }
                // 40163: code been used（code 已被使用）
                if (errCode == 40163) {
                    throw new IllegalArgumentException("登录凭证已失效，请重新获取验证码");
                }
                throw new RuntimeException("微信授权失败：" + errCode + "，" + errMsg);
            }

            String openid = json.has("openid") ? json.get("openid").asText() : null;
            String unionid = json.has("unionid") ? json.get("unionid").asText() : null;

            if (openid == null || openid.isEmpty()) {
                log.error("微信授权失败：未获取到 openid，code: {}，响应: {}", code, json.toString());
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
        } catch (IllegalArgumentException biz) {
            // 40029/40163/空 code 等明确业务错误，直接抛出给上层，由全局异常处理返回提示信息
            throw biz;
        } catch (Exception e) {
            // 调用微信接口异常（网络 / 配置问题等）
            log.error("调用微信接口异常，code: {}", code, e);
            if (isDevOrTest()) {
                // 仅在开发 / 测试环境降级为本地模拟登录，避免生产环境滥用模拟登录
                log.warn("当前为开发/测试环境，降级为本地模拟登录，code: {}", code);
                String mockOpenid = "mock_" + (code == null ? "" : code);
                WxUser existUser = wxUserMapper.selectByOpenid(mockOpenid);
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
                    newUser.setOpenid(mockOpenid);
                    newUser.setUnionid(null);
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
            }
            // 非 dev/test 环境：直接抛出通用异常信息
            throw new RuntimeException("登录接口异常，请稍后重试");
        }
    }

    private boolean isDevOrTest() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) {
            return false;
        }
        return Arrays.stream(profiles).anyMatch(p -> "dev".equalsIgnoreCase(p) || "test".equalsIgnoreCase(p));
    }

    public WxUser getByOpenid(String openid) {
        return wxUserMapper.selectByOpenid(openid);
    }

    public WxUser getById(Integer id) {
        return wxUserMapper.selectById(id);
    }

    public WxUser updateProfile(Integer id, UserUpdateDTO dto) {
        WxUser user = wxUserMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getCountry() != null) {
            user.setCountry(dto.getCountry());
        }
        if (dto.getProvince() != null) {
            user.setProvince(dto.getProvince());
        }
        user.setUpdateTime(LocalDateTime.now());
        wxUserMapper.updateById(user);
        return user;
    }
}