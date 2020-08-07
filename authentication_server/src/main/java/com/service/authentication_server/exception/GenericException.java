package com.service.authentication_server.exception;

/**
 * Helper class to generate custom exceptions with increased modularity
 */
public class GenericException extends Exception{

    String error;

    /**
     * Returns new GenericException based on EntityType, ExceptionType and exception error message
     *
     * @param entityType The entity that produced the exception
     * @param exceptionType The type of exception
     * @param error The error message associated with the exception
     * @return CustomException
     */
    public static GenericException throwException(EntityType entityType, ExceptionType exceptionType, String error) {
        String messageTemplate = generateMessageTemplate(entityType, exceptionType);
        String messageVerbose = messageTemplate + "=" + error;
        return new GenericException(messageVerbose);
    }

    private GenericException(String error) {
        this.error = error;
    }

    private static String generateMessageTemplate(EntityType entityType, ExceptionType exceptionType){
        return entityType.name().concat(".").concat(exceptionType.getValue()).toLowerCase();
    }

    @Override
    public String getMessage(){
        return this.error;
    }

}
