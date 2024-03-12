package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.langspec.cpp.CppDockerCodeSandbox;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cn.hutool.core.io.resource.ResourceUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * @description: JavaDockerCodeSandboxTest
 * @author: Fish
 * @date: 2024/3/9
 */
@SpringBootTest
@Slf4j
class CppDockerCodeSandboxTest {

    @Resource
    private CppDockerCodeSandbox cppDockerCodeSandbox;

    @Test
    void executeCode() {
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(List.of("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("cpp");
        cppDockerCodeSandbox.executeCode(executeCodeRequest);
    }
}