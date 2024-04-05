package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.impl.DockerAcmCodeSandbox;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeRequest;
import cn.hutool.core.io.resource.ResourceUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * @description: DockerAcmCodeSandboxTest
 * @author: Fish
 * @date: 2024/3/9
 */
@SpringBootTest
@Slf4j
class DockerAcmCodeSandboxTest {

    @Resource
    private DockerAcmCodeSandbox dockerAcmCodeSandbox;

    @Test
    void executeCodeJava() {
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(List.of("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        dockerAcmCodeSandbox.executeCode(executeCodeRequest);
    }

    @Test
    void executeCodeCpp() {
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(List.of("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("cpp");
        dockerAcmCodeSandbox.executeCode(executeCodeRequest);
    }

    @Test
    void executeCodeGo() {
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(List.of("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.go", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("go");
        dockerAcmCodeSandbox.executeCode(executeCodeRequest);
    }
}