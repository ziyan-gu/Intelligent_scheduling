package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

import java.sql.Date;

@Data
public class GetSchedulingRequest {

    private static final long serialVersionUID = 1L;

    private String id;

    private Date date;
}
