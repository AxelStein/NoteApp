package com.axel_stein.domain.model;

import org.joda.time.DateTime;

// todo remove
public class Resource implements Cloneable {

    private String id;

    private String title;

    private String content;

    private DateTime createdDate;

    private DateTime modifiedDate;

    private String driveId;

    private long views;

    /* */

    private String notebookId;

    private boolean trashed;

    private DateTime trashedDate;

    private boolean pinned;

    private boolean starred;



}
