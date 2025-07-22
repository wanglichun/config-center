package com.example.configcenter.dto;

import lombok.Data;
import org.apache.catalina.LifecycleState;

import java.util.List;

@Data
public class PublishDto {
    private List<String> ipList;
}
