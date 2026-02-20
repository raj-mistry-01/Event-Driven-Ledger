package com.ledger.command_service.controller;

import com.ledger.command_service.application.dto.*;
import com.ledger.command_service.application.handler.*;
import com.ledger.command_service.domain.command.CreateWalletCommand;
import com.ledger.command_service.domain.command.CreditWalletCommand;
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
    private final CloseWalletHandler closeWalletHandler;
    private final CreditWalletHandler creditWalletHandler;

    public WalletCommandController(
            CreateWalletHandler createWalletHandler,
            ActivateWalletHandler activateHandler,
            SuspendWalletHandler suspendWalletHandler,
            CloseWalletHandler closeWalletHandler,
            CreditWalletHandler creditWalletHandler
    ) {
        this.createWalletHandler = createWalletHandler;
        this.activateHandler = activateHandler;
        this.suspendWalletHandler = suspendWalletHandler;
        this.closeWalletHandler = closeWalletHandler;
        this.creditWalletHandler = creditWalletHandler;
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


    @PostMapping(path = "/close")
    public ResponseEntity<WalletLifecycleResponse> closeHandler(
            @RequestBody WalletLifecycleRequest request
    ) throws Exception{
        WalletLifeCycleCommand command =
                new WalletLifeCycleCommand(
                        request.walletId(),
                        request.clientId(),
                        request.clientRequestId()
                );


        WalletLifecycleResponse response =
                closeWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/credit")
    public ResponseEntity<CreditWalletResponse> creditHandler(
            @RequestBody CreditWalletRequest request
    ) throws Exception{
        CreditWalletCommand command =
                new CreditWalletCommand(
                        request.walletId(),
                        request.creditAmount(),
                        request.clientId(),
                        request.clientRequestId()
                );


        CreditWalletResponse response =
                creditWalletHandler.handle(command);

        return ResponseEntity.status(HttpStatus.OK).body(response);
//        return ResponseEntity.status(HttpStatus.OK).body("yes");
    }
}
