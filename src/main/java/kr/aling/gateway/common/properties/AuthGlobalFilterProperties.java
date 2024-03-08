package kr.aling.gateway.common.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * auth 설정을 위한 properties.
 *
 * @author : 여운석
 * @since : 1.0
 **/
@Getter
@Setter
@Component
public class AuthGlobalFilterProperties {
    @Value("${aling.auth.global-filter.excludes}")
    private List<String> globalExcludes = new ArrayList<>();
}
