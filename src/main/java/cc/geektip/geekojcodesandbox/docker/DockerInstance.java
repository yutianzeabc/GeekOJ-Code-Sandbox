package cc.geektip.geekojcodesandbox.docker;

import cc.geektip.geekojcodesandbox.config.DockerProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;

/**
 * @description: DockerInstance
 * @author: Fish
 * @date: 2024/3/8
 */
@Slf4j
@Component
public class DockerInstance {

    @Resource
    private DockerProperties dockerProperties;

    @Getter
    private DockerClient dockerClient;

    @PostConstruct
    public void init() {
        dockerClient = DockerClientBuilder.getInstance().withDockerHttpClient(new ApacheDockerHttpClient.Builder().dockerHost(URI.create(dockerProperties.getHost())).build()).build();
        if (dockerProperties.isFirstInit()) {
            log.info("首次启动，拉取镜像");
            String image = dockerProperties.getImage();
            try {
                dockerClient.pullImageCmd(image).exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        System.out.println("下载镜像: " + item.getStatus());
                        super.onNext(item);
                    }
                }).awaitCompletion();
            } catch (Exception e) {
                log.error("拉取镜像失败");
                throw new RuntimeException(e);
            }
            log.info("拉取镜像完成");
        }
    }

    public String createContainer(String appPath) {
        String image = dockerProperties.getImage();

        HostConfig hostConfig = new HostConfig()
                .withMemory(dockerProperties.getContainer().getMemory())
                .withMemorySwap(dockerProperties.getContainer().getMemorySwap())
                .withCpuCount(dockerProperties.getContainer().getCpuCount())
                .withReadonlyRootfs(dockerProperties.getContainer().isReadOnlyRootfs())
                .withBinds(new Bind(appPath, new Volume("/app")));

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image)
                .withHostConfig(hostConfig)
                .withNetworkDisabled(dockerProperties.getContainer().isNetworkDisabled())
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true);

        try {
            CreateContainerResponse createContainerResponse = createContainerCmd.exec();
            String containerId = createContainerResponse.getId();

            log.debug("创建容器成功，容器ID: {}", containerId);
            return containerId;
        } catch (Exception e) {
            log.error("创建容器失败", e);
            throw new RuntimeException(e);
        }
    }

    public void removeContainer(String containerId) {
        try {
            dockerClient.removeContainerCmd(containerId).exec();

            log.debug("删除容器成功，容器ID: {}", containerId);
        } catch (Exception e) {
            log.error("删除容器失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }

    public void startContainer(String containerId) {
        try {
            dockerClient.startContainerCmd(containerId).exec();

            log.debug("启动容器成功，容器ID: {}", containerId);
        } catch (Exception e) {
            log.error("启动容器失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }

    public void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();

            log.debug("停止容器成功，容器ID: {}", containerId);
        } catch (Exception e) {
            log.error("停止容器失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }

    public String createExecCmd(String containerId, String[] cmd) {
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withTty(true)
                    .withCmd(cmd)
                    .exec();

            log.debug("创建执行命令成功，容器ID: {}", containerId);
            return execCreateCmdResponse.getId();
        } catch (Exception e) {
            log.error("创建执行命令失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }

    public <T extends ResultCallback<Frame>> T startExecCmd(String execId, T callback) {
        try {
            ExecStartCmd execStartCmd = dockerClient.execStartCmd(execId);
            T exec = execStartCmd.exec(callback);

            log.debug("启动执行命令成功，执行命令ID: {}", execId);
            return exec;
        } catch (Exception e) {
            log.error("启动执行命令失败，执行命令ID: {}", execId, e);
            throw new RuntimeException(e);
        }
    }

    public long inspectExecCmd(String execId) {
        try {
            InspectExecResponse inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();

            log.debug("获取执行命令状态成功，执行命令ID: {}", execId);
            Long exitCode = inspectExecResponse.getExitCodeLong();
            return exitCode == null ? -1 : exitCode;
        } catch (Exception e) {
            log.error("获取执行命令状态失败，执行命令ID: {}", execId, e);
            throw new RuntimeException(e);
        }
    }

    public <T extends ResultCallback<Statistics>> StatsCmd statsContainer(String containerId, T callback) {
        try {
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(callback);

            log.debug("获取容器状态成功，容器ID: {}", containerId);
            return statsCmd;
        } catch (Exception e) {
            log.error("获取容器状态失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }

    public <T extends ResultCallback<Frame>> AttachContainerCmd attachContainerCmd(String containerId, InputStream inputStream) {
        try {
            AttachContainerCmd attach = dockerClient.attachContainerCmd(containerId)
                    .withStdIn(inputStream)
                    .withStdOut(true)
                    .withStdErr(true);

            log.debug("挂接容器成功，容器ID: {}", containerId);
            return attach;
        } catch (Exception e) {
            log.error("挂接容器失败，容器ID: {}", containerId, e);
            throw new RuntimeException(e);
        }
    }
}
