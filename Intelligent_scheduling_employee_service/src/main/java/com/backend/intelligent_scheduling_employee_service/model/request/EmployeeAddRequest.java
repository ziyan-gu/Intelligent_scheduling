package com.backend.intelligent_scheduling_employee_service.model.request;


import lombok.Data;

@Data
public class EmployeeAddRequest {
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String email;

    private Integer position;

    private String store;
}
