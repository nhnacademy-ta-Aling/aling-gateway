package kr.aling.gateway.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 인가 실패시 발생하는 예외입니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
public class AuthorizationException extends ResponseStatusException {
    private static final String MESSAGE = "Authorization Failed: ";

    public AuthorizationException(HttpStatus status, String reason) {
        super(status, MESSAGE + reason);
    }
}
