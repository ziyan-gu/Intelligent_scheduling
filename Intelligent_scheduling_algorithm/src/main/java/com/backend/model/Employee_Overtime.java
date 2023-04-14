package com.backend.model;

import lombok.Data;

@Data
public class Employee_Overtime {
    private String employeeId;
    private int weekTime;
    private int mondayTime;
    private int tuesdayTime;
    private int wednesdayTime;
    private int thursdayTime;
    private int fridayTime;
    private int saturdayTime;
    private int sundayTime;
}
