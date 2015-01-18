package com.discover.step.ex;

/**
 * Created by Geri on 2015.01.18..
 */
public class DefaultStepException extends Exception  {

    String message;

    public DefaultStepException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
