package pt.tecnico.distledger.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.AccountDoesNotExistException;

import pt.tecnico.distledger.server.domain.exceptions.ValueNotValidException;
import pt.tecnico.distledger.server.domain.exceptions.ServerException;

import java.util.logging.Logger;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private final ServerState serverState;

    public UserServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    // Operations supported are:
    // 1. Create Account
    // 2. Delete Account
    // 3. Balance
    // 4. Transfer To
    // 5. Share with others

    @Override
    public synchronized void createAccount(UserDistLedger.CreateAccountRequest request,
                                           StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {

        try {
            // Unpack request
            String userId = request.getUserId();

            // Perform operation
            this.serverState.createAccount(userId);

            // Build response
            UserDistLedger.CreateAccountResponse.Builder responseBuilder = UserDistLedger.CreateAccountResponse
                    .newBuilder();
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            LOGGER.info(String.format("Account Created <%s>", userId));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public synchronized void deleteAccount(UserDistLedger.DeleteAccountRequest request,
                                           StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {

        try {
            // Unpack request
            String userId = request.getUserId();

            // Perform operation
            this.serverState.deleteAccount(userId);

            // Build response
            UserDistLedger.DeleteAccountResponse.Builder responseBuilder = UserDistLedger.DeleteAccountResponse
                    .newBuilder();
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            LOGGER.info(String.format("Account Deleted <%s>", userId));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }

    }

    @Override
    public synchronized void balance(UserDistLedger.BalanceRequest request,
                                     StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {
        try {
            // Unpack request
            String userId = request.getUserId();

            // Perform operation
            int balance = this.serverState.getUserBalance(userId);

            // Build response
            UserDistLedger.BalanceResponse.Builder responseBuilder = UserDistLedger.BalanceResponse.newBuilder();
            responseBuilder.setValue(balance);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            LOGGER.info(String.format("Balance request for <%s>", userId));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }

    }

    @Override
    public synchronized void transferTo(UserDistLedger.TransferToRequest request,
                                        StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {

        try {
            // Unpack request
            String from = request.getAccountFrom();
            String to = request.getAccountTo();

            int value = request.getAmount();

            // Perform operation
            this.serverState.transfer(from, to, value);

            // Build response
            UserDistLedger.TransferToResponse.Builder responseBuilder = UserDistLedger.TransferToResponse.newBuilder();

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            LOGGER.info(String.format("Transfer request from <%s> to <%s> | Amount: %d", from, to, value));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }

    }

    @Override
    public synchronized void shareWithOthers (UserDistLedger.ShareWithOthersRequest request, 
                                                StreamObserver<UserDistLedger.ShareWithOthersResponse> responseObserver) {
        try {
        
        // Obter dados que vêm no request
        String name = request.getName();
        int value = request.getValue();

        // Fazer a operação shareWithOthers
        this.serverState.shareWithOthersSvState(name ,  value);

        // Construir Resposta
        UserDistLedger.ShareWithOthersResponse.Builder BuilderResponse = UserDistLedger.ShareWithOthersResponse.newBuilder();

        responseObserver.onNext(BuilderResponse.build());
        responseObserver.onCompleted();
        LOGGER.info(String.format("Share With Others From  <%s> with %d ", name , value));
        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
       
        
    }
}
