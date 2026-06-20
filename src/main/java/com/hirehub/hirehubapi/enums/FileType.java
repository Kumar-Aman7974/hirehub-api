package com.hirehub.hirehubapi.enums;

import lombok.Getter;

import java.io.File;

@Getter
public enum FileType {
    // Image
    JPEG("image/jpeg", new String[]{"jpg", "jpeg"}),
    PNG("image/png", new String[]{"pgn"}),
    GIF("image/gif", new String[]{"gif"}),
    WEBP("image/webp", new String[]{"webp"}),

    //Documents
    PDF("application/pdf", new String[]{"pdf"}),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new String[]{"docx"}),
    DOC("application/msword", new String[]{"doc"}),
    TXT("text/plain", new String[]{"txt"}),
    RTF("application/rtf", new String[]{"rtf"}),

    // Archives
    ZIP("application/zip", new String[]{"zip"}),
    RAR("application/x-rar-compressed", new String[]{"rar"});

    private final String mimeType;
    private final String[] extensions;

    FileType(String mimeType, String[] extensions)
    {
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    public  static  FileType fromMimeType(String mimeType) {

        for (FileType type : values())
        {
            if(type.mimeType.equalsIgnoreCase(mimeType)) {
                return type;
            }
        }
        return null;

    }

    public static FileType fromExtension(String extension) {
        for (FileType type : values()) {
            for (String ext : type.extensions) {
                if (ext.equalsIgnoreCase(extension)) {
                    return type;
                }
            }
        }
        return null;
    }

    public boolean isImage() {
        return this == JPEG || this == PNG || this == GIF || this == WEBP;
    }

    public boolean isDocument() {
        return this == PDF || this == DOCX || this == DOC || this == TXT || this == RTF;
    }


}
