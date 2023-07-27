package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class AccountHasNoMoneyToShareException extends ServerException {

    public AccountHasNoMoneyToShareException() {
        super("Account empty: ", Status.INVALID_ARGUMENT.withDescription("Account has no money to share."));
    }

    public AccountHasNoMoneyToShareException(String message, Status status) {
        super(message, status);
    }

}