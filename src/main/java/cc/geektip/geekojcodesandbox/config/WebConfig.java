package cc.geektip.geekojcodesandbox.config;

import cc.geektip.geekojcodesandbox.interceptor.ServiceAuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description: WebConfig
 * @author: Fish
 * @date: 2024/3/12
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private ServiceAuthInterceptor serviceAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serviceAuthInterceptor);
    }
}