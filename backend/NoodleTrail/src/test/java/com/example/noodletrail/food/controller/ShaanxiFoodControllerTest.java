package com.example.noodletrail.food.controller;

import com.example.noodletrail.food.entity.ShaanxiFood;
import com.example.noodletrail.food.service.ShaanxiFoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ShaanxiFoodControllerTest {

    @Mock
    private ShaanxiFoodService shaanxiFoodService;

    @InjectMocks
    private ShaanxiFoodController shaanxiFoodController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearch() {
        // 准备测试数据
        List<Map<String, Object>> mockMapData = Arrays.asList(
            Map.of("id", 1, "foodName", "肉夹馍", "tag", "小吃"),
            Map.of("id", 2, "foodName", "凉皮", "tag", "小吃")
        );
        
        when(shaanxiFoodService.search(eq("肉夹馍"), any())).thenReturn(mockMapData);

        // 执行测试
        ResponseEntity<List<ShaanxiFood>> response = shaanxiFoodController.search("肉夹馍", null);

        // 验证结果
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(2, response.getBody().size());
        assertEquals("肉夹馍", response.getBody().get(0).getFoodName());
    }

    @Test
    void testSearchByName() {
        List<Map<String, Object>> mockMapData = Arrays.asList(
            Map.of("id", 1, "foodName", "羊肉泡馍", "tag", "主食")
        );
        
        when(shaanxiFoodService.search(eq("羊肉泡馍"), isNull())).thenReturn(mockMapData);

        ResponseEntity<List<ShaanxiFood>> response = shaanxiFoodController.searchByName("羊肉泡馍");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(1, response.getBody().size());
        assertEquals("羊肉泡馍", response.getBody().get(0).getFoodName());
    }

    @Test
    void testSearchByTag() {
        List<Map<String, Object>> mockMapData = Arrays.asList(
            Map.of("id", 1, "foodName", "肉夹馍", "tag", "小吃"),
            Map.of("id", 2, "foodName", "凉皮", "tag", "小吃")
        );
        
        when(shaanxiFoodService.search(isNull(), eq("小吃"))).thenReturn(mockMapData);

        ResponseEntity<List<ShaanxiFood>> response = shaanxiFoodController.searchByTag("小吃");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(2, response.getBody().size());
    }
}