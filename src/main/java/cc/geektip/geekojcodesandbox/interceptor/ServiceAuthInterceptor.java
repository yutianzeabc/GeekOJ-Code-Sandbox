package cc.geektip.geekojcodesandbox.interceptor;

/**
 * @description: 服务鉴权拦截器
 * @author: Fish
 * @date: 2024/3/12
 */

import cc.geektip.geekojcodesandbox.config.CodeSandboxProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ServiceAuthInterceptor implements HandlerInterceptor {

    @Resource
    private CodeSandboxProperties codeSandboxProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String hServiceKey = request.getHeader("X-Service-Key");
        String xServiceKey = codeSandboxProperties.getXServiceKey();
        if (xServiceKey.equals(hServiceKey)) {
            return true;
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
