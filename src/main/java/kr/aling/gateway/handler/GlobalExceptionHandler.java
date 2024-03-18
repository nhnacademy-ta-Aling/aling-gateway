package kr.aling.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이 내부에서 발생하는 예외를 전역으로 처리하기 위한 핸들러.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * HTTP Status 를 지정하여 예외를 처리합니다. 현재 처리하는 예외: {@link ResponseStatusException}, {@link FeignException}
     *
     * @param exchange the current exchange
     * @param ex       the exception to handle
     * @return 처리된 response
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;

            response.setStatusCode(rse.getStatus());
            return writeReason(response, rse.getReason());
        }

        if (ex instanceof FeignException) {
            FeignException fe = (FeignException) ex;

            response.setStatusCode(HttpStatus.valueOf(fe.status()));
            return writeReason(response, fe.getMessage());
        }

        return Mono.error(ex);
    }

    /**
     * 예외 메시지를 담아서 보내줍니다.
     *
     * @param response ServerHttpResponse of exchange
     * @param reason   why occur exception
     * @return 메지시를 포함한 Mono
     */
    private Mono<Void> writeReason(ServerHttpResponse response, String reason) {
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();

            try {
                return bufferFactory.wrap(objectMapper.writeValueAsString(reason).getBytes());
            } catch (Exception e) {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
