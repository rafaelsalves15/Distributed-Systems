package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class AccountDoesNotExistException extends ServerException {

    public AccountDoesNotExistException() {
        super("Account does not exist", Status.INVALID_ARGUMENT.withDescription("Account does not exist"));
    }

    public AccountDoesNotExistException(String message, Status status) {
        super(message, status);
    }

}