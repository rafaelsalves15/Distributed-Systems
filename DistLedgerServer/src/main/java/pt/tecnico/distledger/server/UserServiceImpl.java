package pt.tecnico.distledger.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.ShareWithOthersResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedBalanceResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedCreateAccountResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedDeleteAccountResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedShareWithOthersResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedTransferToResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.AccountDoesNotExistException;
import java.io.File;
import java.io.FileInputStream;
import pt.tecnico.distledger.server.domain.exceptions.ValueNotValidException;
import pt.tecnico.distledger.server.domain.exceptions.ServerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;

import java.io.InputStream;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.MessageDigest;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private final ServerState serverState;
    private static final String SECRETKEYPATH = "/home/rafael/SDEE23/distledger-private-solution-phase-1-reference/DistLedgerServer/src/main/resources/secret.key";


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
                                           StreamObserver<UserDistLedger.SignedCreateAccountResponse> responseObserver)  {

        try {

            // Unpack request
            String userId = request.getUserId();
            List<Integer> prevTS = request.getPrevTimestampList();

            // Perform operation
            List<Integer> timeStamp = this.serverState.createAccount(userId , prevTS);

             // Guardar chave secreta 
             SecretKeySpec secretKey = readKey(SECRETKEYPATH) ;

            // Build response
            UserDistLedger.CreateAccountResponse.Builder responseBuilder = UserDistLedger.CreateAccountResponse
                    .newBuilder();
            
            responseBuilder.addNewTimestamp(timeStamp.get(0));
            responseBuilder.addNewTimestamp(timeStamp.get(1));

            UserDistLedger.CreateAccountResponse response = responseBuilder.build();

            // Criar Signed Response 
            UserDistLedger.SignedCreateAccountResponse signedResponse = createSignedCreateAccountResponse(response , secretKey);
            
            responseObserver.onNext(signedResponse);
            responseObserver.onCompleted();


            LOGGER.info(String.format("Account Created <%s>", userId));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public synchronized void deleteAccount(UserDistLedger.DeleteAccountRequest request,
                                           StreamObserver<UserDistLedger.SignedDeleteAccountResponse> responseObserver) {

        try {
            // Unpack request
            String userId = request.getUserId();

            // Perform operation
            this.serverState.deleteAccount(userId);

            // Build response
            UserDistLedger.SignedDeleteAccountResponse.Builder responseBuilder = UserDistLedger.SignedDeleteAccountResponse
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
                                     StreamObserver<UserDistLedger.SignedBalanceResponse> responseObserver)  {
        try {
            // Unpack request
            String userId = request.getUserId();
            List<Integer> prev = request.getPrevTimestampList();

            // Perform operation
            List<Object> balanceList = this.serverState.getUserBalance(userId,prev);

            Integer balance = (Integer) balanceList.get(0) ;
            List<Integer> timeStamp = (List<Integer>) balanceList.get(1);

            // Guardar chave secreta 
            SecretKeySpec secretKey = readKey(SECRETKEYPATH) ;
            // --- Build response : ----
            // BalanceResponse e Signature formam SignatureBalanceResponse

            UserDistLedger.BalanceResponse.Builder responseBuilder = UserDistLedger.BalanceResponse.newBuilder();
            responseBuilder.setValue(balance);
            responseBuilder.addNewTimestamp(timeStamp.get(0));
            responseBuilder.addNewTimestamp(timeStamp.get(1));
            UserDistLedger.BalanceResponse response  = responseBuilder.build(); 

            UserDistLedger.SignedBalanceResponse signedResponse = createSignedBalanceResponse(response , secretKey);


            responseObserver.onNext(signedResponse);
            responseObserver.onCompleted();
            
            LOGGER.info(String.format("Balance request for <%s>", userId));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }

    }

    @Override
    public synchronized void transferTo(UserDistLedger.TransferToRequest request,
                                        StreamObserver<UserDistLedger.SignedTransferToResponse> responseObserver) {

        try {
            // Unpack request
            String from = request.getAccountFrom();
            String to = request.getAccountTo();
            List<Integer> prev = request.getPrevTimestampList();

            int value = request.getAmount();

            // Perform operation
            List<Integer> timeStamp = this.serverState.transfer(from, to, value ,prev );


            // Guardar chave secreta 
            SecretKeySpec secretKey = readKey(SECRETKEYPATH) ;

            // Build response
            UserDistLedger.TransferToResponse.Builder responseBuilder = UserDistLedger.TransferToResponse.newBuilder();
            
            responseBuilder.addNewTimestamp(timeStamp.get(0));
            responseBuilder.addNewTimestamp(timeStamp.get(1));

            UserDistLedger.TransferToResponse response  = responseBuilder.build(); 

            UserDistLedger.SignedTransferToResponse signedResponse = createSignedTransferToResponse(response , secretKey);

            responseObserver.onNext(signedResponse);
            responseObserver.onCompleted();
            LOGGER.info(String.format("Transfer request from <%s> to <%s> | Amount: %d", from, to, value));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }

    }

    @Override
    public synchronized void shareWithOthers (UserDistLedger.ShareWithOthersRequest request, 
                                                StreamObserver<UserDistLedger.SignedShareWithOthersResponse> responseObserver) {
        try {
        
        // Obter dados que vêm no request
        String name = request.getName();
        int value = request.getValue();
        List<Integer> prev = request.getPrevTimestampList();

        // Fazer a operação shareWithOthers
        List<Integer> timeStamp = this.serverState.shareWithOthersSvState(name ,  value, prev);
        
        // Guardar chave secreta 
        SecretKeySpec secretKey = readKey(SECRETKEYPATH) ;
        
        // Construir Resposta
        UserDistLedger.ShareWithOthersResponse.Builder responseBuilder = UserDistLedger.ShareWithOthersResponse.newBuilder();

        responseBuilder.addNewTimestamp(timeStamp.get(0));
        responseBuilder.addNewTimestamp(timeStamp.get(1));
        
        UserDistLedger.ShareWithOthersResponse response  = responseBuilder.build(); 

        UserDistLedger.SignedShareWithOthersResponse signedResponse = createSignedShareWithOthersResponse(response , secretKey);

        responseObserver.onNext(signedResponse);
        responseObserver.onCompleted();

        LOGGER.info(String.format("Share With Others From  <%s> with %d ", name , value));

        } catch (ServerException e) {
            LOGGER.warning(e.getMessage());
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

    public static SecretKeySpec readKey(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);

            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            fis.close();

            return new SecretKeySpec(content, "AES");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    


    // Computa algoritmo SHA256 - gera sequencia de bytes a partir de uma mensagem(data)  
     public static byte[] calculateSHA256(byte[] data) throws NoSuchAlgorithmException {
    
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256"); 
        
        return messageDigest.digest(data);
    }


    // Funcao para cifrar o resumo usando a chave secreta (algoritmo AES)
    private static byte[] encryptDigest(byte[] digest, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        return cipher.doFinal(digest);
    }

    // Funcoes auxiliares para criar respostas assinadas para cada tipo de operacao :  

    //Cria resposta de Balance 
     public static SignedBalanceResponse createSignedBalanceResponse(BalanceResponse response, SecretKeySpec secretKey) throws Exception {
        byte[] responseBytes = response.toByteArray();
        byte[] digest = calculateSHA256(responseBytes);
        byte[] signature = encryptDigest(digest, secretKey);

        UserDistLedger.Signature sig = UserDistLedger.Signature.newBuilder()
                .setSignerId("server")
                .setValue(ByteString.copyFrom(signature))
                .build();

        UserDistLedger.SignedBalanceResponse signedResponse = UserDistLedger.SignedBalanceResponse.newBuilder()
                .setResponse(response)
                .setSignature(sig)
                .build();

        return signedResponse;
    }

    //Cria resposta de CreateAccount 
     public static SignedCreateAccountResponse createSignedCreateAccountResponse(CreateAccountResponse response, SecretKeySpec secretKey) throws Exception {
        byte[] responseBytes = response.toByteArray();
        byte[] digest = calculateSHA256(responseBytes);
        byte[] signature = encryptDigest(digest, secretKey);

        UserDistLedger.Signature sig = UserDistLedger.Signature.newBuilder()
                .setSignerId("server")
                .setValue(ByteString.copyFrom(signature))
                .build();

        UserDistLedger.SignedCreateAccountResponse signedResponse = UserDistLedger.SignedCreateAccountResponse.newBuilder()
                .setResponse(response)
                .setSignature(sig)
                .build();

        return signedResponse;
    }

    //Cria resposta de TransferTo 
     public static SignedTransferToResponse createSignedTransferToResponse(TransferToResponse response, SecretKeySpec secretKey) throws Exception {
        byte[] responseBytes = response.toByteArray();
        byte[] digest = calculateSHA256(responseBytes);
        byte[] signature = encryptDigest(digest, secretKey);

        UserDistLedger.Signature sig = UserDistLedger.Signature.newBuilder()
                .setSignerId("server")
                .setValue(ByteString.copyFrom(signature))
                .build();

        UserDistLedger.SignedTransferToResponse signedResponse = UserDistLedger.SignedTransferToResponse.newBuilder()
                .setResponse(response)
                .setSignature(sig)
                .build();

        return signedResponse;
    }

    //Cria resposta de ShareWithOthers 
     public static SignedShareWithOthersResponse createSignedShareWithOthersResponse(ShareWithOthersResponse response, SecretKeySpec secretKey) throws Exception {
        byte[] responseBytes = response.toByteArray();
        byte[] digest = calculateSHA256(responseBytes);
        byte[] signature = encryptDigest(digest, secretKey);

        UserDistLedger.Signature sig = UserDistLedger.Signature.newBuilder()
                .setSignerId("server")
                .setValue(ByteString.copyFrom(signature))
                .build();

        UserDistLedger.SignedShareWithOthersResponse signedResponse = UserDistLedger.SignedShareWithOthersResponse.newBuilder()
                .setResponse(response)
                .setSignature(sig)
                .build();

        return signedResponse;
    }


}



