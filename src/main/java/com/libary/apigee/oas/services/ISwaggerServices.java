package com.libary.apigee.oas.services;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

public interface ISwaggerServices {
    void processSwagger(String swaggerJson);
    List<String> searchInDatabase(String keyword);
    List<String> returnAllOfDatabase();
}
