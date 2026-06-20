package com.hirehub.hirehubapi.exception;

public class FileSizeExceededException extends RuntimeException{

    public FileSizeExceededException(String  message)
    {
        super(message);
    }

}
