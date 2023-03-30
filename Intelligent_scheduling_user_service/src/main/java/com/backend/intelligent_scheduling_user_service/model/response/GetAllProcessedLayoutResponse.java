package com.backend.intelligent_scheduling_user_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllProcessedLayoutResponse {

    Date date;

    List<GetProcessedLayoutData> getProcessedLayoutData;

}
