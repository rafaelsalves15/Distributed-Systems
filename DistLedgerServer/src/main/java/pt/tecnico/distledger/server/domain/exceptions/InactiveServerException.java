package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public class InactiveServerException extends ServerException {
    public InactiveServerException() {
        super("Server is not active", Status.PERMISSION_DENIED.withDescription("Server is not active"));
    }
}