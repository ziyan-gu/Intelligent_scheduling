package com.backend.intelligent_scheduling_user_service.model.response;

import lombok.Data;

@Data
public class LoginInfo {

    private String[] roles;
    private String Avatar;
    private String Name;
}
