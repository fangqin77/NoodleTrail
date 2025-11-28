package com.example.noodletrail.user.controller;

import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class WxUserController {

    private final WxUserService wxUserService;

    public WxUserController(WxUserService wxUserService) {
        this.wxUserService = wxUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> param) {
        String code = (String) param.get("code");
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfoMap = (Map<String, Object>) param.get("userInfo");

        WxUser userInfo = null;
        if (userInfoMap != null) {
            userInfo = new WxUser();
            Object nickName = userInfoMap.get("nickName");
            if (nickName != null) {
                userInfo.setNickname(nickName.toString());
            }
            Object avatarUrl = userInfoMap.get("avatarUrl");
            if (avatarUrl != null) {
                userInfo.setAvatarUrl(avatarUrl.toString());
            }
            Object gender = userInfoMap.get("gender");
            if (gender != null) {
                try {
                    userInfo.setGender(Integer.parseInt(gender.toString()));
                } catch (NumberFormatException e) {
                    userInfo.setGender(0);
                }
            }
            Object city = userInfoMap.get("city");
            if (city != null) {
                userInfo.setCity(city.toString());
            }
            Object province = userInfoMap.get("province");
            if (province != null) {
                userInfo.setProvince(province.toString());
            }
            Object country = userInfoMap.get("country");
            if (country != null) {
                userInfo.setCountry(country.toString());
            }
        }

        WxUser user = wxUserService.login(code, userInfo);

        Map<String, Object> data = new HashMap<>();
        data.put("openid", user.getOpenid());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("gender", user.getGender());
        data.put("city", user.getCity());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("data", data);

        return ResponseEntity.ok(result);
    }
}
