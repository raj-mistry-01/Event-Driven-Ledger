package com.ledger.command_service.application.execution;

import com.ledger.command_service.application.exception.OptimisticLockException;
import com.ledger.command_service.application.exception.RetryLaterException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;


@Component
public class RetryExecutor {

    private static final int MAX_RETRIES = 3;

    public <T> T execute(Supplier<T> action) {

        int attempt = 0;

        while (true) {
            try {
//                System.out.println("Attempt " + (attempt + 1) + " of " + MAX_RETRIES);
                return action.get();
            } catch (OptimisticLockException e) {
                attempt++;


                // 1..N strategy:
                if (attempt >= MAX_RETRIES) {
                    throw new RetryLaterException(); // exhausted retries
                }


                // fixed delay strategy:
                /*

                try {
                    Thread.sleep(200L); // Fixed 200ms delay
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    throw new RuntimeException("Retry interrupted", ie);
                }
                */

                // backoff + jitter strategy:
                /*

                long backoffTime = (long) Math.pow(2, attempt) * 100L; // Exponential backoff
                long jitter = (long) (Math.random() * 100L); // Random jitter
                long sleepTime = backoffTime + jitter;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                * */
            }
        }
    }
}