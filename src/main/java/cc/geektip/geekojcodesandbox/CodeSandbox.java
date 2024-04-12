package cc.geektip.geekojcodesandbox;

import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeRequest;
import cc.geektip.geekojcodesandbox.model.dto.ExecuteCodeResponse;

/**
 * @description: 代码沙箱接口
 * @author: Bill Yu
 * @date: 2024/2/28
 */
public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);
}
