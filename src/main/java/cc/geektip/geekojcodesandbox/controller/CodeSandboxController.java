package cc.geektip.geekojcodesandbox.controller;

import cc.geektip.geekojcodesandbox.CodeSandboxManager;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 代码沙箱控制器类
 * @author: Bill Yu
 * @date: 2024/3/1
 */
@RestController
public class CodeSandboxController {
    @Resource
    CodeSandboxManager codeSandboxManager;

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@Valid @RequestBody ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = codeSandboxManager.executeCode(executeCodeRequest);
        if (executeCodeResponse == null) {
            throw new RuntimeException("代码沙箱执行失败");
        }
        return executeCodeResponse;
    }
}