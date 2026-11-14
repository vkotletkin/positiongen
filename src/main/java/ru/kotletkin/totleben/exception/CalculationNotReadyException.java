package ru.kotletkin.totleben.exception;

import java.text.MessageFormat;

public class CalculationNotReadyException extends RuntimeException {

    public CalculationNotReadyException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }
}
