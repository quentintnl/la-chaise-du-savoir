package fr.lachaisedusavoir.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Public endpoint works!");
        response.put("timestamp", System.currentTimeMillis() + "");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public")
    public ResponseEntity<Map<String, String>> publicTestPost(@RequestBody(required = false) Map<String, String> body) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Public POST endpoint works!");
        response.put("receivedBody", body != null ? body.toString() : "null");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Spring Boot!");
    }
}

