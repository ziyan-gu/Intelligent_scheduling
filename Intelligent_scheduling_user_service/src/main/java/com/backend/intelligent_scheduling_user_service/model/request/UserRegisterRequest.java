package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wx"
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String account;

    private String password;

    private String name;

    private String checkPassword;

}
