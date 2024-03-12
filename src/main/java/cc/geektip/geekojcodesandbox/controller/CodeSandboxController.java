package cc.geektip.geekojcodesandbox.controller;

import cc.geektip.geekojcodesandbox.impl.DockerCodeSandbox;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 代码沙箱控制器类
 * @author: Fish
 * @date: 2024/3/1
 */
@RestController("/")
public class CodeSandboxController {

    @Resource
    DockerCodeSandbox dockerCodeSandbox;

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@Valid @RequestBody ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        if (executeCodeResponse == null) {
            throw new RuntimeException("代码沙箱执行失败");
        }
        return executeCodeResponse;
    }
}
