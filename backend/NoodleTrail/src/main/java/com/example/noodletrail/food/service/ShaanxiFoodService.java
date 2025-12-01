package com.example.noodletrail.food.service;

import com.example.noodletrail.food.entity.ShaanxiFood;

import java.util.List;
import java.util.Map;

/**
 * 陕西美食服务接口
 */
public interface ShaanxiFoodService {

    /**
     * 根据名称或标签搜索美食，返回实体对象列表
     */
    List<ShaanxiFood> searchFoods(String name, String tag);

    /**
     * 根据名称或标签搜索美食，返回 Map 列表
     */
    List<Map<String, Object>> search(String name, String tag);

    /**
     * 按 id / 名称 / 标签组合查询，返回实体对象列表
     */
    List<ShaanxiFood> queryByConditions(Integer id, String foodName, String tag);

    /**
     * 按城市查询
     */
    List<ShaanxiFood> getByCity(String city);

    /**
     * 按城市 + 标签查询
     */
    List<ShaanxiFood> getByCityAndTag(String city, String tag);

    /**
     * 按特色标签筛选（非遗美食、老字号、网红打卡、本地人推荐等）
     */
    List<ShaanxiFood> getByFeatureTags(List<String> featureTags);

    /**
     * 获取所有基础标签（tag 列）
     */
    List<String> getAllTags();

    /**
     * 获取展开后的所有特色标签（feature_tags 列切分去重）
     */
    List<String> getAllFeatureTags();
}