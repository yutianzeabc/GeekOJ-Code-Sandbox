package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.config.CodeSandboxProperties;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @description: 代码沙箱管理类
 * @author: Bill Yu
 */
@Component
public class CodeSandboxManager {
    @Resource
    CodeSandboxRegistry codeSandboxRegistry;
    @Resource
    CodeSandboxProperties codeSandboxProperties;

    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        CodeSandbox codeSandbox = codeSandboxRegistry.getInstance(codeSandboxProperties.getJudgeMode());
        return codeSandbox.executeCode(executeCodeRequest);
    }
}
