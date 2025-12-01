package com.example.noodletrail.heritage.dto;

public class HeritageDTO {

    private Integer id;
    private String name;
    private String category;
    private String imageUrl;
    private String videoUrl;
    private String historicalOrigin;
    private String contentIntroduction;

    public HeritageDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getHistoricalOrigin() {
        return historicalOrigin;
    }

    public void setHistoricalOrigin(String historicalOrigin) {
        this.historicalOrigin = historicalOrigin;
    }

    public String getContentIntroduction() {
        return contentIntroduction;
    }

    public void setContentIntroduction(String contentIntroduction) {
        this.contentIntroduction = contentIntroduction;
    }
}