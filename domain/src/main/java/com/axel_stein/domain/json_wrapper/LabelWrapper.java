package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;

import java.util.UUID;

public class LabelWrapper {

    private String id;

    private String title;

    private int order;

    private long views;

    private String createdDate;

    private String modifiedDate;

    private String driveId;

    public LabelWrapper(Label label) {
        id = label.getId();
        title = label.getTitle();
        order = label.getOrder();
        views = label.getViews();
        driveId = label.getDriveId();

        DateTime c = label.getCreatedDate();
        if (c != null) {
            createdDate = c.toString();
        }

        DateTime m = label.getModifiedDate();
        if (m != null) {
            modifiedDate = m.toString();
        }
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
