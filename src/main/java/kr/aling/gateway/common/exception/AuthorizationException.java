package kr.aling.gateway.common.exception;

/**
 * 인가 실패시 발생하는 예외입니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
public class AuthorizationException extends RuntimeException {
    private static final String MESSAGE = "인가에 실패하였습니다.";

    public AuthorizationException() {
        super(MESSAGE);
    }
}
