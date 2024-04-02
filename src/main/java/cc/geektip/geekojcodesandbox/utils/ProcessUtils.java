package cc.geektip.geekojcodesandbox.utils;

import cc.geektip.geekojcodesandbox.model.dto.ExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 进程工具类，用于处理进程的执行信息，包括正常输出和错误输出，以及错误码等信息
 * @author: Fish
 * @date: 2024/3/1
 */
@Slf4j
public class ProcessUtils {

    private final static String PROCESS_CHARSET = "GBK";

    /**
     * 运行进程并获取进程的执行信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteResult runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteResult executeResult = new ExecuteResult();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            long exitValue = runProcess.waitFor();
            executeResult.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                log.info(opName + "成功");
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), PROCESS_CHARSET));

                // 逐行读取
                List<String> outputStrList = new ArrayList<>();
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeResult.setOutput(String.join("\n", outputStrList));
            } else {
                // 异常退出
                log.error(opName + "失败，错误码: " + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), PROCESS_CHARSET));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeResult.setOutput(String.join("\n", outputStrList));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream(), PROCESS_CHARSET));

                // 逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeResult.setErrorOutput(String.join("\n", errorOutputStrList));
            }
            stopWatch.stop();
            executeResult.setTime(stopWatch.lastTaskInfo().getTimeMillis());
        } catch (Exception e) {
            log.error("运行进程失败: ", e);
        } finally {
            runProcess.destroy();
        }
        return executeResult;

    }

    /**
     * 运行交互式进程并获取进程的执行信息
     *
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteResult runInteractProcessAndGetMessage(Process runProcess, String args) {
        ExecuteResult executeMessage = new ExecuteResult();

        try (OutputStream outputStream = runProcess.getOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
             InputStream inputStream = runProcess.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, PROCESS_CHARSET))) {

            outputStreamWriter.write(String.join("\n", args.split(" ")) + "\n");
            outputStreamWriter.flush();

            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setOutput(compileOutputStringBuilder.toString());
        } catch (Exception e) {
            log.error("运行交互式进程失败: ", e);
        } finally {
            // 记得资源的释放，否则会卡死
            runProcess.destroy();
        }
        return executeMessage;
    }
}
