package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class DestAccountDoesNotExistException extends ServerException {

    public DestAccountDoesNotExistException() {
        super("Destination account does not exist", Status.INVALID_ARGUMENT.withDescription("Destination account does not exist"));
    }

    public DestAccountDoesNotExistException(String message, Status status) {
        super(message, status);
    }

}
