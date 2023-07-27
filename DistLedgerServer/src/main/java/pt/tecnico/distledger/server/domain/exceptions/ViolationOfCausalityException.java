package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class ViolationOfCausalityException extends ServerException {

    public ViolationOfCausalityException() {
        super("Violation of Causality: Not updated", Status.INVALID_ARGUMENT.withDescription("Violation of Causality: Not updated with other server"));
    }

    public ViolationOfCausalityException(String message, Status status) {
        super(message, status);
    }

}