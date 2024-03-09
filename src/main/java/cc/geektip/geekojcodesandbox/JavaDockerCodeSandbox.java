package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.docker.JudgeDockerClient;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;
import cc.geektip.geekojcodesandbox.model.ExecuteMessage;
import cc.geektip.geekojcodesandbox.model.JudgeInfo;
import cc.geektip.geekojcodesandbox.utils.ProcessUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description: JavaDockerCodeSandbox
 * @author: Fish
 * @date: 2024/3/1
 */
@Component
@Slf4j
public class JavaDockerCodeSandbox implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    @Resource
    private JudgeDockerClient judgeDockerClient;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();


//        1. 把用户的代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 2. 编译代码, 生成class文件
        String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            ExecuteMessage compileMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            log.info(compileMessage.toString());
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        // 3. 创建容器，把文件复制到容器内
        // 创建容器
        String containerId = judgeDockerClient.createContainer(userCodeParentPath);
        // 启动容器
        judgeDockerClient.startContainer(containerId);

        // 执行命令并获取结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {

            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            String execId = judgeDockerClient.createExecCmd(containerId, cmdArray);

            ExecuteMessage executeMessage = new ExecuteMessage();
            final StringBuffer message = new StringBuffer();
            final StringBuffer errorMessage = new StringBuffer();
            // 退出码
            final AtomicLong exitCode = new AtomicLong(0L);
            // 记录时间
            final AtomicLong time = new AtomicLong();
            // 判断是否超时
            final AtomicBoolean isTimeout = new AtomicBoolean(true);
            // 判断是否超内存
            final AtomicLong maxMemory = new AtomicLong(0L);

            try {
                var statsCmd = judgeDockerClient.statsContainer(containerId, new ResultCallbackTemplate<ResultCallback<Statistics>, Statistics>() {
                    @Override
                    public void onNext(Statistics statistics) {
                        Long memoryUsage = statistics.getMemoryStats().getUsage();
                        if (ObjectUtil.isNotNull(memoryUsage)) {
                            maxMemory.set(Math.max(maxMemory.get(), memoryUsage));
                        } else {
                            log.debug("内存使用情况: 暂无数据");
                        }
                    }
                });
                var exec = judgeDockerClient.startExecCmd(execId, new ResultCallbackTemplate<ResultCallback<Frame>, Frame>() {
                    @Override
                    public void onStart(Closeable stream) {
                        super.onStart(stream);
                        log.debug("用例执行开始: {}", inputArgs);
                        stopWatch.start();
                    }

                    @Override
                    public void onNext(Frame frame) {
                        StreamType streamType = frame.getStreamType();
                        if (streamType == StreamType.STDOUT) {
                            message.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                        } else if (streamType == StreamType.STDERR) {
                            errorMessage.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopWatch.stop();
                        time.set(stopWatch.getTotalTimeMillis());
                        isTimeout.set(false);
                        log.debug("用例执行完成");
                    }
                });
                // 等待执行完成
                exec.awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                statsCmd.close();
                // 获取退出码
                exitCode.set(judgeDockerClient.inspectExecCmd(execId));
            } catch (Exception e) {
                log.error("程序执行异常: ", e);
                return getErrorResponse(e);
            }
            executeMessage.setExitValue(exitCode.get());
            executeMessage.setMessage(message.toString());
            executeMessage.setErrorMessage(errorMessage.toString());
            executeMessage.setTime(time.get());
            executeMessage.setTimeout(isTimeout.get());
            executeMessage.setMemory(maxMemory.get());
            executeMessageList.add(executeMessage);
            log.debug("用例执行结果: {}", executeMessage);
        }

        // 4. 封装结果，跟原生实现方式完全一致
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取最大时间
        long maxTime = 0;
        long maxMemory = 0;
        boolean isTimeout = false;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage) || (executeMessage.getExitValue() != 0)) {
                executeCodeResponse.setMessage(errorMessage);
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            maxTime = Math.max(maxTime, executeMessage.getTime());
            maxMemory = Math.max(maxMemory, executeMessage.getMemory());
            isTimeout = executeMessage.isTimeout() || isTimeout;
        }
        // 正常执行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setIsTimeOut(isTimeout);

        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 5. 移除容器
        judgeDockerClient.stopContainer(containerId);
        judgeDockerClient.removeContainer(containerId);

        // 6. 文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            if (!del) {
                log.error("删除失败");
            } else {
                log.info("删除成功");
            }
        }

        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     *
     * @param e 异常
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Exception e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
