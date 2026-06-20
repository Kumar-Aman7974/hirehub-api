package com.hirehub.hirehubapi.enums;

public enum FileCategory {

    RESUME("resume"),
    COMPANY_LOGO("logos"),
    PROFILE_PICTURE("profiles"),
    COVER_LETTER("cover-letters"),
    ATTACHMENT("attachments");

    private final String directory;

    FileCategory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
