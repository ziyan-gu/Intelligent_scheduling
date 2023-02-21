package com.backend.model;

import lombok.Data;

@Data
public class Employee {
    private String id;
    private String name;
    private String email;
    private String position;
    private String store;
    private String preferenceValue;
}
