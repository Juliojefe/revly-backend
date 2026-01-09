package com.example.revly.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping
    public String test() {
        return "This is a protected endpoint!";
    }

    @GetMapping("/public")
    public String testPublic() { return "This is an open endpoint!"; }
}