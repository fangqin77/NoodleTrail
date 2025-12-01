package com.example.noodletrail.checklist.dto;

import java.util.List;

public class ChecklistTemplateDTO {

    private Long id;
    private String title;
    private String scene;
    private List<ChecklistItemDTO> items;

    public ChecklistTemplateDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public List<ChecklistItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ChecklistItemDTO> items) {
        this.items = items;
    }
}