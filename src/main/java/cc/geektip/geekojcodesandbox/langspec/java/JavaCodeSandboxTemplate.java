package cc.geektip.geekojcodesandbox.langspec.java;

import cc.geektip.geekojcodesandbox.CodeSandboxTemplate;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;
import cc.geektip.geekojcodesandbox.model.ExecuteMessage;

import java.io.File;
import java.util.List;

/**
 * @description:
 * @author: Fish
 * @date: 2024/3/11
 */
public abstract class JavaCodeSandboxTemplate extends CodeSandboxTemplate {

    @Override
    protected String language() {
        return "java";
    }

    @Override
    protected abstract ExecuteMessage compile(File codeFile);

    @Override
    protected abstract List<ExecuteMessage> run(File codeFile, List<String> inputList);

    @Override
    protected abstract ExecuteCodeResponse buildResp(List<ExecuteMessage> executeMessageList);

}
