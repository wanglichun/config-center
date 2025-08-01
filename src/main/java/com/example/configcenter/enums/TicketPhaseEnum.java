package com.example.configcenter.enums;

public enum TicketPhaseEnum {

    Reviewing("Reviewing"),
    GrayPublish("GrayPublish"),
    Success("Success"),
    Rejected("Rejected"),
    Cancelled("Cancelled");


    private String value;

    TicketPhaseEnum(String value) {
        this.value = value;
    }

}
