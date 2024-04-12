package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.enums.JudgeModeEnum;
import cc.geektip.geekojcodesandbox.impl.DockerAcmCodeSandbox;
import cc.geektip.geekojcodesandbox.impl.DockerArgsCodeSandbox;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 代码沙箱注册中心
 * @author: Bill Yu
 *
 */
@Component
public class CodeSandboxRegistry {

    Map<String, CodeSandbox> codeSandboxMap = new ConcurrentHashMap<>();
    @Resource
    DockerAcmCodeSandbox dockerAcmCodeSandbox;
    @Resource
    DockerArgsCodeSandbox dockerArgsCodeSandbox;

    @PostConstruct
    public void init() {
        codeSandboxMap.put(JudgeModeEnum.ACM.getValue(), dockerAcmCodeSandbox);
        codeSandboxMap.put(JudgeModeEnum.ARGS.getValue(), dockerArgsCodeSandbox);
    }

    public CodeSandbox getInstance(String type) {
        if (codeSandboxMap.containsKey(type)) {
            return codeSandboxMap.get(type);
        } else {
            throw new RuntimeException("非法的评测模式：" + type + "，目前仅支持 acm 与 args 两种评测模式！");
        }
    }
}
