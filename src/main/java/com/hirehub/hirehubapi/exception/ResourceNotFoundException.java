package com.hirehub.hirehubapi.exception;

import org.apache.catalina.valves.rewrite.ResolverImpl;

public class ResourceNotFoundException  extends RuntimeException{

    public ResourceNotFoundException(String message)
    {
        super(message);
    }
}
