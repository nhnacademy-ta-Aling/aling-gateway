package kr.aling.gateway.config;

import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;

/**
 * Open Feign 사용을 위한 Config.<br>
 *
 * @author 정유진
 * @since 1.0
 **/
public class OpenFeignConfig {

    /**
     * json 통신을 위한 decoder 빈.
     *
     * @return Decoder 빈
     */
    @Bean
    public Decoder decoder() {
        return new JacksonDecoder();
    }

    /**
     * json 통신을 위한 decoder 빈.
     *
     * @return Encoder 빈
     */
    @Bean
    public Encoder encoder() {
        return new JacksonEncoder();
    }

    /**
     * Feign 커넥션 옵션 설정.
     *
     * @return 커넥션 옵션
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, false);
    }
}
