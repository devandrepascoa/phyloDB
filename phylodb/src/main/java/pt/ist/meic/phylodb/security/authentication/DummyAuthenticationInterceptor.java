package pt.ist.meic.phylodb.security.authentication;

import org.springframework.stereotype.Component;
import pt.ist.meic.phylodb.security.SecurityInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DummyAuthenticationInterceptor extends SecurityInterceptor {
    @Override
    public boolean handle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute(SecurityInterceptor.ID, "admin@gmail.com");
        req.setAttribute(SecurityInterceptor.PROVIDER, "phylodb");

        return true;
    }
}
