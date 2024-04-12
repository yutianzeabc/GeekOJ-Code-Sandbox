package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.config.CodeSandboxProperties;
import cc.geektip.geekojcodesandbox.config.LangSpecSetting;
import cc.geektip.geekojcodesandbox.enums.SandboxErrorEnum;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeResponse;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteResult;
import cc.geektip.geekojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 代码沙箱模板类，用于定义代码沙箱的基本流程，包括保存代码、编译代码、运行代码、构建响应、清理代码等
 * @author: Bill Yu
 * @date: 2024/3/11
 */
@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {

    @Resource
    protected CodeSandboxProperties codeSandboxProperties;

    @Override
    public final ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        List<String> inputList = request.getInputList();
        String code = request.getCode();
        String language = request.getLanguage();

        // 0. 初始化上下文
        Map<String, String> context = new HashMap<>();
        context.put("code", code);
        context.put("language", language);

        // 1. 保存代码
        File codeFile = save(context);

        // 2. 编译代码
        ExecuteResult compileResult;
        try {
            beforeCompile(context, codeFile);
            compileResult = compile(context, codeFile);
            afterCompile(context, compileResult);
            if (compileResult.isTimeout()) {
                beforeExit(context);
                return buildCompileErrorResp(new Exception(SandboxErrorEnum.CODE_COMPILE_TLE.getMsg()));
            }
            if (compileResult.getExitValue() != 0) {
                beforeExit(context);
                return buildCompileErrorResp(new Exception(compileResult.getErrorOutput()));
            }
        } catch (Exception e) {
            log.error("代码沙箱异常: ", e);
            beforeExit(context);
            return buildCompileErrorResp(e);
        }

        // 3. 运行代码
        // 4. 清理代码
        List<ExecuteResult> executeMessageList;
        try {
            beforeRun(context, codeFile, inputList);
            executeMessageList = run(context, codeFile, inputList);
            ExecuteResult lastExecuteMessage = executeMessageList.get(executeMessageList.size() - 1);
            afterRun(context, executeMessageList);
            cleanup(codeFile);
            if (lastExecuteMessage.isTimeout()) {
                beforeExit(context);
                return buildRunErrorResp(new Exception(SandboxErrorEnum.CODE_RUN_TLE.getMsg()));
            }
            if (lastExecuteMessage.getExitValue() != 0) {
                beforeExit(context);
                return buildRunErrorResp(new Exception(lastExecuteMessage.getErrorOutput()));
            }
        } catch (Exception e) {
            log.error("代码沙箱异常: ", e);
            beforeExit(context);
            return buildRunErrorResp(e);
        }

        // 5. 构建响应
        ExecuteCodeResponse executeCodeResponse = buildResp(executeMessageList);
        log.debug("代码沙箱响应: {}", executeCodeResponse);

        // 6. 返回响应
        beforeExit(context);
        return executeCodeResponse;

    }

    protected String language(Map<String, String> context) {
        return context.get("language");
    }

    protected LangSpecSetting langSpecSetting(Map<String, String> context) {
        return codeSandboxProperties.getLanguageSettings().get(language(context));
    }

    protected File save(Map<String, String> context) {
        String code = context.get("code");
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + codeSandboxProperties.getGlobalCodePath();

        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + langSpecSetting(context).getMainFile();
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    protected void beforeCompile(Map<String, String> context, File codeFile) {
    }

    protected abstract ExecuteResult compile(Map<String, String> context, File codeFile);

    protected void afterCompile(Map<String, String> context, ExecuteResult executeMessage) {
    }

    protected void beforeRun(Map<String, String> context, File codeFile, List<String> inputList) {
    }

    protected abstract List<ExecuteResult> run(Map<String, String> context, File codeFile, List<String> inputList);

    protected void afterRun(Map<String, String> context, List<ExecuteResult> executeMessageList) {
    }

    protected void beforeExit(Map<String, String> context) {
    }

    protected ExecuteCodeResponse buildResp(List<ExecuteResult> executeResultList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setCode(ExecuteCodeStatusEnum.SUCCESS.getValue());
        executeCodeResponse.setMsg(ExecuteCodeStatusEnum.SUCCESS.getMsg());
        executeCodeResponse.setResults(executeResultList);
        return executeCodeResponse;
    }

    protected void cleanup(File codeFile) {
        File parentFile = codeFile.getParentFile();
        if (FileUtil.exist(parentFile)) {
            boolean del = FileUtil.del(parentFile);
            if (!del) {
                log.error("代码缓存删除失败");
            } else {
                log.debug("代码缓存删除成功");
            }
        }
    }

    /**
     * 获取错误响应（编译）
     *
     * @param e 异常
     * @return 错误响应
     */
    protected ExecuteCodeResponse buildCompileErrorResp(Exception e) {
        return ExecuteCodeResponse.builder()
                .code(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())
                .msg(e.getMessage())
                .build();
    }

    /**
     * 获取错误响应 （运行）
     *
     * @param e 异常
     * @return 错误响应
     */
    protected ExecuteCodeResponse buildRunErrorResp(Exception e) {
        return ExecuteCodeResponse.builder()
                .code(ExecuteCodeStatusEnum.RUN_FAILED.getValue())
                .msg(e.getMessage())
                .build();
    }

}
