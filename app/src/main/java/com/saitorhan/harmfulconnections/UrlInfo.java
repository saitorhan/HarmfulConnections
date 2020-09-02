package com.saitorhan.harmfulconnections;

import java.util.Date;

public class UrlInfo {
    String _id;
    String url;
    String desc;
    String source;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public UrlInfo() {

    }

    public UrlInfo(String _id, String url, String desc, String source) {

        this._id = _id;
        this.url = url;
        this.desc = desc;
        this.source = source;
    }
}
