import java.util.ArrayList;
import java.util.List;

/**
 * @description: 无限占用空间（浪费系统内存）
 * @author: Fish
 * @date: 2024/3/7
 */
public class Main {

    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[10000]);
        }
    }
}
