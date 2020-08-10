package com.service.authentication_server.exception.GenericException;

/**
 * Helper class to generate custom exceptions with increased modularity
 */
public class GenericException extends Exception{

    String error;

    /**
     * Returns new GenericException based on EntityType, ExceptionType and exception error message
     *
     * @param genericExceptionEntityType The entity that produced the exception
     * @param genericExceptionType The type of exception
     * @param error The error message associated with the exception
     * @return CustomException
     */
    public static GenericException throwException(GenericExceptionEntityType genericExceptionEntityType, GenericExceptionType genericExceptionType, String error) {
        String messageTemplate = generateMessageTemplate(genericExceptionEntityType, genericExceptionType);
        String messageVerbose = messageTemplate + "=" + error;
        return new GenericException(messageVerbose);
    }

    private GenericException(String error) {
        this.error = error;
    }

    private static String generateMessageTemplate(GenericExceptionEntityType genericExceptionEntityType, GenericExceptionType genericExceptionType){
        return genericExceptionEntityType.name().concat(".").concat(genericExceptionType.getValue()).toLowerCase();
    }

    @Override
    public String getMessage(){
        return this.error;
    }

}
