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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        Checklist checklist = new Checklist();
        checklist.setUserId(userId);
        checklist.setTitle(dto.getTitle());
        checklist.setTemplateId(dto.getTemplateId());
        checklist.setItemsJson(toJson(dto.getItems()));
        LocalDateTime now = LocalDateTime.now();
        checklist.setCreateTime(now);
        checklist.setUpdateTime(now);
        checklistMapper.insert(checklist);
        return checklist.getId();
    }

    @Override
    public Long importFromTemplate(String userId, Long templateId) {
        ChecklistTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("清单模板不存在");
        }
        Checklist checklist = new Checklist();
        checklist.setUserId(userId);
        checklist.setTitle(template.getTitle());
        checklist.setTemplateId(template.getId());
        checklist.setItemsJson(template.getItemsJson());
        LocalDateTime now = LocalDateTime.now();
        checklist.setCreateTime(now);
        checklist.setUpdateTime(now);
        checklistMapper.insert(checklist);
        return checklist.getId();
    }

    @Override
    public List<ChecklistDTO> listChecklists(String userId) {
        List<Checklist> list = checklistMapper.selectByUser(userId);
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
        checklist.setTitle(dto.getTitle());
        checklist.setTemplateId(dto.getTemplateId());
        checklist.setItemsJson(toJson(dto.getItems()));
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
        dto.setTitle(checklist.getTitle());
        dto.setTemplateId(checklist.getTemplateId());
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
        if (items == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new RuntimeException("序列化清单条目失败", e);
        }
    }
}