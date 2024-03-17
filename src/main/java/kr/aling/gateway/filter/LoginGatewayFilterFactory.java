package kr.aling.gateway.filter;

import kr.aling.gateway.filter.LoginGatewayFilterFactory.Config;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class LoginGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return null;
    }

    public static class Config {

    }
}
