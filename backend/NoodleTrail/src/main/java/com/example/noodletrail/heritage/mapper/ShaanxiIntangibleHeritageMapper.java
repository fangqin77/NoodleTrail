package com.example.noodletrail.heritage.mapper;

import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShaanxiIntangibleHeritageMapper {

    // 查询所有非遗信息
    List<ShaanxiIntangibleHeritage> findAll();

    // 按非遗分类查询
    List<ShaanxiIntangibleHeritage> findByType(@Param("heritageType") String heritageType);

    // 模糊搜索（名称 / 历史渊源 / 内容介绍）
    List<ShaanxiIntangibleHeritage> searchByKeyword(@Param("keyword") String keyword);
}
