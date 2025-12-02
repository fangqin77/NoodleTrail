package com.example.noodletrail.checklist.service;

import com.example.noodletrail.checklist.dto.ChecklistDTO;
import com.example.noodletrail.checklist.dto.ChecklistItemDTO;
import com.example.noodletrail.checklist.dto.ChecklistTemplateDTO;

import java.util.List;
import java.util.Map;

public interface ChecklistService {

    List<ChecklistTemplateDTO> listTemplates();

    ChecklistTemplateDTO getTemplate(Long id);

    Long createChecklist(String userId, ChecklistDTO dto);

    Long importFromTemplate(String userId, Long templateId, String date);

    List<ChecklistDTO> listChecklists(String userId, String date);

    ChecklistDTO getChecklist(Long id, String userId);

    ChecklistDTO updateChecklist(Long id, String userId, ChecklistDTO dto);

    void deleteChecklist(Long id, String userId);

    ChecklistDTO addItem(Long checklistId, String userId, ChecklistItemDTO item);

    ChecklistDTO updateItem(Long checklistId, String userId, String itemId, ChecklistItemDTO item);

    ChecklistDTO deleteItem(Long checklistId, String userId, String itemId);

    Map<String, Object> exportChecklist(Long id, String userId, String format);

    Map<String, Object> importChecklistFromText(String userId, String text, String date, String name);

    Map<String, Object> importItemsToChecklist(Long checklistId, String userId, String text);
}
