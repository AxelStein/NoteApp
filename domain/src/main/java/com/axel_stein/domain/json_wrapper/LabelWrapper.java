package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.util.UUID;

public class LabelWrapper {

    private String id;

    private String title;

    private int order;

    private long views;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("modified_date")
    private String modifiedDate;

    @JsonProperty("drive_id")
    private String driveId;

    public static LabelWrapper fromLabel(Label label) {
        LabelWrapper wrapper = new LabelWrapper();
        wrapper.id = label.getId();
        wrapper.title = label.getTitle();
        wrapper.order = label.getOrder();
        wrapper.views = label.getViews();
        wrapper.driveId = label.getDriveId();

        DateTime c = label.getCreatedDate();
        if (c != null) {
            wrapper.createdDate = c.toString();
        }

        DateTime m = label.getModifiedDate();
        if (m != null) {
            wrapper.modifiedDate = m.toString();
        }
        return wrapper;
    }

    public Label toLabel() {
        Label label = new Label();
        label.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        label.setTitle(title);
        label.setOrder(order);
        label.setViews(views);
        label.setCreatedDate(TextUtil.isEmpty(createdDate) ? new DateTime() : DateTime.parse(createdDate));
        label.setModifiedDate(TextUtil.isEmpty(modifiedDate) ? new DateTime() : DateTime.parse(modifiedDate));
        return label;
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

}
