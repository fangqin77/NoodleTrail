package com.example.noodletrail.checklist.service;

import com.example.noodletrail.checklist.dto.ChecklistDTO;
import com.example.noodletrail.checklist.dto.ChecklistItemDTO;
import com.example.noodletrail.checklist.dto.ChecklistTemplateDTO;

import java.util.List;

public interface ChecklistService {

    List<ChecklistTemplateDTO> listTemplates();

    ChecklistTemplateDTO getTemplate(Long id);

    Long createChecklist(String userId, ChecklistDTO dto);

    Long importFromTemplate(String userId, Long templateId);

    List<ChecklistDTO> listChecklists(String userId);

    ChecklistDTO getChecklist(Long id, String userId);

    ChecklistDTO updateChecklist(Long id, String userId, ChecklistDTO dto);

    void deleteChecklist(Long id, String userId);

    ChecklistDTO addItem(Long checklistId, String userId, ChecklistItemDTO item);

    ChecklistDTO updateItem(Long checklistId, String userId, String itemId, ChecklistItemDTO item);

    ChecklistDTO deleteItem(Long checklistId, String userId, String itemId);
}