package com.example.diplomathesisfinalcollection.medicine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Document(collection = "medicines")
@Data
//@Builder
@NoArgsConstructor
//@AllArgsConstructor
public class Medicine {
    @Id
    private String id;
    private String name;
    private String category;
    private Double price;
    private Integer stock;
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    // 🔴 ՆՈՐ ԴԱՇՏ. Երբ է այս դեղը ստեղծվել համակարգում
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate addedDate;
//    @Builder.Default
    private boolean active = true;


}