package com.libary.apigee.oas.services;

import com.libary.apigee.oas.entities.SwaggerSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SwaggerServices implements ISwaggerServices {
    private static Map<String, String> saveAllSchemaItems(OpenAPI openAPI, String schemaName) {
        Map<String, String> savedItems = new HashMap<>();
        Components components = openAPI.getComponents();
        Schema<?> schema = components.getSchemas().get(schemaName);

        if (schema != null) {
            saveSchemaItems(schema, savedItems);
        } else {
            System.out.println("Esquema não encontrado: " + schemaName);
        }

        return savedItems;
    }
    private static void saveSchemaItems(Schema<?> schema, Map<String, String> savedItems) {
        if (schema instanceof ArraySchema) {
            Schema<?> itemsSchema = ((ArraySchema) schema).getItems();
            if (itemsSchema != null) {
                saveSchemaItems(itemsSchema, savedItems);
            }
        } else if (schema instanceof ObjectSchema) {
            Map<String, Schema> properties = ((ObjectSchema) schema).getProperties();
            if (properties != null) {
                properties.forEach((propertyName, propertySchema) -> {
                    savedItems.put(propertyName, propertySchema.getDescription());
                    saveSchemaItems(propertySchema, savedItems);
                });
            }
        } else {
            System.out.println();
        }
    }
    public void processSwagger(String swaggerJson) {
        createTable();
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(swaggerJson).getOpenAPI();
        Map<String, String> items = new HashMap<>();
        Map<String, PathItem> paths = openAPI.getPaths();
        SwaggerSchema newSchema = new SwaggerSchema(openAPI.getInfo().getTitle(), openAPI.getInfo().getVersion());


        var components = openAPI.getComponents();
        var schemas = components.getSchemas();
        for(var schema : schemas.entrySet()) {
            items.putAll(saveAllSchemaItems(openAPI, schema.getKey()));
        }
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            PathItem pathItem = entry.getValue();
            Map<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> operations = pathItem.readOperationsMap();
            for (io.swagger.v3.oas.models.Operation operation : operations.values()) {
                List<Parameter> parameters = operation.getParameters();
                if (parameters != null) {
                    for (Parameter parameter : parameters) {
                        items.put(parameter.getName(), parameter.getDescription());
                    }
                }
            }
        }
        newSchema.setItems(items);
        if(!verifyDuplicatedSwagger(newSchema)) {
            storeInDatabase(newSchema);
        }
    }
    private static void createTable() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:swagger_data.db")) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS swagger_info (id INTEGER PRIMARY KEY, name TEXT, description TEXT, swaggerTitle TEXT, version TEXT)";
            conn.createStatement().executeUpdate(createTableSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void storeInDatabase(SwaggerSchema schema) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:swagger_data.db")) {
            String insertDataSql = "INSERT INTO swagger_info (name, description, swaggerTitle, version) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertDataSql)) {
                schema.getItems().forEach((name, description) -> {
                    try {
                        stmt.setString(1, name);
                        stmt.setString(2, description);
                        stmt.setString(3, schema.getTitle());
                        stmt.setString(4, schema.getVersion());
                        stmt.executeUpdate();
                    }catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> searchInDatabase(String keyword) {
        List<String> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:swagger_data.db")) {
            String searchSql = "SELECT * FROM swagger_info WHERE name LIKE ? OR description LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(searchSql)) {
                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    items.add("Name: " + rs.getString("name") + ", Description: " + rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    public  List<String> returnAllOfDatabase() {
        List<String> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:swagger_data.db")) {
            String searchSql = "SELECT * FROM swagger_info";
            try (PreparedStatement stmt = conn.prepareStatement(searchSql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    items.add("Name: " + rs.getString("name") + ", Description: " + rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    public static boolean verifyDuplicatedSwagger(SwaggerSchema schema) {
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:swagger_data.db")) {
            String searchApiInDatabase = "SELECT * FROM swagger_info WHERE swaggerTitle = ? AND version = ?";
            try(PreparedStatement stmt = conn.prepareStatement(searchApiInDatabase)) {
                stmt.setString(1, schema.getTitle());
                stmt.setString(2, schema.getVersion());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

