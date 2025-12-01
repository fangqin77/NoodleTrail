package com.example.noodletrail.food.entity;

public class ShaanxiFood {
    private Integer id;
    private String foodName;
    private String imageUrl;
    private String history;
    private String introduction;
    private String features;
    private String tag;
    private String city; // 新增城市字段
    private String featureTags; // 新增：特色标签（非遗美食、老字号、网红打卡、本地人推荐）

    public ShaanxiFood() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFeatureTags() {
        return featureTags;
    }

    public void setFeatureTags(String featureTags) {
        this.featureTags = featureTags;
    }
}
