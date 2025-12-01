package com.example.noodletrail.checklist.controller;

import com.example.noodletrail.checklist.dto.ChecklistDTO;
import com.example.noodletrail.checklist.dto.ChecklistItemDTO;
import com.example.noodletrail.checklist.dto.ChecklistTemplateDTO;
import com.example.noodletrail.checklist.service.ChecklistService;
import com.example.noodletrail.common.ApiResponse;
import com.example.noodletrail.user.entity.WxUser;
import com.example.noodletrail.user.service.WxUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private final ChecklistService checklistService;
    private final WxUserService wxUserService;

    public ChecklistController(ChecklistService checklistService, WxUserService wxUserService) {
        this.checklistService = checklistService;
        this.wxUserService = wxUserService;
    }

    @GetMapping("/templates")
    public ApiResponse<List<ChecklistTemplateDTO>> templates() {
        List<ChecklistTemplateDTO> list = checklistService.listTemplates();
        return ApiResponse.ok(list);
    }

    @GetMapping("/templates/{id}")
    public ApiResponse<ChecklistTemplateDTO> templateDetail(@PathVariable Long id) {
        ChecklistTemplateDTO dto = checklistService.getTemplate(id);
        return ApiResponse.ok(dto);
    }

    @GetMapping
    public ApiResponse<List<ChecklistDTO>> myChecklists() {
        String userOpenid = getCurrentUserOpenid();
        List<ChecklistDTO> list = checklistService.listChecklists(userOpenid);
        return ApiResponse.ok(list);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody ChecklistDTO dto) {
        String userOpenid = getCurrentUserOpenid();
        Long id = checklistService.createChecklist(userOpenid, dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    @PostMapping("/import-from-template")
    public ApiResponse<Map<String, Object>> importFromTemplate(@RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        Object tid = body.get("templateId");
        if (tid == null) {
            throw new RuntimeException("templateId 不能为空");
        }
        Long templateId = Long.parseLong(tid.toString());
        Long id = checklistService.importFromTemplate(userOpenid, templateId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return ApiResponse.ok(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<ChecklistDTO> detail(@PathVariable Long id) {
        String userOpenid = getCurrentUserOpenid();
        ChecklistDTO dto = checklistService.getChecklist(id, userOpenid);
        return ApiResponse.ok(dto);
    }

    @PutMapping("/{id}")
    public ApiResponse<ChecklistDTO> update(@PathVariable Long id, @RequestBody ChecklistDTO dto) {
        String userOpenid = getCurrentUserOpenid();
        ChecklistDTO updated = checklistService.updateChecklist(id, userOpenid, dto);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        String userOpenid = getCurrentUserOpenid();
        checklistService.deleteChecklist(id, userOpenid);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/items")
    public ApiResponse<ChecklistDTO> addItem(@PathVariable Long id, @RequestBody ChecklistItemDTO item) {
        String userOpenid = getCurrentUserOpenid();
        ChecklistDTO dto = checklistService.addItem(id, userOpenid, item);
        return ApiResponse.ok(dto);
    }

    @PutMapping("/{id}/items/{itemId}")
    public ApiResponse<ChecklistDTO> updateItem(@PathVariable Long id,
                                                @PathVariable String itemId,
                                                @RequestBody ChecklistItemDTO item) {
        String userOpenid = getCurrentUserOpenid();
        ChecklistDTO dto = checklistService.updateItem(id, userOpenid, itemId, item);
        return ApiResponse.ok(dto);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ApiResponse<ChecklistDTO> deleteItem(@PathVariable Long id,
                                                @PathVariable String itemId) {
        String userOpenid = getCurrentUserOpenid();
        ChecklistDTO dto = checklistService.deleteItem(id, userOpenid, itemId);
        return ApiResponse.ok(dto);
    }

    private String getCurrentUserOpenid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        Integer userId;
        if (principal instanceof Long l) {
            userId = l.intValue();
        } else if (principal instanceof Integer i) {
            userId = i;
        } else {
            userId = Integer.parseInt(principal.toString());
        }
        WxUser user = wxUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getOpenid();
    }
}