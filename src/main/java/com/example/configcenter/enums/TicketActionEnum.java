package com.example.configcenter.enums;

public enum TicketActionEnum {

    Approve("Approve"),
    Reject("Reject"),
    Cancel("Cancel"),
    Complete("Complete");

    private String value;

    TicketActionEnum(String value) {
        this.value = value;
    }
}
