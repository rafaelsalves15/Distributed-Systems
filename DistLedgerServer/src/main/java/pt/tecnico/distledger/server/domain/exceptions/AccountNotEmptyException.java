package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class AccountNotEmptyException extends ServerException {
    public AccountNotEmptyException() {
        super("Account not empty", Status.PERMISSION_DENIED.withDescription("Account not empty"));
    }

    public AccountNotEmptyException(String message, Status status) {
        super(message, status);
    }

}