package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

@Data
public class ModifyFixRulesRequest {
    private String ruleType;
    /**
     *
     */
    private String admin;

    /**
     *
     */
    private Object ruleValue;

    private static final long serialVersionUID = 1L;
}
