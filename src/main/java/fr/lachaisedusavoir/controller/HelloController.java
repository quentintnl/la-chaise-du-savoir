package fr.lachaisedusavoir.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World !";
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return "Bienvenue sur La Chaise du Savoir !";
    }
}
