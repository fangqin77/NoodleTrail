package com.example.noodletrail.share.controller;

import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.share.dto.ShareRecordDTO;
import com.example.noodletrail.share.dto.WechatSignDTO;
import com.example.noodletrail.share.dto.WechatSignVO;
import com.example.noodletrail.share.service.ShareService;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final ShareService shareService;
    private final WxUserService wxUserService;

    public ShareController(ShareService shareService, WxUserService wxUserService) {
        this.shareService = shareService;
        this.wxUserService = wxUserService;
    }

    /**
     * 获取微信分享签名
     */
    @PostMapping("/wechat/sign")
    public ApiResponse<WechatSignVO> getWechatSign(@RequestBody WechatSignDTO dto) {
        if (dto == null || dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new RuntimeException("url 不能为空");
        }
        WechatSignVO vo = shareService.getWechatShareSign(dto.getUrl());
        return ApiResponse.ok(vo);
    }

    /**
     * 记录分享行为
     */
    @PostMapping("/record")
    public ApiResponse<Map<String, Object>> recordShare(@RequestBody ShareRecordDTO dto,
                                                        HttpServletRequest request) {
        String userOpenid = getCurrentUserOpenid();
        if (dto != null && (dto.getIp() == null || dto.getIp().isBlank())) {
            dto.setIp(getClientIp(request));
        }
        shareService.recordShare(userOpenid, dto);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        return ApiResponse.ok(data);
    }

    /**
     * 判断是否分享过
     */
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkShare(@RequestParam("targetId") Long targetId,
                                                       @RequestParam("targetType") String targetType) {
        String userOpenid = getCurrentUserOpenid();
        boolean hasShared = shareService.hasShared(userOpenid, targetId, targetType);
        Map<String, Object> data = new HashMap<>();
        data.put("hasShared", hasShared);
        return ApiResponse.ok(data);
    }

    private String getCurrentUserOpenid() {
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
        return user.getOpenid();
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            if (idx > 0) {
                return ip.substring(0, idx).trim();
            }
            return ip.trim();
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
