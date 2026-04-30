package fr.lachaisedusavoir.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/match")
@Slf4j
public class MatchController {

    @RequestMapping("/create")
    public ResponseEntity<?> createMatch() {
        try {

            return ResponseEntity.ok("Match created successfully");
        } catch (Exception e) {
            log.error("Unexpected error during match creation", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la création du match");
        }
    }
}
