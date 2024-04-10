package cc.geektip.geekojcodesandbox.job.scheduled;

import cc.geektip.geekojcodesandbox.docker.DockerDao;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryHeartBeatPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 定时任务类 - Docker 健康检查
 * @author: Fish
 * @date: 2024/4/1
 */
@Slf4j
@Component
public class DockerHealthCheckTask {
    @Resource
    private DockerDao dockerDao;
    @Resource
    private NacosDiscoveryHeartBeatPublisher nacosDiscoveryHeartBeatPublisher;

    private final AtomicBoolean lastHealth = new AtomicBoolean(true);

    /**
     * 定时检查 Docker 健康状态，每分钟一次
     */
    @Scheduled(cron = "0 0/1 * * ? ")
    public void doDockerHealthCheck() {
        boolean health = dockerDao.pingCheck();
        if (health == lastHealth.get()) return;
        lastHealth.set(health);
        if (!health) {
            log.error("监测到 Docker 服务异常，暂停服务注册");
            nacosDiscoveryHeartBeatPublisher.stop();
        } else {
            log.info("Docker 服务恢复正常，恢复服务注册");
            nacosDiscoveryHeartBeatPublisher.start();
        }
    }
}