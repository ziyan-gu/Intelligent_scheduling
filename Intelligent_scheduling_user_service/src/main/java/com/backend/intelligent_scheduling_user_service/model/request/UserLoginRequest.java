package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private static final long serialVersionUID = 1L;
    private String account;
    private String password;
}
