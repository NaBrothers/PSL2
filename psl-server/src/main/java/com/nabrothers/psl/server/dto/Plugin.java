package com.nabrothers.psl.server.dto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Plugin {
    private String name;
    private String info;
    private String author;
    private String path;
    private String packageName;

    public String getName() {
        return name;
    }

    public void setName(String name) throws UnsupportedEncodingException {
        this.name = new String(name.getBytes(StandardCharsets.ISO_8859_1), "UTF8");
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) throws UnsupportedEncodingException {
        this.info = new String(info.getBytes(StandardCharsets.ISO_8859_1), "UTF8");
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) throws UnsupportedEncodingException {
        this.author = new String(author.getBytes(StandardCharsets.ISO_8859_1), "UTF8");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "【插件】" + name + "\n"
                + info + "\n"
                + "【作者】" + author;
    }
}
