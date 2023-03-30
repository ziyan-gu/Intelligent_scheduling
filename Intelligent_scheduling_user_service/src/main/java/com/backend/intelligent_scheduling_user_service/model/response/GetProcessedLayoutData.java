package com.backend.intelligent_scheduling_user_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProcessedLayoutData {


    int start;

    int end;

    String id;

    String name;
}
