package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.util.UUID;

public class NotebookWrapper {

    private String id;

    private String title;

    private String color;

    private int order;

    private long views;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("modified_date")
    private String modifiedDate;

    @JsonProperty("drive_id")
    private String driveId;

    public static NotebookWrapper fromNotebook(Notebook notebook) {
        NotebookWrapper wrapper = new NotebookWrapper();
        wrapper.id = notebook.getId();
        wrapper.title = notebook.getTitle();
        wrapper.color = notebook.getColor();
        wrapper.order = notebook.getOrder();
        wrapper.views = notebook.getViews();
        wrapper.driveId = notebook.getDriveId();

        DateTime c = notebook.getCreatedDate();
        if (c != null) {
            wrapper.createdDate = c.toString();
        }

        DateTime m = notebook.getModifiedDate();
        if (m != null) {
            wrapper.modifiedDate = m.toString();
        }
        return wrapper;
    }

    public Notebook toNotebook() {
        Notebook notebook = new Notebook();
        notebook.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        notebook.setTitle(title);
        notebook.setColor(color);
        notebook.setOrder(order);
        notebook.setViews(views);
        notebook.setCreatedDate(TextUtil.isEmpty(createdDate) ? new DateTime() : DateTime.parse(createdDate));
        notebook.setModifiedDate(TextUtil.isEmpty(modifiedDate) ? new DateTime() : DateTime.parse(modifiedDate));
        return notebook;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
