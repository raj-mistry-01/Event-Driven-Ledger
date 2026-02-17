package com.ledger.command_service.controller;

import com.ledger.command_service.application.dto.CreateWalletRequest;
import com.ledger.command_service.application.dto.CreateWalletResponse;
import com.ledger.command_service.application.dto.WalletLifecycleRequest;
import com.ledger.command_service.application.dto.WalletLifecycleResponse;
import com.ledger.command_service.application.handler.ActivateWalletHandler;
import com.ledger.command_service.application.handler.CreateWalletHandler;
import com.ledger.command_service.application.handler.SuspendWalletHandler;
import com.ledger.command_service.domain.command.CreateWalletCommand;
import com.ledger.command_service.domain.command.WalletLifeCycleCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletCommandController{

    private final CreateWalletHandler createWalletHandler;
    private final ActivateWalletHandler activateHandler;
    private final SuspendWalletHandler suspendWalletHandler;

    public WalletCommandController(
            CreateWalletHandler createWalletHandler,
            ActivateWalletHandler activateHandler,
            SuspendWalletHandler suspendWalletHandler
    ) {
        this.createWalletHandler = createWalletHandler;
        this.activateHandler = activateHandler;
        this.suspendWalletHandler = suspendWalletHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateWalletResponse> createWallet(
            @RequestBody CreateWalletRequest request
    ) throws Exception {
        System.out.println("yes");
        CreateWalletCommand command =
                new CreateWalletCommand(
                        request.clientId(),
                        request.clientRequestId()
                );


        CreateWalletResponse  reponse = createWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PostMapping(path = "/activate")
    public ResponseEntity<WalletLifecycleResponse> activateHandler(
            @RequestBody WalletLifecycleRequest request
    ) throws Exception{
        WalletLifeCycleCommand command =
                new WalletLifeCycleCommand(
                        request.walletId(),
                        request.clientId(),
                        request.clientRequestId()
                );

        WalletLifecycleResponse response =
                activateHandler.handle(command);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/suspend")
    public ResponseEntity<WalletLifecycleResponse> suspendHandler(
            @RequestBody WalletLifecycleRequest request
    ) throws Exception{
        WalletLifeCycleCommand command =
                new WalletLifeCycleCommand(
                        request.walletId(),
                        request.clientId(),
                        request.clientRequestId()
                );


        WalletLifecycleResponse response =
                suspendWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
