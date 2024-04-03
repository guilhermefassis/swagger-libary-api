package com.libary.apigee.oas.controllers;

import com.libary.apigee.oas.entities.OutputMessage;
import com.libary.apigee.oas.services.ISwaggerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
@RequestMapping("/swagger/library")
public class SwaggerController {
    private final ISwaggerServices services;

    @Autowired
    public SwaggerController(ISwaggerServices services) {
        this.services = services;
    }

    @GetMapping
    public ResponseEntity<OutputMessage> get(@RequestParam(name="keyword", required = false) String keyword) {
        OutputMessage message = new OutputMessage();
        try {
            if(keyword == null || keyword.equals("")) {
                message.setContent(services.returnAllOfDatabase());
            } else {
                message.setContent(services.searchInDatabase(keyword));
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.setMessage("Internal Server Error");
            message.setContent(new ArrayList<String>());
            return ResponseEntity.internalServerError().body(message);
        }
        message.setMessage("Success");
        return ResponseEntity.ok().body(message);
    }

    @PostMapping
    public ResponseEntity<OutputMessage> post(@RequestBody String swagger) {
        OutputMessage message = new OutputMessage();
        message.setContent(new ArrayList<String>());
        try {
            if(swagger == null || swagger.equals("")) {
                message.setMessage("Internal Server Error");
                return ResponseEntity.badRequest().body(message);
            } else {
                this.services.processSwagger(swagger);
                message.setMessage("Success");
                return ResponseEntity.ok().body(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.setMessage("Internal Server Error");
            return ResponseEntity.internalServerError().body(message);
        }
    }
}
