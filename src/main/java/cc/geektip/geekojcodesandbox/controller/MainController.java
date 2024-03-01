package cc.geektip.geekojcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Fish
 * @date: 2024/3/1
 */
@RestController("/")
public class MainController {
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
