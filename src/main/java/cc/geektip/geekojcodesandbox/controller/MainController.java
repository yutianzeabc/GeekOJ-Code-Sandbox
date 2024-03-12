package cc.geektip.geekojcodesandbox.controller;

import cc.geektip.geekojcodesandbox.langspec.java.JavaDockerCodeSandbox;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Fish
 * @date: 2024/3/1
 */
@RestController("/")
public class MainController {

    @Resource
    JavaDockerCodeSandbox javaDockerCodeSandbox;

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        return new ExecuteCodeResponse();
    }
}
