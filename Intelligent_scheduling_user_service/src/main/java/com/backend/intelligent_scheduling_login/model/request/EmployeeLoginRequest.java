package com.backend.intelligent_scheduling_login.model.request;

import lombok.Data;

@Data
public class EmployeeLoginRequest {
    private static final long serialVersionUID = 1L;
    private String email;
    private String password;
}
