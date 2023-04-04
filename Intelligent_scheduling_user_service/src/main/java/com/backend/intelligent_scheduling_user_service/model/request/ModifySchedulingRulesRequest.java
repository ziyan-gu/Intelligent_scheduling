package com.backend.intelligent_scheduling_user_service.model.request;

import lombok.Data;

@Data
public class ModifySchedulingRulesRequest {
    private String ruleType;
    /**
     *
     */
    private String store;

    /**
     *
     */
    private Object ruleValue;

    private static final long serialVersionUID = 1L;
}
