package com.backend.intelligent_scheduling_user_service.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum Week {
    MONDAY("星期一"),
    TUESDAY("星期二"),
    WEDNESDAY("星期三"),
    THURSDAY("星期四"),
    FRIDAY("星期五"),
    SATURDAY("星期六"),
    SUNDAY("星期日");

    private final String name;

    private Week(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Week fromDayOfWeek(DayOfWeek dayOfWeek) {
        return Week.valueOf(dayOfWeek.toString());
    }

    public static Week fromLocalDate(LocalDate date) {
        return fromDayOfWeek(date.getDayOfWeek());
    }

    public static Week fromString(String name) {
        for (Week week : Week.values()) {
            if (week.name.equalsIgnoreCase(name)) {
                return week;
            }
        }
        throw new IllegalArgumentException("Invalid week name: " + name);
    }

    @Override
    public String toString() {
        return name;
    }


}

