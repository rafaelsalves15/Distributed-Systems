package pt.tecnico.distledger.server.domain.exceptions;

import io.grpc.Status;

public abstract class ServerException extends Exception {
    private final Status status;

    public ServerException(String message, Status status) {
        super(message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
