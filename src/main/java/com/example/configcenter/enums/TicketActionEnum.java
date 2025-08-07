package com.example.configcenter.enums;

import com.example.configcenter.exception.ParamException;

public enum TicketActionEnum {

    Approve("Approve"),
    Reject("Reject"),
    Cancel("Cancel"),
    Complete("Complete"),
    Publish("Publish");

    public final String value;

    TicketActionEnum(String value) {
        this.value = value;
    }

    public static TicketPhaseEnum getTargetPhase(TicketActionEnum actionEnum) {
        switch (actionEnum) {
            case Approve:
                return TicketPhaseEnum.GrayPublish;
            case Reject:
                return TicketPhaseEnum.Rejected;
            case Cancel:
                return TicketPhaseEnum.Cancelled;
            case Complete:
                return TicketPhaseEnum.Success;
            default:
                throw new ParamException(String.format("action【%s】is not support", actionEnum));
        }
    }
}
