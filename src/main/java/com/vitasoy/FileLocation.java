package com.vitasoy;

public class FileLocation {

    private String name;

    private String url;

    private String filePath;

    public FileLocation(String name, String url, String filePath) {
        this.name = name;
        this.url = url;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
