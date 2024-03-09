package cc.geektip.geekojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.util.List;

/**
 * @description: DockerDemo
 * @author: Fish
 * @date: 2024/3/8
 */
public class DockerDemo {

    public static void main(String[] args) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost("npipe:////./pipe/docker_engine").build();

        try (DockerClient dockerClient = DockerClientBuilder.getInstance().withDockerHttpClient(new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).build()).build()) {
            //        PingCmd pingCmd = dockerClient.pingCmd();
//        pingCmd.exec();
            // 拉取镜像
            String image = "nginx:latest";
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("下载镜像：" + item.getStatus());
//                super.onNext(item);
//            }
//        };
//        pullImageCmd
//                .exec(pullImageResultCallback)
//                .awaitCompletion();
//        System.out.println("下载完成");
            // 创建容器
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
            CreateContainerResponse createContainerResponse = containerCmd
                    .withCmd("echo", "Hello Docker")
                    .exec();
            System.out.println(createContainerResponse);
            String containerId = createContainerResponse.getId();

            // 查看容器状态
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
            List<Container> containerList = listContainersCmd.withShowAll(true).exec();
            for (Container container : containerList) {
                System.out.println(container);
            }

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

//        Thread.sleep(5000L);

            // 查看日志

            LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    System.out.println(item.getStreamType());
                    System.out.println("日志：" + new String(item.getPayload()));
                    super.onNext(item);
                }
            };


            dockerClient.logContainerCmd(containerId)
                    .withStdErr(true)
                    .withStdOut(true)
                    .exec(logContainerResultCallback)
                    .awaitCompletion();

            // 删除容器
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();

            // 删除镜像
            dockerClient.removeImageCmd(image).exec();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
