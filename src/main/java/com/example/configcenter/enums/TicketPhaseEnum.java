package com.example.configcenter.enums;

public enum TicketPhaseEnum {

    Reviewing("Reviewing"),
    Processing("Processing"),
    Success("Success"),
    Rejected("Rejected"),
    Cancelled("Cancelled");


    private String value;

    TicketPhaseEnum(String value) {
        this.value = value;
    }

}
