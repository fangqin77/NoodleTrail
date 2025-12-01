package com.example.noodletrail.user.controller;

import com.example.noodletrail.checkin.service.CheckinService;
import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.security.JwtUtil;
import com.example.noodletrail.user.dto.UserAchievementDTO;
import com.example.noodletrail.user.dto.UserProfileDTO;
import com.example.noodletrail.user.dto.UserUpdateDTO;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class WxUserController {

    private final WxUserService wxUserService;
    private final CheckinService checkinService;

    public WxUserController(WxUserService wxUserService, CheckinService checkinService) {
        this.wxUserService = wxUserService;
        this.checkinService = checkinService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, Object> param) {
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

        String token = JwtUtil.generate(user.getId().longValue());
        data.put("token", token);
        data.put("userId", user.getId());

        return ApiResponse.ok(data, "登录成功");
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDTO> me() {
        WxUser user = getCurrentUser();
        UserProfileDTO dto = toProfileDTO(user);
        return ApiResponse.ok(dto);
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileDTO> updateMe(@RequestBody UserUpdateDTO dto) {
        WxUser current = getCurrentUser();
        WxUser updated = wxUserService.updateProfile(current.getId(), dto);
        UserProfileDTO profileDTO = toProfileDTO(updated);
        return ApiResponse.ok(profileDTO);
    }

    @GetMapping("/achievements")
    public ApiResponse<List<UserAchievementDTO>> achievements() {
        WxUser user = getCurrentUser();
        int checkinCount = checkinService.countUserCheckins(user.getOpenid());
        List<UserAchievementDTO> list = buildAchievements(checkinCount);
        return ApiResponse.ok(list);
    }

    @GetMapping("/badges")
    public ApiResponse<List<UserAchievementDTO>> badges() {
        WxUser user = getCurrentUser();
        int checkinCount = checkinService.countUserCheckins(user.getOpenid());
        List<UserAchievementDTO> list = buildBadges(checkinCount);
        return ApiResponse.ok(list);
    }

    private WxUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        Integer userId;
        if (principal instanceof Long l) {
            userId = l.intValue();
        } else if (principal instanceof Integer i) {
            userId = i;
        } else {
            userId = Integer.parseInt(principal.toString());
        }
        WxUser user = wxUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    private UserProfileDTO toProfileDTO(WxUser user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setOpenid(user.getOpenid());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());
        dto.setCity(user.getCity());
        dto.setCountry(user.getCountry());
        dto.setProvince(user.getProvince());
        return dto;
    }

    private List<UserAchievementDTO> buildAchievements(int checkinCount) {
        List<UserAchievementDTO> list = new ArrayList<>();
        list.add(newAchievement("FIRST_CHECKIN", "第一次打卡", "完成第一条打卡", checkinCount >= 1));
        list.add(newAchievement("FIVE_CHECKINS", "打卡爱好者", "累计打卡 5 次", checkinCount >= 5));
        list.add(newAchievement("TEN_CHECKINS", "打卡达人", "累计打卡 10 次", checkinCount >= 10));
        return list;
    }

    private List<UserAchievementDTO> buildBadges(int checkinCount) {
        List<UserAchievementDTO> list = new ArrayList<>();
        list.add(newAchievement("BADGE_CHECKIN_START", "非遗之旅开启", "完成第一次非遗相关打卡", checkinCount >= 1));
        list.add(newAchievement("BADGE_CHECKIN_LOVER", "城市探索者", "累计打卡 5 次", checkinCount >= 5));
        list.add(newAchievement("BADGE_CHECKIN_MASTER", "非遗守护者", "累计打卡 10 次", checkinCount >= 10));
        return list;
    }

    private UserAchievementDTO newAchievement(String code, String name, String description, boolean achieved) {
        UserAchievementDTO dto = new UserAchievementDTO();
        dto.setCode(code);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAchieved(achieved);
        return dto;
    }
}