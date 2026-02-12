package com.ledger.command_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletCommandController{

    @PostMapping(path = "/create") public String createHandler(){
        // call to create handler
        System.out.println("yes");
        return "testing";
    }

    @PostMapping(path = "/activate") public String activateHandler(){
        // call to create handler
        System.out.println("yes");
        return "testing";
    }

}
