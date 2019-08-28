package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.utils.TextUtil;

import java.util.UUID;

public class NotebookWrapper {
    private String id;
    private String title;

    public static NotebookWrapper fromNotebook(Notebook notebook) {
        NotebookWrapper wrapper = new NotebookWrapper();
        wrapper.id = notebook.getId();
        wrapper.title = notebook.getTitle();
        return wrapper;
    }

    public Notebook toNotebook() {
        Notebook notebook = new Notebook();
        notebook.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        notebook.setTitle(title);
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

}
