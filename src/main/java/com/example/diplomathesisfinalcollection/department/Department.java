package com.example.diplomathesisfinalcollection.department;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departments")
public class Department {
    @Id
    private String id;
    private String code;
    private String name;
    private String description;

    // List of images with status
    @Builder.Default
    private List<DepartmentImage> images = new ArrayList<>();

    private String location;
    private String contactPhone;
    private String iconSvg;

    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private boolean isPubliclyBookable = true;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartmentImage {
        private String publicId;
        private boolean active = true;

        // This is not saved in DB, used only for the UI display
        private transient String url;
    }
}