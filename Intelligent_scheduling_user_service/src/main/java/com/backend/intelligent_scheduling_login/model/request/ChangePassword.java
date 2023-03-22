package com.backend.intelligent_scheduling_login.model.request;

import lombok.Data;

@Data
public class ChangePassword {

    private String account;
    private String password;
    private String newPassword;;
}
