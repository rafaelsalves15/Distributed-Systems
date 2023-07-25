package pt.tecnico.distledger.userclient.grpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedTransferToResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.ShareWithOthersRequest;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedBalanceResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedCreateAccountResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.SignedShareWithOthersResponse;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import javax.crypto.spec.SecretKeySpec;

import java.io.InputStream;

public class UserService {
    private  ManagedChannel channel;
    private  UserServiceGrpc.UserServiceBlockingStub stub;
    private final static String SECRETKEYPATH = "/home/rafael/SDEE23/distledger-private-solution-phase-1-reference/User/src/main/resources/secret.key";
    
    public UserService(String target) {
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }

    // Operations supported are:
    // 1. Create account
    // 2. Delete account
    // 3. Balance
    // 4. Transfer to
    // 5. Share with others

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

    public UserDistLedger.SignedCreateAccountResponse createAccount(UserDistLedger.CreateAccountRequest request, String server) throws Exception {
        // in the first phase we ignore the server because there is only one
        connect(server);

        UserDistLedger.SignedCreateAccountResponse signedResponse = this.stub.createAccount(request);

        if (signedResponse == null) {
            System.out.println("Error . null ");
            return null;
        }

        // Verificar a assinatura usando a chave secreta
        SecretKey secretKey = readKey(SECRETKEYPATH);
        
        if (verifyCreateAccountSignature(signedResponse , secretKey)) {
            // Assinatura e valida -> return a resposta original 
            return signedResponse;
        } else {
            // Assinatura e invalida 
            return null;
        }
    }

    public UserDistLedger.SignedDeleteAccountResponse deleteAccount(UserDistLedger.DeleteAccountRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        connect(server);
        return this.stub.deleteAccount(request);
    }

    public UserDistLedger.SignedBalanceResponse balance(UserDistLedger.BalanceRequest request, String server) throws Exception {
        // in the first phase we ignore the server because there is only one
        connect(server);

        UserDistLedger.SignedBalanceResponse signedResponse = this.stub.balance(request);
        
        if (signedResponse == null) {
            System.out.println("Error . null ");
            return null;
        }

        // Verificar a assinatura usando a chave secreta
        SecretKey secretKey = readKey(SECRETKEYPATH);
        
        if (verifyBalanceSignature(signedResponse , secretKey)) {
            // Assinatura e valida -> return a resposta original 
            return signedResponse;
        } else {
            // Assinatura e invalida 
            return null;
        }
    }

    public UserDistLedger.SignedTransferToResponse transferTo(UserDistLedger.TransferToRequest request, String server) throws Exception {
        // in the first phase we ignore the server because there is only one
        connect(server);


        UserDistLedger.SignedTransferToResponse signedResponse = this.stub.transferTo(request);
        
        if (signedResponse == null) {
            System.out.println("Error . null ");
            return null;
        }

        // Verificar a assinatura usando a chave secreta
        SecretKey secretKey = readKey(SECRETKEYPATH);
        
        if (verifyTransferToSignature(signedResponse , secretKey)) {
            // Assinatura e valida -> return a resposta original 
            return signedResponse;
        } else {
            // Assinatura e invalida 
            return null;
        }
    }
    
    public UserDistLedger.SignedShareWithOthersResponse shareWithOthers(UserDistLedger.ShareWithOthersRequest request, String server) throws Exception {
        connect(server);
        UserDistLedger.SignedShareWithOthersResponse signedResponse = this.stub.shareWithOthers(request);
        
        if (signedResponse == null) {
            System.out.println("Error . null ");
            return null;
        }

        // Verificar a assinatura usando a chave secreta
        SecretKey secretKey = readKey(SECRETKEYPATH);
        
        if (verifyShareWithOthersSignature(signedResponse , secretKey)) {
            // Assinatura e valida -> return a resposta original 
            return signedResponse;
        } else {
            // Assinatura e invalida 
            return null;
        }
    }
    
    public void connect(String server){
         if(server.equals("A")){
            int port = 2001;                // port de servidor A
            String host = "localhost" ;     //host de servidor A 
            String target = host + ":" + port;
            shutdown();
            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stub = UserServiceGrpc.newBlockingStub(channel);
        }
        else{
            int port = 2002;                // port de servidor A
            String host = "localhost" ;     //host de servidor A 
            String target = host + ":" + port;
            shutdown();
            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stub = UserServiceGrpc.newBlockingStub(channel);
        }
    }

    private byte[] decryptSignature(byte[] receivedSignature, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
    return cipher.doFinal(receivedSignature);
    }

    // Função para calcular o resumo SHA-256 a partir dos dados recebidos
    private byte[] calculateSHA256(byte[] data) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(data);
    }


    public boolean verifyBalanceSignature(SignedBalanceResponse signedResponse, SecretKey secretKey) throws Exception {
        byte[] originalResponse = signedResponse.getResponse().toByteArray();
        byte[] signature = signedResponse.getSignature().getValue().toByteArray();

        // Calculate digest of the original response
        byte[] calculatedDigest = calculateSHA256(originalResponse);

        // Decrypt the signature using AES/ECB/PKCS5Padding
        byte[] decryptedSignature = decryptSignature(signature, secretKey);

        // Compare the decrypted signature with the calculated digest
        boolean signatureIsValid = Arrays.equals(calculatedDigest, decryptedSignature);

            if (signatureIsValid) {
                System.out.println("Signature is valid! Message accepted! :)");
        } 
        else {
                System.out.println("Signature is invalid! Message rejected! :(");
        }

        return signatureIsValid;
    }

    public boolean verifyCreateAccountSignature(SignedCreateAccountResponse signedResponse, SecretKey secretKey) throws Exception {
        byte[] originalResponse = signedResponse.getResponse().toByteArray();
        byte[] signature = signedResponse.getSignature().getValue().toByteArray();

        // Calculate digest of the original response
        byte[] calculatedDigest = calculateSHA256(originalResponse);

        // Decrypt the signature using AES/ECB/PKCS5Padding
        byte[] decryptedSignature = decryptSignature(signature, secretKey);

        // Compare the decrypted signature with the calculated digest
        boolean signatureIsValid = Arrays.equals(calculatedDigest, decryptedSignature);

            if (signatureIsValid) {
                System.out.println("Signature is valid! Message accepted! :)");
        } 
        else {
                System.out.println("Signature is invalid! Message rejected! :(");
        }

        return signatureIsValid;
    }

    public boolean verifyTransferToSignature(SignedTransferToResponse signedResponse, SecretKey secretKey) throws Exception {
        byte[] originalResponse = signedResponse.getResponse().toByteArray();
        byte[] signature = signedResponse.getSignature().getValue().toByteArray();

        // Calculate digest of the original response
        byte[] calculatedDigest = calculateSHA256(originalResponse);

        // Decrypt the signature using AES/ECB/PKCS5Padding
        byte[] decryptedSignature = decryptSignature(signature, secretKey);

        // Compare the decrypted signature with the calculated digest
        boolean signatureIsValid = Arrays.equals(calculatedDigest, decryptedSignature);

            if (signatureIsValid) {
                System.out.println("Signature is valid! Message accepted! :)");
        } 
        else {
                System.out.println("Signature is invalid! Message rejected! :(");
        }

        return signatureIsValid;
    }

    public boolean verifyShareWithOthersSignature(SignedShareWithOthersResponse signedResponse, SecretKey secretKey) throws Exception {
        byte[] originalResponse = signedResponse.getResponse().toByteArray();
        byte[] signature = signedResponse.getSignature().getValue().toByteArray();

        // Calculate digest of the original response
        byte[] calculatedDigest = calculateSHA256(originalResponse);

        // Decrypt the signature using AES/ECB/PKCS5Padding
        byte[] decryptedSignature = decryptSignature(signature, secretKey);

        // Compare the decrypted signature with the calculated digest
        boolean signatureIsValid = Arrays.equals(calculatedDigest, decryptedSignature);

            if (signatureIsValid) {
                System.out.println("Signature is valid! Message accepted! :)");
        } 
        else {
                System.out.println("Signature is invalid! Message rejected! :(");
        }

        return signatureIsValid;
    }





    public void shutdown() {
        channel.shutdown();
    }
}
