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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    public ApiResponse<List<ChecklistDTO>> myChecklists(@RequestParam(value = "date", required = false) String date) {
        String userOpenid = getCurrentUserOpenid();
        List<ChecklistDTO> list = checklistService.listChecklists(userOpenid, date);
        return ApiResponse.ok(list);
    }

    /**
     * 获取用户的所有清单（不按日期筛选，支持简单分页）
     */
    @GetMapping("/all")
    public ApiResponse<List<ChecklistDTO>> getAllMyChecklists(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "100") int size) {
        String userOpenid = getCurrentUserOpenid();
        List<ChecklistDTO> all = checklistService.listChecklists(userOpenid, null);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 100;
        }
        int fromIndex = (page - 1) * size;
        if (fromIndex >= all.size()) {
            // 返回空列表
            return ApiResponse.ok(all.subList(all.size(), all.size()));
        }
        int toIndex = Math.min(fromIndex + size, all.size());
        List<ChecklistDTO> pageList = all.subList(fromIndex, toIndex);
        return ApiResponse.ok(pageList);
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
        String date = null;
        Object d = body.get("date");
        if (d != null) {
            date = d.toString();
        }
        Long id = checklistService.importFromTemplate(userOpenid, templateId, date);
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

    @PostMapping("/{id}/export")
    public ApiResponse<Map<String, Object>> exportChecklist(@PathVariable Long id,
                                                            @RequestBody(required = false) Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        String format = null;
        if (body != null && body.get("format") != null) {
            format = body.get("format").toString();
        }
        Map<String, Object> data = checklistService.exportChecklist(id, userOpenid, format);
        return ApiResponse.ok(data);
    }

    @PostMapping("/import-from-text")
    public ApiResponse<Map<String, Object>> importFromText(@RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        Object textObj = body.get("text");
        if (textObj == null) {
            throw new RuntimeException("text 不能为空");
        }
        String text = textObj.toString();
        String date = body.get("date") != null ? body.get("date").toString() : null;
        String name = body.get("name") != null ? body.get("name").toString() : null;
        Map<String, Object> data = checklistService.importChecklistFromText(userOpenid, text, date, name);
        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/import-items")
    public ApiResponse<Map<String, Object>> importItems(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        Object textObj = body.get("text");
        if (textObj == null) {
            throw new RuntimeException("text 不能为空");
        }
        String text = textObj.toString();
        Map<String, Object> data = checklistService.importItemsToChecklist(id, userOpenid, text);
        return ApiResponse.ok(data);
    }

    /**
     * 生成清单分享码
     * POST /api/checklists/{id}/share
     */
    @PostMapping("/{id}/share")
    public ApiResponse<Map<String, Object>> createShareCode(@PathVariable Long id) {
        String userOpenid = getCurrentUserOpenid();
        // 导出为文本格式
        Map<String, Object> exported = checklistService.exportChecklist(id, userOpenid, "text");
        Object textObj = exported.get("text");
        if (textObj == null) {
            throw new RuntimeException("清单导出失败");
        }
        String text = textObj.toString();
        // 使用 URL-safe Base64 作为分享码，不带 padding
        String code = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        // 当前方案不做过期时间控制，前端文档中的 expireAt 保留为 null
        data.put("expireAt", null);
        return ApiResponse.ok(data);
    }

    /**
     * 通过分享码导入清单
     * POST /api/checklists/import-from-share
     */
    @PostMapping("/import-from-share")
    public ApiResponse<Map<String, Object>> importFromShare(@RequestBody Map<String, Object> body) {
        String userOpenid = getCurrentUserOpenid();
        Object codeObj = body.get("code");
        if (codeObj == null) {
            throw new RuntimeException("code 不能为空");
        }
        String code = codeObj.toString();
        String date = body.get("date") != null ? body.get("date").toString() : null;

        String text;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(code);
            text = new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("分享码无效");
        }

        Map<String, Object> result = checklistService.importChecklistFromText(userOpenid, text, date, null);
        return ApiResponse.ok(result);
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
