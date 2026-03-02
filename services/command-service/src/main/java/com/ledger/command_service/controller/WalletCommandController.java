package com.ledger.command_service.controller;

import com.ledger.command_service.application.dto.*;
import com.ledger.command_service.application.execution.RetryExecutor;
import com.ledger.command_service.application.handler.*;
import com.ledger.command_service.domain.command.*;
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
    private final  DebitWalletHandler debitWalletHandler;
    private final ReverseTransactionHandler reverseTransactionHandler;
    private final RetryExecutor retryExecutor;

    public WalletCommandController(
            CreateWalletHandler createWalletHandler,
            ActivateWalletHandler activateHandler,
            SuspendWalletHandler suspendWalletHandler,
            CloseWalletHandler closeWalletHandler,
            CreditWalletHandler creditWalletHandler,
            DebitWalletHandler debitWalletHandler,
            ReverseTransactionHandler reverseTransactionHandler,
            RetryExecutor retryExecutor
    ) {
        this.createWalletHandler = createWalletHandler;
        this.activateHandler = activateHandler;
        this.suspendWalletHandler = suspendWalletHandler;
        this.closeWalletHandler = closeWalletHandler;
        this.creditWalletHandler = creditWalletHandler;
        this.debitWalletHandler = debitWalletHandler;
        this.reverseTransactionHandler = reverseTransactionHandler;
        this.retryExecutor = retryExecutor;
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

//        WalletLifecycleResponse response =
//                activateHandler.handle(command);
        WalletLifecycleResponse response =
                retryExecutor.execute(() -> activateHandler.handle(command));

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


//        WalletLifecycleResponse response =
//                suspendWalletHandler.handle(command);
        WalletLifecycleResponse response =
                retryExecutor.execute(() -> suspendWalletHandler.handle(command));

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


//        WalletLifecycleResponse response =
//                closeWalletHandler.handle(command);
        WalletLifecycleResponse response =
                retryExecutor.execute(() -> closeWalletHandler.handle(command));

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


//        CreditWalletResponse response =
//                creditWalletHandler.handle(command);
        CreditWalletResponse response =
                retryExecutor.execute(() -> creditWalletHandler.handle(command));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/debit")
    public ResponseEntity<DebitWalletResponse> debitHandler(
            @RequestBody DebitWalletRequest request
    ) throws Exception{
        DebitWalletCommand command =
                new DebitWalletCommand(
                        request.walletId(),
                        request.debitAmount(),
                        request.clientId(),
                        request.clientRequestId()
                );


//        DebitWalletResponse response =
//                debitWalletHandler.handle(command);
        DebitWalletResponse response =
                retryExecutor.execute(() -> debitWalletHandler.handle(command));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/reverstransaction")
    public ResponseEntity<ReverseTransactionResponse> debitHandler(
            @RequestBody ReverseTransactionRequest request
    ) throws Exception{

        ReverseTransactionCommand command =
                new ReverseTransactionCommand(
                        request.walletId(),
                        request.originalTransactionId(),
                        request.clientId(),
                        request.clientRequestId()
                );


//        ReverseTransactionResponse response =
//                reverseTransactionHandler.handle(command);

        ReverseTransactionResponse response =
                retryExecutor.execute(() -> reverseTransactionHandler.handle(command));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
