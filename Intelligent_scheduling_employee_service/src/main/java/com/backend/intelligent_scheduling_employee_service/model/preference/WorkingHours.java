package com.backend.intelligent_scheduling_employee_service.model.preference;

import lombok.Data;

import java.util.List;

@Data
public class WorkingHours {
    private List<Integer> time;
}
