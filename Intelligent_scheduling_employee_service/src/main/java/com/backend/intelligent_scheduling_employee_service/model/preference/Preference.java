package com.backend.intelligent_scheduling_employee_service.model.preference;

import lombok.Data;

@Data
public class Preference {
    private Workday workday;
    private WorkingHours working_hours;
    private ShiftDuration shift_duration;
}
