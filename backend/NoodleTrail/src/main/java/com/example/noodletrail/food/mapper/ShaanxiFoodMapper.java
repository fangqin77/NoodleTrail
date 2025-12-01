package com.example.noodletrail.food.mapper;

import com.example.noodletrail.food.entity.ShaanxiFood;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShaanxiFoodMapper {

    // 按名称或标签查询，返回实体列表
    List<ShaanxiFood> searchByNameOrTag(@Param("name") String name,
                                        @Param("tag") String tag);

    // 按名称或标签查询，返回 Map 列表
    List<Map<String, Object>> searchByNameOrTagAsMap(@Param("name") String name,
                                                     @Param("tag") String tag);

    // 按 id / 名称 / 标签组合查询，返回实体列表
    List<ShaanxiFood> queryByConditions(@Param("id") Integer id,
                                        @Param("foodName") String foodName,
                                        @Param("tag") String tag);

    // 新增：按城市查询
    List<ShaanxiFood> selectByCity(@Param("city") String city);

    // 新增：按城市 + 标签查询
    List<ShaanxiFood> selectByCityAndTag(@Param("city") String city,
                                         @Param("tag") String tag);

    // 新增：按特色标签列表筛选（非遗美食、老字号、网红打卡、本地人推荐等）
    List<ShaanxiFood> selectByFeatureTags(@Param("featureTags") List<String> featureTags);

    // 获取所有基础标签
    List<String> findAllTags();

    // 获取所有原始特色标签（未切分）
    List<String> findAllFeatureTagsRaw();
}