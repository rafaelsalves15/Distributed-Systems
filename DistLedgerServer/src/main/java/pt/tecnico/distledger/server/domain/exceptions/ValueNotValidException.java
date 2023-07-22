package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class ValueNotValidException extends ServerException {

    public ValueNotValidException() {
        super("Value invalid", Status.INVALID_ARGUMENT.withDescription("Value must be integer between 1 and 100"));
    }

    public ValueNotValidException(String message, Status status) {
        super(message, status);
    }

}