package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class InsufficientBalanceException extends ServerException {
    public InsufficientBalanceException() {
        super("Insufficient Balance", Status.PERMISSION_DENIED.withDescription("Insufficient Balance"));
    }

    public InsufficientBalanceException(String message, Status status) {
        super(message, status);
    }

}
