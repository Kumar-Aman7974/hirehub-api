package com.hirehub.hirehubapi.enums;

public enum JobType {

    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    FREELANCE("Freelance"),
    INTERNSHIP("Internship"),
    REMOTE("Remote"),
    HYBRID("Hybrid");


    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

}
