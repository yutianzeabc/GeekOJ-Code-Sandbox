package cc.geektip.geekojcodesandbox.langspec.cpp;

import cc.geektip.geekojcodesandbox.CodeSandboxTemplate;
import cc.geektip.geekojcodesandbox.docker.DockerCleanupManager;
import cc.geektip.geekojcodesandbox.docker.DockerInstance;
import cc.geektip.geekojcodesandbox.model.ExecuteMessage;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description: CppDockerCodeSandbox
 * @author: Fish
 * @date: 2024/3/1
 */
@Component
@Slf4j
public class CppDockerCodeSandbox extends CodeSandboxTemplate {
    @Resource
    private DockerInstance dockerInstance;

    @Resource
    private DockerCleanupManager dockerCleanupManager;

    @Override
    protected String language() {
        return "cpp";
    }

    @Override
    protected void beforeCompile(Map<String, String> context, File codeFile) {
        String containerId = dockerInstance.createContainer(codeFile.getParent());
        context.put("containerId", containerId);
        dockerInstance.startContainer(containerId);
    }

    @SneakyThrows
    @Override
    protected ExecuteMessage compile(Map<String, String> context, File codeFile) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        String[] compileCmd = langSpecSetting().getCompileCommand().split(" ");
        String containerId = context.get("containerId");
        String execId = dockerInstance.createExecCmd(containerId, compileCmd);

        // 执行结果
        final StringBuffer message = new StringBuffer();
        final StringBuffer errorMessage = new StringBuffer();

        // 记录时间
        StopWatch stopWatch = new StopWatch();
        final AtomicLong time = new AtomicLong();
        // 判断是否超时
        final AtomicBoolean isTimeout = new AtomicBoolean(true);

        try (var exec = dockerInstance.startExecCmd(execId, new ResultCallbackTemplate<ResultCallback<Frame>, Frame>() {
            @Override
            public void onStart(Closeable stream) {
                super.onStart(stream);
                stopWatch.start();
                log.debug("编译开始");
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
                isTimeout.set(false);
                time.set(stopWatch.getTotalTimeMillis());
                log.debug("编译完成");
                super.onComplete();
            }
        })) {
            exec.awaitCompletion(langSpecSetting().getCompileTimeOut(), TimeUnit.MILLISECONDS);
        }

        executeMessage.setExitValue(dockerInstance.inspectExecCmd(execId));
        executeMessage.setMessage(message.toString());
        executeMessage.setErrorMessage(errorMessage.toString());
        executeMessage.setTime(time.get());
        executeMessage.setTimeout(isTimeout.get());

        log.debug("代码编译结果: {}", executeMessage);

        return executeMessage;
    }

    @SneakyThrows
    @Override
    protected List<ExecuteMessage> run(Map<String, String> context, File codeFile, List<String> inputList) {
        String containerId = context.get("containerId");
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList) {

            String[] inputArgsArray = inputArgs.split(" ");
            String[] runCommand = langSpecSetting().getRunCommand().split(" ");
            String[] cmdArray = ArrayUtil.append(runCommand, inputArgsArray);
            String execId = dockerInstance.createExecCmd(containerId, cmdArray);

            // 执行结果
            final StringBuffer message = new StringBuffer();
            final StringBuffer errorMessage = new StringBuffer();

            // 记录时间
            StopWatch stopWatch = new StopWatch();
            final AtomicLong time = new AtomicLong();
            // 判断是否超时
            final AtomicBoolean isTimeout = new AtomicBoolean(true);
            // 判断是否超内存
            final AtomicLong maxMemory = new AtomicLong(0L);

            try (var statsCmd = dockerInstance.statsContainer(containerId, new ResultCallbackTemplate<ResultCallback<Statistics>, Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    Long memoryUsage = statistics.getMemoryStats().getUsage();
                    if (ObjectUtil.isNotNull(memoryUsage)) {
                        maxMemory.set(Math.max(maxMemory.get(), memoryUsage));
                    } else {
                        log.debug("内存使用情况: N/A");
                    }
                }
            }); var exec = dockerInstance.startExecCmd(execId, new ResultCallbackTemplate<ResultCallback<Frame>, Frame>() {
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
                    isTimeout.set(false);
                    time.set(stopWatch.getTotalTimeMillis());
                    log.debug("用例执行完成");
                    super.onComplete();
                }
            })
            ) {
                exec.awaitCompletion(langSpecSetting().getRunTimeOut(), TimeUnit.MILLISECONDS);
            }

            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(dockerInstance.inspectExecCmd(execId));
            executeMessage.setMessage(message.toString());
            executeMessage.setErrorMessage(errorMessage.toString());
            executeMessage.setTime(time.get());
            executeMessage.setTimeout(isTimeout.get());
            executeMessage.setMemory(maxMemory.get());
            executeMessageList.add(executeMessage);

            log.debug("用例执行结果: {}", executeMessage);
        }

        return executeMessageList;
    }

    @Override
    protected void afterRun(Map<String, String> context, List<ExecuteMessage> executeMessageList) {
        String containerId = context.get("containerId");
        dockerCleanupManager.submitCleanupTask(containerId);
    }
}
