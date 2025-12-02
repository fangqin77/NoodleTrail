package com.example.noodletrail.heritage.entity;

import java.time.LocalDateTime;

public class HeritageFavorite {

    private Long id;
    private String userId;
    private Integer heritageId;
    private LocalDateTime createTime;

    public HeritageFavorite() {
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

    public Integer getHeritageId() {
        return heritageId;
    }

    public void setHeritageId(Integer heritageId) {
        this.heritageId = heritageId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
