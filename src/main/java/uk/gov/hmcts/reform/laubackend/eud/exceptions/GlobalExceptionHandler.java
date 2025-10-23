package uk.gov.hmcts.reform.laubackend.eud.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex,
                                              HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad Request");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("errors", fieldErrors(ex));
        return pd;
    }

    private Map<String, String> fieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return errors;
    }

    @ExceptionHandler(InvalidServiceAuthorizationException.class)
    public ProblemDetail handleInvalidAuth(InvalidServiceAuthorizationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Forbidden");
        pd.setDetail("Invalid service authorization");
        pd.setProperty("path", request.getRequestURI());
        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("reason", "Service token is missing or invalid");
        pd.setProperty("errors", errorInfo);
        return pd;
    }

}
