package com.example.configcenter.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PublishDto {
    @NotEmpty
    private List<String> ipList;
    @NotNull
    private Long ticketId;
    @NotNull
    private String action;
}
