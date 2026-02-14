package com.ledger.command_service.controller;

import com.ledger.command_service.application.dto.CreateWalletRequest;
import com.ledger.command_service.application.dto.CreateWalletResponse;
import com.ledger.command_service.application.dto.WalletLifecycleRequest;
import com.ledger.command_service.application.dto.WalletLifecycleResponse;
import com.ledger.command_service.application.handler.CreateWalletHandler;
import com.ledger.command_service.application.handler.SuspendWalletHandler;
import com.ledger.command_service.domain.command.CreateWalletCommand;
import com.ledger.command_service.domain.command.SuspendWalletCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletCommandController{

    private final CreateWalletHandler createWalletHandler;
    private final SuspendWalletHandler suspendWalletHandler;

    public WalletCommandController(CreateWalletHandler createWalletHandler , SuspendWalletHandler suspendWalletHandler) {
        this.createWalletHandler = createWalletHandler;
        this.suspendWalletHandler = suspendWalletHandler;
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


        CreateWalletResponse  reponse = createWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PostMapping(path = "/activate") public String activateHandler(){
        // call to create handler
        System.out.println("yes");
        return "testing";
    }

    @PostMapping(path = "/suspend")
    public ResponseEntity<WalletLifecycleResponse> suspendHandler(
            @RequestBody WalletLifecycleRequest request
    ) throws Exception{
        SuspendWalletCommand command =
                new SuspendWalletCommand(
                        request.walletId(),
                        request.clientId(),
                        request.clientRequestId()
                );

        WalletLifecycleResponse response =
                suspendWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
