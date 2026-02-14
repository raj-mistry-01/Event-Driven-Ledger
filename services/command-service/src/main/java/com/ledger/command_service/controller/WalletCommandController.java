package com.ledger.command_service.controller;

import com.ledger.command_service.application.dto.CreateWalletRequest;
import com.ledger.command_service.application.dto.CreateWalletResponse;
import com.ledger.command_service.application.handler.CreateWalletHandler;
import com.ledger.command_service.domain.command.CreateWalletCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletCommandController{

    private final CreateWalletHandler createWalletHandler;

    public WalletCommandController(CreateWalletHandler createWalletHandler) {
        this.createWalletHandler = createWalletHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateWalletResponse> createWallet(
            @RequestBody CreateWalletRequest request
    ) throws Exception {

        CreateWalletCommand command =
                new CreateWalletCommand(
                        request.clientId(),
                        request.clientRequestId()
                );


        CreateWalletResponse  resposne = createWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposne);
    }

    @PostMapping(path = "/activate") public String activateHandler(){
        // call to create handler
        System.out.println("yes");
        return "testing";
    }

    @PostMapping(path = "/suspend") public String suspendHandler(){
        // call to suspend handler
        System.out.println("yes");
        return "testing";
    }



}
