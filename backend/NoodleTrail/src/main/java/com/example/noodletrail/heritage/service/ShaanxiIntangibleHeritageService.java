package com.example.noodletrail.heritage.service;

import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;

import java.util.List;

public interface ShaanxiIntangibleHeritageService {

    // 返回所有非遗信息
    List<ShaanxiIntangibleHeritage> getAll();

    // 按非遗分类查询
    List<ShaanxiIntangibleHeritage> getByType(String heritageType);

    // 模糊搜索（名称 / 历史渊源 / 内容介绍）
    List<ShaanxiIntangibleHeritage> searchByKeyword(String keyword);

    // 根据主键查询单条非遗信息
    ShaanxiIntangibleHeritage getById(Integer id);
}