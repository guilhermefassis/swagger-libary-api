package com.libary.apigee.oas.entities;

import java.util.Map;

public class SwaggerSchema {
    private String title;
    private String version;
    private Map<String, String> items;


    public SwaggerSchema(String title, String version) {
        this.title = title;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getItems(){
        return  this.items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

}
