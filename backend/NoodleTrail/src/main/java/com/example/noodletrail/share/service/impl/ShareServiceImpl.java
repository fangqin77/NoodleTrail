package com.example.noodletrail.share.service.impl;

import com.example.noodletrail.config.WxMaConfig;
import com.example.noodletrail.share.dto.ShareRecordDTO;
import com.example.noodletrail.share.dto.WechatSignVO;
import com.example.noodletrail.share.entity.ShareRecord;
import com.example.noodletrail.share.entity.WechatSignCache;
import com.example.noodletrail.share.mapper.ShareRecordMapper;
import com.example.noodletrail.share.mapper.WechatSignCacheMapper;
import com.example.noodletrail.share.service.ShareService;
import com.example.noodletrail.util.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareServiceImpl implements ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareServiceImpl.class);

    private final ShareRecordMapper shareRecordMapper;
    private final WechatSignCacheMapper signCacheMapper;
    private final WxMaConfig wxConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShareServiceImpl(ShareRecordMapper shareRecordMapper,
                            WechatSignCacheMapper signCacheMapper,
                            WxMaConfig wxConfig) {
        this.shareRecordMapper = shareRecordMapper;
        this.signCacheMapper = signCacheMapper;
        this.wxConfig = wxConfig;
    }

    @Override
    public void recordShare(String userId, ShareRecordDTO dto) {
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("未登录");
        }
        if (dto == null) {
            throw new RuntimeException("分享数据不能为空");
        }
        if (dto.getTargetId() == null) {
            throw new RuntimeException("分享内容ID不能为空");
        }
        if (dto.getTargetType() == null || dto.getTargetType().isBlank()) {
            throw new RuntimeException("分享内容类型不能为空");
        }
        if (dto.getShareChannel() == null || dto.getShareChannel().isBlank()) {
            throw new RuntimeException("分享渠道不能为空");
        }

        String targetType = dto.getTargetType().trim().toUpperCase();
        if (!"FOOD".equals(targetType) && !"HERITAGE".equals(targetType)) {
            throw new RuntimeException("分享类型仅支持 FOOD/HERITAGE");
        }
        String channel = dto.getShareChannel().trim().toUpperCase();
        if (!"WECHAT_FRIEND".equals(channel) && !"WECHAT_MOMENT".equals(channel)) {
            throw new RuntimeException("分享渠道仅支持 WECHAT_FRIEND/WECHAT_MOMENT");
        }

        String title = dto.getShareTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "分享内容";
        } else {
            title = title.trim();
        }

        ShareRecord record = new ShareRecord();
        record.setUserId(userId);
        record.setTargetType(targetType);
        record.setTargetId(dto.getTargetId());
        record.setShareChannel(channel);
        record.setShareTitle(title);
        record.setShareDesc(dto.getShareDesc());
        record.setShareCover(dto.getShareCover());
        record.setShareTime(LocalDateTime.now());
        record.setIp(dto.getIp());

        shareRecordMapper.insert(record);
    }

    @Override
    public WechatSignVO getWechatShareSign(String url) {
        if (url == null || url.isBlank()) {
            throw new RuntimeException("url 不能为空");
        }
        String ticket = getValidJsApiTicket();
        if (ticket == null || ticket.isBlank()) {
            throw new RuntimeException("获取微信 Ticket 失败");
        }
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String baseUrl = url.split("#")[0];

        String signStr = "jsapi_ticket=" + ticket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timestamp +
                "&url=" + baseUrl;

        String signature = sha1(signStr);

        WechatSignVO vo = new WechatSignVO();
        vo.setAppId(wxConfig.getAppid());
        vo.setNonceStr(nonceStr);
        vo.setTimestamp(timestamp);
        vo.setSignature(signature);
        return vo;
    }

    @Override
    public boolean hasShared(String userId, Long targetId, String targetType) {
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("未登录");
        }
        if (targetId == null) {
            throw new RuntimeException("targetId 不能为空");
        }
        if (targetType == null || targetType.isBlank()) {
            throw new RuntimeException("targetType 不能为空");
        }
        String type = targetType.trim().toUpperCase();
        int count = shareRecordMapper.countByUserAndTarget(userId, type, targetId);
        return count > 0;
    }

    private String getValidJsApiTicket() {
        String appid = wxConfig.getAppid();
        WechatSignCache cache = signCacheMapper.selectValidCache(appid);
        if (cache != null && cache.getTicket() != null && !cache.getTicket().isBlank()) {
            return cache.getTicket();
        }

        String secret = wxConfig.getSecret();
        String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" + appid + "&secret=" + secret;
        String tokenResp = HttpUtil.doGet(tokenUrl);
        try {
            JsonNode tokenNode = objectMapper.readTree(tokenResp);
            if (tokenNode.has("errcode") && tokenNode.get("errcode").asInt() != 0) {
                log.error("获取微信 access_token 失败: {}", tokenResp);
                throw new RuntimeException("获取微信 access_token 失败");
            }
            String accessToken = tokenNode.path("access_token").asText(null);
            int expiresIn = tokenNode.path("expires_in").asInt(7200);
            if (accessToken == null || accessToken.isBlank()) {
                log.error("获取微信 access_token 失败: {}", tokenResp);
                throw new RuntimeException("获取微信 access_token 失败");
            }

            String ticketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
                    + accessToken + "&type=jsapi";
            String ticketResp = HttpUtil.doGet(ticketUrl);
            JsonNode ticketNode = objectMapper.readTree(ticketResp);
            if (ticketNode.has("errcode") && ticketNode.get("errcode").asInt() != 0) {
                log.error("获取微信 jsapi_ticket 失败: {}", ticketResp);
                throw new RuntimeException("获取微信 jsapi_ticket 失败");
            }
            String ticket = ticketNode.path("ticket").asText(null);
            if (ticket == null || ticket.isBlank()) {
                log.error("获取微信 jsapi_ticket 失败: {}", ticketResp);
                throw new RuntimeException("获取微信 jsapi_ticket 失败");
            }

            int ttl = expiresIn > 200 ? expiresIn - 200 : expiresIn;
            LocalDateTime expireTime = LocalDateTime.now().plusSeconds(ttl);

            WechatSignCache newCache = new WechatSignCache();
            newCache.setAppid(appid);
            newCache.setAccessToken(accessToken);
            newCache.setTicket(ticket);
            newCache.setExpireTime(expireTime);
            newCache.setCreateTime(LocalDateTime.now());
            signCacheMapper.upsertCache(newCache);

            return ticket;
        } catch (Exception e) {
            log.error("获取微信 jsapi_ticket 异常", e);
            throw new RuntimeException("获取微信 jsapi_ticket 异常");
        }
    }

    private String sha1(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }
}
