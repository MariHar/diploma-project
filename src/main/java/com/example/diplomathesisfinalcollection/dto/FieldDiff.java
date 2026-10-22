package com.example.diplomathesisfinalcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldDiff {
    private String fieldName;
    private String oldValue;
    private String newValue;
}