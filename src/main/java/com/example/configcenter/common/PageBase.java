package com.example.configcenter.common;

import lombok.Data;

@Data
public class PageBase {
    private int pageNum = 1;
    private int pageSize = 20;
}
