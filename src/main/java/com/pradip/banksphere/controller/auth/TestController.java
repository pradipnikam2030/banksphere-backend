package com.pradip.banksphere.controller.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "You are authenticated!";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Welcome Admin!";
    }
}
