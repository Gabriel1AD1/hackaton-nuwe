package com.hackathon.blockchain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contracts")
public class SmartContractsController {
    @PostMapping("/create")
    public ResponseEntity<?> newContract(){
        return ResponseEntity.ok().build();
    }
    @PostMapping("/validate/{id-contract}")
    public ResponseEntity<?> isValidContract(@PathVariable("id-contract") Long parameter){
        return ResponseEntity.ok().build();
    }
}
