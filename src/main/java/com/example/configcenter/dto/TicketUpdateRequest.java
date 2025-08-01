package com.example.configcenter.dto;

import com.example.configcenter.enums.TicketActionEnum;
import lombok.Data;

@Data
public class TicketUpdateRequest {
    TicketActionEnum action;
}
