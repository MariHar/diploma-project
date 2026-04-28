package com.example.medicalweb.department;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "department")
public class Department {
    @Id
    private String id;
    private String name;
    private String description;
    private List<String> services = new ArrayList<>();

    public Department() {
    }

    public Department(String name) {
        this.name = name;
    }
    public Department(String id, String name, String description,String services) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.services = Arrays.asList(services);
    }

    public String  getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

}
