package com.example.noodletrail.heritage.controller;

import com.example.noodletrail.heritage.entity.ShaanxiIntangibleHeritage;
import com.example.noodletrail.heritage.service.ShaanxiIntangibleHeritageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/heritages")
public class ShaanxiIntangibleHeritageController {

    private final ShaanxiIntangibleHeritageService heritageService;

    public ShaanxiIntangibleHeritageController(ShaanxiIntangibleHeritageService heritageService) {
        this.heritageService = heritageService;
    }

    // 1. 基础功能：返回所有非遗信息
    @GetMapping
    public ResponseEntity<List<ShaanxiIntangibleHeritage>> getAll() {
        List<ShaanxiIntangibleHeritage> list = heritageService.getAll();
        return ResponseEntity.ok(list);
    }

    // 2. 分类展示：按非遗分类查询
    @GetMapping("/type")
    public ResponseEntity<List<ShaanxiIntangibleHeritage>> getByType(@RequestParam String heritageType) {
        List<ShaanxiIntangibleHeritage> list = heritageService.getByType(heritageType);
        return ResponseEntity.ok(list);
    }

    // 3. 模糊搜索：按关键词模糊匹配名称 / 历史渊源 / 内容介绍
    @GetMapping("/search")
    public ResponseEntity<List<ShaanxiIntangibleHeritage>> searchByKeyword(@RequestParam String keyword) {
        List<ShaanxiIntangibleHeritage> list = heritageService.searchByKeyword(keyword);
        return ResponseEntity.ok(list);
    }
}
