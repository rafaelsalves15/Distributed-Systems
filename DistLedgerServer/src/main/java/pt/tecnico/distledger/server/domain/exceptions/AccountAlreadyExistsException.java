package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class AccountAlreadyExistsException extends ServerException {

    public AccountAlreadyExistsException() {
        super("Account already exists", Status.ALREADY_EXISTS.withDescription("Account already exists"));
    }

    public AccountAlreadyExistsException(String message, Status status) {
        super(message, status);
    }

}