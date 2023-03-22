package com.backend.intelligent_scheduling_user_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class FixedRulesServiceImplTest {

    @Test
    void setDefaultValueFixed() throws JsonProcessingException {
        FixedRulesServiceImpl fixedRulesService = new FixedRulesServiceImpl();
        fixedRulesService.setDefaultValueFixed("2");
    }
}