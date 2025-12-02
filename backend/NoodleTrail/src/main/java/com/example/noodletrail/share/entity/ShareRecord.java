package com.example.noodletrail.share.entity;

import java.time.LocalDateTime;

public class ShareRecord {

    private Long id;
    private String userId;
    private String targetType;
    private Long targetId;
    private String shareChannel;
    private String shareTitle;
    private String shareDesc;
    private String shareCover;
    private LocalDateTime shareTime;
    private String ip;

    public ShareRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getShareChannel() {
        return shareChannel;
    }

    public void setShareChannel(String shareChannel) {
        this.shareChannel = shareChannel;
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public String getShareDesc() {
        return shareDesc;
    }

    public void setShareDesc(String shareDesc) {
        this.shareDesc = shareDesc;
    }

    public String getShareCover() {
        return shareCover;
    }

    public void setShareCover(String shareCover) {
        this.shareCover = shareCover;
    }

    public LocalDateTime getShareTime() {
        return shareTime;
    }

    public void setShareTime(LocalDateTime shareTime) {
        this.shareTime = shareTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
