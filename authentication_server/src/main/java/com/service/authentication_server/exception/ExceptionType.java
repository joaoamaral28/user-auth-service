package com.service.authentication_server.exception;

public enum ExceptionType {

    NOT_FOUND_EXCEPTION("not.found"),
    REPOSITORY_FAIL_EXCEPTION("repository.fail"),
    REPOSITORY_STORE_EXCEPTION("repository.store.fail"),
    REPOSITORY_ACCESS_EXCEPTION("repository.access"),
    INVALID_PARAMETER_EXCEPTION("invalid.parameter"),
    INVALID_PASSWORD_EXCEPTION("invalid.password"),
    INVALID_EMAIL_EXCEPTION("invalid.email");
    // ...

    String value;

    ExceptionType(String value){
        this.value = value;
    }

    String getValue(){
        return this.value;
    }

}