package com.ist.leave_management_system.model;

public enum LeaveTypeEnum {
    PTO("PTO"),
    SICK_LEAVE("SICK_LEAVE"),
    COMPASSIONATE_LEAVE("COMPASSIONATE_LEAVE"),
    MATERNITY_LEAVE("MATERNITY_LEAVE");

    private final String value;

    LeaveTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LeaveTypeEnum fromDisplayName(String name) {
        for (LeaveTypeEnum type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.getValue().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No leave type found for name: " + name);
    }
} 