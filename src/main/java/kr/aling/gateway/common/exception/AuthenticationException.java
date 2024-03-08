package kr.aling.gateway.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 인증시 발생하는 예외입니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
public class AuthenticationException extends ResponseStatusException {
    private static final String MESSAGE = "Authentication Failed";

    public AuthenticationException(HttpStatus status, String reason) {
        super(status, MESSAGE + reason);
    }
}
