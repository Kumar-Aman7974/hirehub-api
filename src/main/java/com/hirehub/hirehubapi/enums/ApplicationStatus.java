package com.hirehub.hirehubapi.enums;

public enum ApplicationStatus {

    PENDING("Application submitted, awaiting review"),
    REVIEWED("Application has been reviewed"),
    INTERVIEWING("Candidate is in interview process"),
    HIRED("Candidate has been hired"),
    REJECTED("Application has been rejected"),
    WITHDRAWN("Candidate withdrew application"),
    EXPIRED("Job posting expired");

    private final String description;

    ApplicationStatus(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    // check if status is terminal (no further changes)
    public boolean isTerminal() {
        return this == HIRED || this == REJECTED || this == WITHDRAWN;
    }

    // Check if status can be changed by job seeker
    public boolean canBeChangedByJobSeeker() {

        return this == PENDING;

    }

}
