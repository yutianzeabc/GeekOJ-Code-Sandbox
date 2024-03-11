package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.config.CodeSandboxProperties;
import cc.geektip.geekojcodesandbox.config.LangSpecSetting;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;
import cc.geektip.geekojcodesandbox.model.ExecuteMessage;
import cc.geektip.geekojcodesandbox.model.JudgeInfo;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 代码沙箱模板类，用于定义代码沙箱的基本流程，包括保存代码、编译代码、运行代码、构建响应、清理代码等
 * @author: Fish
 * @date: 2024/3/11
 */
@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {

    @Resource
    protected CodeSandboxProperties codeSandboxProperties;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        List<String> inputList = request.getInputList();
        String code = request.getCode();

        // 0. 初始化
        Map<String, String> context = new HashMap<>();

        // 1. 保存代码
        File codeFile = save(code);

        // 2. 编译代码
        beforeCompile(context, codeFile);
        ExecuteMessage compileMessage = compile(context, codeFile);
        afterCompile(context, compileMessage);
        log.debug("编译信息: {}", compileMessage);

        // 3. 运行代码
        beforeRun(context, codeFile, inputList);
        List<ExecuteMessage> executeMessageList = run(context, codeFile, inputList);
        afterRun(context, executeMessageList);

        // 4. 构建响应
        ExecuteCodeResponse executeCodeResponse = buildResp(executeMessageList);
        log.debug("代码沙箱响应: {}", executeCodeResponse);

        // 5. 清理代码
        cleanup(codeFile);

        return executeCodeResponse;
    }

    protected abstract String language();

    protected LangSpecSetting langSpecSetting() {
        return codeSandboxProperties.getLanguageSettings().get(language());
    }

    protected File save(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + codeSandboxProperties.getGlobalCodePath();

        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + langSpecSetting().getMainFile();
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    protected void beforeCompile(Map<String, String> context, File codeFile) {}

    protected abstract ExecuteMessage compile(Map<String, String> context, File codeFile);

    protected void afterCompile(Map<String, String> context, ExecuteMessage executeMessage) {}

    protected void beforeRun(Map<String, String> context, File codeFile, List<String> inputList) {}

    protected abstract List<ExecuteMessage> run(Map<String, String> context, File codeFile, List<String> inputList);

    protected void afterRun(Map<String, String> context, List<ExecuteMessage> executeMessageList) {}

    protected ExecuteCodeResponse buildResp(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        long maxMemory = 0;
        boolean isTimeout = false;

        // 取出每轮执行的结果
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage) || (executeMessage.getExitValue() != 0)) {
                return buildUsrErrorResp(errorMessage);
            }
            outputList.add(executeMessage.getMessage());
            maxTime = Math.max(maxTime, executeMessage.getTime());
            maxMemory = Math.max(maxMemory, executeMessage.getMemory());
            isTimeout = executeMessage.isTimeout() || isTimeout;
        }

        // 正常执行完成
        executeCodeResponse.setStatus(1);
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setIsTimeOut(isTimeout);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }

    protected void cleanup(File codeFile) {
        File parentFile = codeFile.getParentFile();
        if (FileUtil.exist(parentFile)) {
            boolean del = FileUtil.del(parentFile);
            if (!del) {
                log.error("代码缓存删除失败");
            } else {
                log.info("代码缓存删除成功");
            }
        }
    }

    /**
     * 获取错误响应（系统）
     *
     * @param e 异常
     * @return 错误响应
     */
    protected ExecuteCodeResponse buildSysErrorResp(Exception e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

    /**
     * 获取错误响应（用户）
     *
     * @param errorMessage 错误信息
     * @return 错误响应
     */
    protected ExecuteCodeResponse buildUsrErrorResp(String errorMessage) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(errorMessage);
        // 表示用户代码错误
        executeCodeResponse.setStatus(3);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
