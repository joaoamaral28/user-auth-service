package com.service.authentication_server.exception;


import com.service.authentication_server.exception.GenericException.GenericException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options){

        System.out.println("getErrorAttributes()");

        Map<String, Object> map = super.getErrorAttributes(request, options);

        Throwable throwable = getError(request);

        if(throwable instanceof GenericException){
            GenericException exception = (GenericException) throwable;
            map.put("message", exception.getMessage());
        }
        //map.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        //map.put("message", "placeholder message");
        return map;
    }

}
