package com.backend.intelligent_scheduling_employee_service.model.request;

import lombok.Data;

@Data
public class EmployeeNewAddRequest {
    private static final long serialVersionUID = 1L;

    private String name;

    private String email;

    private Integer position;

}
