package com.hackathon.blockchain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DashboardController  extends ControllerBase{

    @GetMapping("/dashboard")
    public ResponseEntity<String> getUserDashboard() {
        var userOptional = Optional.ofNullable(getUserSessionSecurity());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("You are not authenticated");
        }
        var user = userOptional.get();
        return ResponseEntity.ok("Welcome to your dashboard, " + user.getUsername() + "!\n" +
                "Your registered email is: " + user.getEmail());
    }
}