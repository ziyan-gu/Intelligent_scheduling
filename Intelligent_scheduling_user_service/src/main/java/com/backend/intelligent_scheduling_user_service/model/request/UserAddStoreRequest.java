package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

@Data
public class UserAddStoreRequest {
    private static final long serialVersionUID = 1L;

    private String company;
    private String address;
    private String account;
    private String name;
    private Float size;
}
