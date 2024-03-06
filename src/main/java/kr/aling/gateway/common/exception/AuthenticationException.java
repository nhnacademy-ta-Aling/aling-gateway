package kr.aling.gateway.common.exception;

/**
 * 인증시 발생하는 예외입니다.
 *
 * @author : 여운석
 * @since : 1.0
 **/
public class AuthenticationException extends RuntimeException {
    private static final String MESSAGE = "인증에 실패하였습니다.";

    public AuthenticationException() {
        super(MESSAGE);
    }
}
