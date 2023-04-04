package com.backend.intelligent_scheduling_user_service.model.response;

import lombok.Data;

@Data
public class GetPassengerFlowSum {

    String id;

    String name;

    Double PassengerFlowSum;
}
