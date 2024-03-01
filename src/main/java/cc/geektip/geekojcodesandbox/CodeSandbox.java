package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.model.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.ExecuteCodeResponse;

/**
 * @description: 代码沙箱接口
 * @author: Fish
 * @date: 2024/2/28
 */
public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);
}
