package com.example.noodletrail.checklist.service.impl;

import com.example.noodletrail.checklist.dto.ChecklistDTO;
import com.example.noodletrail.checklist.dto.ChecklistItemDTO;
import com.example.noodletrail.checklist.dto.ChecklistTemplateDTO;
import com.example.noodletrail.checklist.entity.Checklist;
import com.example.noodletrail.checklist.entity.ChecklistTemplate;
import com.example.noodletrail.checklist.mapper.ChecklistMapper;
import com.example.noodletrail.checklist.mapper.ChecklistTemplateMapper;
import com.example.noodletrail.checklist.service.ChecklistService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ChecklistServiceImpl implements ChecklistService {

    private final ChecklistTemplateMapper templateMapper;
    private final ChecklistMapper checklistMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChecklistServiceImpl(ChecklistTemplateMapper templateMapper,
                                ChecklistMapper checklistMapper) {
        this.templateMapper = templateMapper;
        this.checklistMapper = checklistMapper;
    }

    @Override
    public List<ChecklistTemplateDTO> listTemplates() {
        List<ChecklistTemplate> templates = templateMapper.selectAll();
        List<ChecklistTemplateDTO> result = new ArrayList<>();
        for (ChecklistTemplate t : templates) {
            result.add(toTemplateDTO(t));
        }
        return result;
    }

    @Override
    public ChecklistTemplateDTO getTemplate(Long id) {
        ChecklistTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return null;
        }
        return toTemplateDTO(template);
    }

    @Override
    public Long createChecklist(String userId, ChecklistDTO dto) {
        // 标题必填校验，前端只要传入标题即可创建清单，其余字段后端补默认值
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("清单标题不能为空");
        }

        String dateStr = normalizeDate(dto.getDate());
        Integer order = resolveOrder(userId, dateStr, dto.getOrder(), null);

        Checklist checklist = new Checklist();
        checklist.setUserId(userId);
        checklist.setTitle(dto.getName().trim());
        checklist.setTemplateId(dto.getTemplateId());
        checklist.setItemsJson(toJson(dto.getItems()));
        checklist.setDate(dateStr);
        checklist.setOrderIndex(order);
        LocalDateTime now = LocalDateTime.now();
        checklist.setCreateTime(now);
        checklist.setUpdateTime(now);
        checklistMapper.insert(checklist);
        return checklist.getId();
    }

    @Override
    public Long importFromTemplate(String userId, Long templateId, String date) {
        ChecklistTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("清单模板不存在");
        }
        String dateStr = normalizeDate(date);
        Integer order = resolveOrder(userId, dateStr, null, null);

        Checklist checklist = new Checklist();
        checklist.setUserId(userId);
        checklist.setTitle(template.getTitle());
        checklist.setTemplateId(template.getId());
        checklist.setItemsJson(template.getItemsJson());
        checklist.setDate(dateStr);
        checklist.setOrderIndex(order);
        LocalDateTime now = LocalDateTime.now();
        checklist.setCreateTime(now);
        checklist.setUpdateTime(now);
        checklistMapper.insert(checklist);
        return checklist.getId();
    }

    @Override
    public List<ChecklistDTO> listChecklists(String userId, String date) {
        List<Checklist> list;
        if (date != null && !date.isBlank()) {
            list = checklistMapper.selectByUserAndDate(userId, date.trim());
        } else {
            list = checklistMapper.selectByUser(userId);
        }
        List<ChecklistDTO> result = new ArrayList<>();
        for (Checklist c : list) {
            result.add(toChecklistDTO(c));
        }
        return result;
    }

    @Override
    public ChecklistDTO getChecklist(Long id, String userId) {
        Checklist checklist = checklistMapper.selectByIdAndUser(id, userId);
        if (checklist == null) {
            return null;
        }
        return toChecklistDTO(checklist);
    }

    @Override
    public ChecklistDTO updateChecklist(Long id, String userId, ChecklistDTO dto) {
        Checklist checklist = checklistMapper.selectByIdAndUser(id, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }

        if (dto.getName() != null) {
            checklist.setTitle(dto.getName());
        }
        if (dto.getTemplateId() != null) {
            checklist.setTemplateId(dto.getTemplateId());
        }
        if (dto.getItems() != null) {
            checklist.setItemsJson(toJson(dto.getItems()));
        }

        String newDate = dto.getDate() != null ? normalizeDate(dto.getDate()) : checklist.getDate();
        Integer newOrder = dto.getOrder() != null ? dto.getOrder() : checklist.getOrderIndex();
        Integer resolvedOrder = resolveOrder(userId, newDate, newOrder, id);

        checklist.setDate(newDate);
        checklist.setOrderIndex(resolvedOrder);
        checklist.setUpdateTime(LocalDateTime.now());
        checklistMapper.updateByIdAndUser(checklist);
        return toChecklistDTO(checklist);
    }

    @Override
    public void deleteChecklist(Long id, String userId) {
        int rows = checklistMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new RuntimeException("清单不存在或无权限");
        }
    }

    @Override
    public ChecklistDTO addItem(Long checklistId, String userId, ChecklistItemDTO item) {
        Checklist checklist = checklistMapper.selectByIdAndUser(checklistId, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }
        List<ChecklistItemDTO> items = parseItems(checklist.getItemsJson());
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        if (item.getChecked() == null) {
            item.setChecked(Boolean.FALSE);
        }
        items.add(item);
        checklist.setItemsJson(toJson(items));
        checklist.setUpdateTime(LocalDateTime.now());
        checklistMapper.updateByIdAndUser(checklist);
        return toChecklistDTO(checklist);
    }

    @Override
    public ChecklistDTO updateItem(Long checklistId, String userId, String itemId, ChecklistItemDTO item) {
        Checklist checklist = checklistMapper.selectByIdAndUser(checklistId, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }
        List<ChecklistItemDTO> items = parseItems(checklist.getItemsJson());
        for (ChecklistItemDTO it : items) {
            if (itemId.equals(it.getId())) {
                if (item.getContent() != null) {
                    it.setContent(item.getContent());
                }
                if (item.getChecked() != null) {
                    it.setChecked(item.getChecked());
                }
                if (item.getNote() != null) {
                    it.setNote(item.getNote());
                }
                break;
            }
        }
        checklist.setItemsJson(toJson(items));
        checklist.setUpdateTime(LocalDateTime.now());
        checklistMapper.updateByIdAndUser(checklist);
        return toChecklistDTO(checklist);
    }

    @Override
    public ChecklistDTO deleteItem(Long checklistId, String userId, String itemId) {
        Checklist checklist = checklistMapper.selectByIdAndUser(checklistId, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }
        List<ChecklistItemDTO> items = parseItems(checklist.getItemsJson());
        items.removeIf(it -> itemId.equals(it.getId()));
        checklist.setItemsJson(toJson(items));
        checklist.setUpdateTime(LocalDateTime.now());
        checklistMapper.updateByIdAndUser(checklist);
        return toChecklistDTO(checklist);
    }

    @Override
    public Map<String, Object> exportChecklist(Long id, String userId, String format) {
        Checklist checklist = checklistMapper.selectByIdAndUser(id, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }
        ChecklistDTO dto = toChecklistDTO(checklist);
        String text = buildExportText(dto);

        String fmt = (format == null || format.isBlank()) ? "both" : format.toLowerCase();
        Map<String, Object> result = new HashMap<>();

        if ("text".equals(fmt) || "both".equals(fmt) || "image".equals(fmt)) {
            if ("text".equals(fmt) || "both".equals(fmt)) {
                result.put("text", text);
            }
            if ("image".equals(fmt) || "both".equals(fmt)) {
                String base64 = generateImageBase64(text);
                result.put("imageBase64", base64);
                result.put("imageUrl", null);
            }
        } else {
            result.put("text", text);
        }

        return result;
    }

    @Override
    public Map<String, Object> importChecklistFromText(String userId, String text, String date, String name) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("导入内容不能为空");
        }
        String[] lines = text.split("\\r?\\n");
        List<String> errors = new ArrayList<>();
        String titleFromText = null;
        List<ChecklistItemDTO> items = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int importedCount = 0;
        int duplicateCount = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (titleFromText == null && !isItemLine(line)) {
                titleFromText = line;
                continue;
            }
            ParseItemResult parsed = parseItemLine(line);
            if (parsed == null || parsed.content == null || parsed.content.isBlank()) {
                errors.add("无法解析行: " + rawLine);
                continue;
            }
            String key = parsed.content.trim();
            if (seen.contains(key)) {
                duplicateCount++;
                continue;
            }
            seen.add(key);
            ChecklistItemDTO item = new ChecklistItemDTO();
            item.setId(UUID.randomUUID().toString());
            item.setContent(parsed.content.trim());
            item.setChecked(parsed.checked);
            item.setNote(null);
            items.add(item);
            importedCount++;
        }

        String finalName;
        if (titleFromText != null && !titleFromText.isBlank()) {
            finalName = titleFromText.trim();
        } else if (name != null && !name.isBlank()) {
            finalName = name.trim();
        } else {
            finalName = "清单";
        }

        ChecklistDTO dto = new ChecklistDTO();
        dto.setName(finalName);
        dto.setDate(date);
        dto.setOrder(null);
        dto.setTemplateId(null);
        dto.setItems(items);

        Long checklistId = createChecklist(userId, dto);
        Checklist checklist = checklistMapper.selectByIdAndUser(checklistId, userId);
        ChecklistDTO resultChecklist = toChecklistDTO(checklist);

        Map<String, Object> result = new HashMap<>();
        result.put("checklist", resultChecklist);
        result.put("importedCount", importedCount);
        result.put("duplicateCount", duplicateCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    public Map<String, Object> importItemsToChecklist(Long checklistId, String userId, String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("导入内容不能为空");
        }
        Checklist checklist = checklistMapper.selectByIdAndUser(checklistId, userId);
        if (checklist == null) {
            throw new RuntimeException("清单不存在或无权限");
        }
        List<ChecklistItemDTO> existingItems = parseItems(checklist.getItemsJson());
        Set<String> seen = new HashSet<>();
        for (ChecklistItemDTO item : existingItems) {
            if (item.getContent() != null) {
                seen.add(item.getContent().trim());
            }
        }

        String[] lines = text.split("\\r?\\n");
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int duplicateCount = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            ParseItemResult parsed = parseItemLine(line);
            if (parsed == null || parsed.content == null || parsed.content.isBlank()) {
                errors.add("无法解析行: " + rawLine);
                continue;
            }
            String key = parsed.content.trim();
            if (seen.contains(key)) {
                duplicateCount++;
                continue;
            }
            seen.add(key);
            ChecklistItemDTO item = new ChecklistItemDTO();
            item.setId(UUID.randomUUID().toString());
            item.setContent(parsed.content.trim());
            item.setChecked(parsed.checked);
            item.setNote(null);
            existingItems.add(item);
            importedCount++;
        }

        checklist.setItemsJson(toJson(existingItems));
        checklist.setUpdateTime(LocalDateTime.now());
        checklistMapper.updateByIdAndUser(checklist);

        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", importedCount);
        result.put("duplicateCount", duplicateCount);
        result.put("errors", errors);
        return result;
    }

    private ChecklistTemplateDTO toTemplateDTO(ChecklistTemplate template) {
        ChecklistTemplateDTO dto = new ChecklistTemplateDTO();
        dto.setId(template.getId());
        dto.setTitle(template.getTitle());
        dto.setScene(template.getScene());
        dto.setItems(parseItems(template.getItemsJson()));
        return dto;
    }

    private ChecklistDTO toChecklistDTO(Checklist checklist) {
        ChecklistDTO dto = new ChecklistDTO();
        dto.setId(checklist.getId());
        dto.setName(checklist.getTitle());
        dto.setTemplateId(checklist.getTemplateId());
        dto.setDate(checklist.getDate());
        dto.setOrder(checklist.getOrderIndex());
        dto.setItems(parseItems(checklist.getItemsJson()));
        return dto;
    }

    private List<ChecklistItemDTO> parseItems(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChecklistItemDTO>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("解析清单条目失败", e);
        }
    }

    private String toJson(List<ChecklistItemDTO> items) {
        if (items == null || items.isEmpty()) {
            // 数据库存储上使用 "[]" 表示空清单，避免 NOT NULL 约束冲突
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new RuntimeException("序列化清单条目失败", e);
        }
    }

    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now().toString();
        }
        return date.trim();
    }

    private Integer resolveOrder(String userId, String date, Integer requestedOrder, Long ignoreId) {
        List<Checklist> existing = checklistMapper.selectByUserAndDate(userId, date);
        if (ignoreId != null) {
            existing.removeIf(c -> ignoreId.equals(c.getId()));
        }
        if (existing.size() >= 3 && (requestedOrder == null || requestedOrder < 1 || requestedOrder > 3)) {
            throw new RuntimeException("同一天最多只能创建3个清单");
        }

        boolean[] used = new boolean[4];
        for (Checklist c : existing) {
            Integer idx = c.getOrderIndex();
            if (idx != null && idx >= 1 && idx <= 3) {
                used[idx] = true;
            }
        }

        if (requestedOrder != null && requestedOrder >= 1 && requestedOrder <= 3) {
            if (used[requestedOrder]) {
                throw new RuntimeException("该日期的该顺序已存在清单，请选择其他顺序");
            }
            return requestedOrder;
        }

        for (int i = 1; i <= 3; i++) {
            if (!used[i]) {
                return i;
            }
        }
        throw new RuntimeException("同一天最多只能创建3个清单");
    }

    private String buildExportText(ChecklistDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getName() != null && !dto.getName().isBlank()) {
            sb.append(dto.getName());
        } else {
            sb.append("清单");
        }
        if (dto.getDate() != null && !dto.getDate().isBlank()) {
            sb.append(" - ").append(dto.getDate());
        }
        sb.append(System.lineSeparator());
        List<ChecklistItemDTO> items = dto.getItems();
        if (items != null) {
            int index = 1;
            for (ChecklistItemDTO item : items) {
                if (item.getContent() == null || item.getContent().isBlank()) {
                    continue;
                }
                sb.append(index++).append(". ");
                sb.append(Boolean.TRUE.equals(item.getChecked()) ? "[x] " : "[ ] ");
                sb.append(item.getContent());
                if (item.getNote() != null && !item.getNote().isBlank()) {
                    sb.append(" (").append(item.getNote()).append(")");
                }
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String generateImageBase64(String text) {
        try {
            String[] lines = text.split("\\r?\\n");
            int width = 800;
            int lineHeight = 30;
            int height = Math.max(200, (lines.length + 2) * lineHeight);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
            int y = 40;
            for (String line : lines) {
                g2d.drawString(line, 40, y);
                y += lineHeight;
            }
            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isItemLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") ||
                trimmed.startsWith("[ ]") || trimmed.startsWith("[x]") || trimmed.startsWith("[X]")) {
            return true;
        }
        int i = 0;
        while (i < trimmed.length() && Character.isDigit(trimmed.charAt(i))) {
            i++;
        }
        if (i > 0 && i < trimmed.length() && (trimmed.charAt(i) == '.' || trimmed.charAt(i) == '、')) {
            return true;
        }
        return false;
    }

    private ParseItemResult parseItemLine(String line) {
        String trimmed = line.trim();
        boolean checked = false;
        String content = trimmed;

        if (trimmed.startsWith("[x]") || trimmed.startsWith("[X]")) {
            checked = true;
            content = trimmed.substring(3).trim();
        } else if (trimmed.startsWith("[ ]")) {
            content = trimmed.substring(3).trim();
        } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            content = trimmed.substring(2).trim();
        } else {
            int i = 0;
            while (i < trimmed.length() && Character.isDigit(trimmed.charAt(i))) {
                i++;
            }
            if (i > 0 && i < trimmed.length() && (trimmed.charAt(i) == '.' || trimmed.charAt(i) == '、')) {
                content = trimmed.substring(i + 1).trim();
            }
        }
        if (content.isEmpty()) {
            return null;
        }
        return new ParseItemResult(content, checked);
    }

    private static class ParseItemResult {
        final String content;
        final boolean checked;

        ParseItemResult(String content, boolean checked) {
            this.content = content;
            this.checked = checked;
        }
    }
}
