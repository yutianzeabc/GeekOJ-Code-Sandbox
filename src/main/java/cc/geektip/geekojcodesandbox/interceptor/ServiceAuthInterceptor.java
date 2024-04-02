package cc.geektip.geekojcodesandbox.interceptor;

/**
 * @description: 服务鉴权拦截器
 * @author: Fish
 * @date: 2024/3/12
 */

import cc.geektip.geekojcodesandbox.config.CodeSandboxProperties;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeResponse;
import cc.geektip.geekojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class ServiceAuthInterceptor implements HandlerInterceptor {
    @Resource
    private CodeSandboxProperties codeSandboxProperties;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String hServiceKey = request.getHeader("X-Service-Key");
        String xServiceKey = codeSandboxProperties.getXServiceKey();
        if (xServiceKey.equals(hServiceKey)) {
            return true;
        } else {
            // 构建响应结果
            ExecuteCodeResponse executeCodeResponse = ExecuteCodeResponse.builder()
                    .code(ExecuteCodeStatusEnum.NO_AUTH.getValue())
                    .msg(ExecuteCodeStatusEnum.NO_AUTH.getMsg())
                    .build();
            // 设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            // 将响应结果转换为 JSON 格式并写入响应体
            String jsonResponse = objectMapper.writeValueAsString(executeCodeResponse);
            response.getWriter().write(jsonResponse);
            return false;
        }
    }
}
