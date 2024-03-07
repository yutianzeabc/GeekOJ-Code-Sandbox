package cc.geektip.geekojcodesandbox.unsafe;

/**
 * @description: 无限睡眠（阻塞程序执行）
 * @author: Fish
 * @date: 2024/3/5
 */
public class SleepError {
    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡眠结束");
    }
}