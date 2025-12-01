package com.example.noodletrail.heritage.experience.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HeritageExperienceDTO {

    private Long id;
    private Integer heritageId;
    private String title;
    private String content;
    private Integer rating;
    private String locationName;
    private Double longitude;
    private Double latitude;
    private List<String> images;
    private LocalDateTime createTime;

    public HeritageExperienceDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getHeritageId() {
        return heritageId;
    }

    public void setHeritageId(Integer heritageId) {
        this.heritageId = heritageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}