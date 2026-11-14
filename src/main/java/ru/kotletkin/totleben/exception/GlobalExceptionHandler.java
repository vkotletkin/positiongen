package ru.kotletkin.totleben.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kotletkin.totleben.exception.dto.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERR_DESCRIPTION_EMPTY = "empty";

    private static OffsetDateTime timestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageException.class)
    public ErrorResponse handleOnStorageException(final StorageException e) {
        log.error(e.getMessage());
        return new ErrorResponse(timestamp(), e.getMessage(), ERR_DESCRIPTION_EMPTY);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CalculationNotReadyException.class)
    public ErrorResponse handleOnCalculationNotReadyException(final CalculationNotReadyException e) {
        log.error(e.getMessage());
        return new ErrorResponse(timestamp(), "Calculation is not ready", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleOnDataIntegrityViolationException(final DataIntegrityViolationException e) {
        String cause = e.getMostSpecificCause().getMessage();
        log.error("Data integrity violation: {}", cause);
        if (cause != null && (cause.toLowerCase().contains("unique") || cause.toLowerCase().contains("duplicate"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                    timestamp(),
                    "Database constraint violation",
                    ERR_DESCRIPTION_EMPTY));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                timestamp(),
                "Database constraint violation",
                ERR_DESCRIPTION_EMPTY));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleOnNotFoundException(final NotFoundException e) {
        log.error(e.getMessage());
        return new ErrorResponse(timestamp(), "Object not found", e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(ConstraintViolationException.class)
    public List<ErrorResponse> handleOnConstraintValidationException(final ConstraintViolationException e) {

        OffsetDateTime now = timestamp();
        final List<ErrorResponse> errorResponses = e.getConstraintViolations().stream()
                .map(error -> new ErrorResponse(
                                now,
                                error.getPropertyPath().toString(),
                                error.getMessage()
                        )
                )
                .toList();

        log.error(e.getMessage());

        return errorResponses;
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<ErrorResponse> handleOnMethodArgumentNotValidException(final MethodArgumentNotValidException e) {

        OffsetDateTime now = timestamp();
        final List<ErrorResponse> errorResponses = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse(now, error.getField(), error.getDefaultMessage()))
                .toList();

        log.error(e.getMessage());

        return errorResponses;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handleOnMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error(e.getMessage());
        return new ErrorResponse(timestamp(), e.getParameterName(), "Required request parameter is missing");
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ErrorResponse handleOnMissingRequestHeaderException(final MissingRequestHeaderException e) {
        log.error(e.getMessage());
        return new ErrorResponse(
                timestamp(), "Ошибка передачи заголовка." + e.getHeaderName(), ERR_DESCRIPTION_EMPTY);
    }
}
