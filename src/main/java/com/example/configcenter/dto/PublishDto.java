package com.example.configcenter.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublishDto {
    private List<String> ipList;
    private Long ticketId;
    private String action;
}
