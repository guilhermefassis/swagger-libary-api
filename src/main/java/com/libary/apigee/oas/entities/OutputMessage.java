package com.libary.apigee.oas.entities;

import java.util.List;

public class OutputMessage {
    private String message;
    private List<String> content;


    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
