package com.example.noodletrail.checklist.dto;

import java.util.List;

public class ChecklistDTO {

    private Long id;
    /**
     * 清单名称（标题）
     */
    private String name;
    /**
     * 清单日期（YYYY-MM-DD）
     */
    private String date;
    /**
     * 当天顺序（1-3）
     */
    private Integer order;
    private Long templateId;
    private List<ChecklistItemDTO> items;

    public ChecklistDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public List<ChecklistItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ChecklistItemDTO> items) {
        this.items = items;
    }
}
