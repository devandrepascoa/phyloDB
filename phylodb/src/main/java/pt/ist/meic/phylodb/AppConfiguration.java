package pt.ist.meic.phylodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pt.ist.meic.phylodb.security.authentication.AuthenticationInterceptor;
import pt.ist.meic.phylodb.security.authentication.DummyAuthenticationInterceptor;
import pt.ist.meic.phylodb.security.authorization.AuthorizationInterceptor;

/**
 * Configuration of the application
 */
@Configuration
public class AppConfiguration implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AuthorizationInterceptor authorizationInterceptor;
    private final DummyAuthenticationInterceptor dummyAuthenticationInterceptor;

    public AppConfiguration(AuthenticationInterceptor authenticationInterceptor,
                            AuthorizationInterceptor authorizationInterceptor,
                            DummyAuthenticationInterceptor dummyAuthenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.authorizationInterceptor = authorizationInterceptor;
        this.dummyAuthenticationInterceptor = dummyAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dummyAuthenticationInterceptor);
    }

}
