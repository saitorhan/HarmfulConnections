package com.saitorhan.harmfulconnections;

import java.util.Date;

public class xmlInfo {
    String updated;
    String author;

    public xmlInfo(String updated, String author) {
        this.updated = updated;
        this.author = author;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
