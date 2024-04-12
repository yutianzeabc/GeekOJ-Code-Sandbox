package cc.geektip.geekojcodesandbox.unsafe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @description: 读取服务器文件（文件信息泄露）
 * @author: Bill Yu
 * @date: 2024/3/7
 */
public class ReadFileError {

    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + "/src/main/resources/application.yml";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        System.out.println(String.join("\n", lines));
    }
}
