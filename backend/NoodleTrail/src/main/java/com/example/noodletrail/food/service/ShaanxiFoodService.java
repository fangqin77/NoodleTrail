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
}
