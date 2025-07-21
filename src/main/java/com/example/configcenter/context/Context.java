package com.example.configcenter.context;

import lombok.Data;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Data
public class Context {
    String userEmail;
}
