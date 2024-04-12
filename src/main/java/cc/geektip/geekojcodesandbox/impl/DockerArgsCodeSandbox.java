package cc.geektip.geekojcodesandbox.impl;

import cc.geektip.geekojcodesandbox.CodeSandboxTemplate;
import cc.geektip.geekojcodesandbox.docker.DockerCleanupManager;
import cc.geektip.geekojcodesandbox.docker.DockerDao;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteResult;
import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.ByteArrayOutputStream;
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
 * @description: DockerArgsCodeSandbox
 * @author: Bill Yu
 * @date: 2024/3/1
 */
@Component
@Slf4j
public class DockerArgsCodeSandbox extends CodeSandboxTemplate {
    @Resource
    private DockerDao dockerDao;

    @Resource
    private DockerCleanupManager dockerCleanupManager;


    @Override
    protected void beforeCompile(Map<String, String> context, File codeFile) {
        String hostCodeCachePath = codeSandboxProperties.getHostCodeCachePath();
        String hostUserCodePath = hostCodeCachePath + File.separator + context.get("userCodeUuid");
        String containerId = dockerDao.createContainer(langSpecSetting(context).getImage(), hostUserCodePath);
        context.put("containerId", containerId);
        dockerDao.startContainer(containerId);
    }

    @SneakyThrows
    @Override
    protected ExecuteResult compile(Map<String, String> context, File codeFile) {
        ExecuteResult executeResult = new ExecuteResult();
        String[] compileCmd = langSpecSetting(context).getCompileCommand().split(" ");
        String containerId = context.get("containerId");
        String execId = dockerDao.createExecCmd(containerId, compileCmd);

        // 执行结果
        final StringBuffer output = new StringBuffer();
        final StringBuffer errorOutput = new StringBuffer();

        // 记录时间
        StopWatch stopWatch = new StopWatch();
        final AtomicLong time = new AtomicLong();
        // 判断是否超时
        final AtomicBoolean isTimeout = new AtomicBoolean(true);

        try (var exec = dockerDao.startExecCmd(execId, new ResultCallbackTemplate<ResultCallback<Frame>, Frame>() {
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
                    output.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                } else if (streamType == StreamType.STDERR) {
                    errorOutput.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
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
            exec.awaitCompletion(langSpecSetting(context).getCompileTimeOut(), TimeUnit.MILLISECONDS);
        }

        executeResult.setExitValue(dockerDao.inspectExecCmd(execId));
        executeResult.setOutput(output.toString());
        executeResult.setErrorOutput(errorOutput.toString());
        executeResult.setTime(time.get());
        executeResult.setTimeout(isTimeout.get());

        log.debug("代码编译结果: {}", executeResult);

        return executeResult;
    }

    @SneakyThrows
    @Override
    protected List<ExecuteResult> run(Map<String, String> context, File codeFile, List<String> inputList) {
        String containerId = context.get("containerId");
        List<ExecuteResult> executeResultList = new ArrayList<>(inputList.size());

        for (String inputArgs : inputList) {

            String[] inputArgsArray = inputArgs.split(" ");
            String[] runCommand = langSpecSetting(context).getRunCommand().split(" ");
            String[] cmdArray = ArrayUtil.append(runCommand, inputArgsArray);
            String execId = dockerDao.createExecCmd(containerId, cmdArray);

            // 记录时间
            StopWatch stopWatch = new StopWatch();
            final AtomicLong time = new AtomicLong();
            // 判断是否超时
            final AtomicBoolean isTimeout = new AtomicBoolean(true);
            // 判断是否超内存
            final AtomicLong maxMemory = new AtomicLong(0L);
            // 执行结果
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            try (var stat = dockerDao.statsContainer(containerId, new ResultCallbackTemplate<ResultCallback<Statistics>, Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    Long memoryUsage = statistics.getMemoryStats().getUsage();
                    if (ObjectUtil.isNotNull(memoryUsage)) {
                        maxMemory.set(Math.max(maxMemory.get(), memoryUsage));
                    }
                }
            }); var exec = dockerDao.startExecCmd(execId, new ExecStartResultCallback(outputStream, errorStream) {
                @Override
                public void onStart(Closeable stream) {
                    super.onStart(stream);
                    log.debug("用例执行开始: {}", inputArgs);
                    stopWatch.start();
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
                exec.awaitCompletion(langSpecSetting(context).getRunTimeOut(), TimeUnit.MILLISECONDS);
            }

            ExecuteResult executeResult = new ExecuteResult();
            executeResult.setExitValue(dockerDao.inspectExecCmd(execId));
            executeResult.setOutput(outputStream.toString(StandardCharsets.UTF_8).stripTrailing());
            executeResult.setErrorOutput(errorStream.toString(StandardCharsets.UTF_8).stripTrailing());
            executeResult.setTime(time.get());
            executeResult.setTimeout(isTimeout.get());
            executeResult.setMemory(DataSize.ofBytes(maxMemory.get()).toMegabytes());
            executeResultList.add(executeResult);

            outputStream.close();
            errorStream.close();

            log.debug("用例执行结果: {}", executeResult);
        }

        return executeResultList;
    }

    @Override
    protected void beforeExit(Map<String, String> context) {
        String containerId = context.get("containerId");
        dockerCleanupManager.submitCleanupTask(containerId);
    }
}
