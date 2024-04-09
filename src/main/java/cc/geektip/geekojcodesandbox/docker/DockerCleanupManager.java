package cc.geektip.geekojcodesandbox.docker;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description: DockerCleanupManager
 * @author: Fish
 * @date: 2024/3/11
 */
@Slf4j
@Component
public class DockerCleanupManager {

    @Resource
    private DockerDao dockerDao;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 提交清理任务
     * @param containerId 容器ID
     */
    public void submitCleanupTask(String containerId) {
        log.debug("提交清理任务: {}", containerId);
        executorService.submit(() -> {
            dockerDao.stopContainer(containerId);
            dockerDao.removeContainer(containerId);
            log.debug("容器已停止并移除: {}", containerId);
        });
    }

    /**
     * 关闭清理服务
     */
    @PreDestroy
    public void shutdown() {
        try {
            // 通知执行服务执行关闭
            executorService.shutdown();
            // 等待一段时间让已提交的任务完成
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // 超时后，尝试强制关闭
                executorService.shutdownNow();
                // 如果仍然有任务未完成，等待再次尝试
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    log.error("清理服务关闭异常");
            }
        } catch (InterruptedException ie) {
            // 如果等待过程中线程被中断，重新尝试强制关闭
            log.warn("清理服务被中断，尝试强制关闭");
            executorService.shutdownNow();
            // 保留中断状态
            Thread.currentThread().interrupt();
        }
    }

}

