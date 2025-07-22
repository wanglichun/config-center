package com.example.configcenter.entity;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class MachineInstance {
    private String ip;
    private Long version;
    private String status;
    private String configValue;
    private Long lastUpdateTime;
}
