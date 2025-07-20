package com.example.configcenter.entity;

import lombok.Data;

import java.util.List;

@Data
public class MachineInstance extends BaseEntity{
    private String instanceName;
    private List<String> instanceIp;
    private long lastHeartbeat;
}
