package com.example.configcenter.entity;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class MachineInstance extends BaseEntity{
    private String instanceName;
    private Set<String> instanceIp;
    private long registerTime = System.currentTimeMillis();
    private long lastHeartbeat = System.currentTimeMillis();
}
