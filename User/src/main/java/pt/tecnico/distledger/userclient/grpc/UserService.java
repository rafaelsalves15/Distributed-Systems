package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger.ShareWithOthersRequest;
import pt.tecnico.distledger.contract.user.UserDistLedger.ShareWithOthersResponse;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService {
    private final ManagedChannel channel;
    private final UserServiceGrpc.UserServiceBlockingStub stub;

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

    public UserDistLedger.CreateAccountResponse createAccount(UserDistLedger.CreateAccountRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.createAccount(request);
    }

    public UserDistLedger.DeleteAccountResponse deleteAccount(UserDistLedger.DeleteAccountRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.deleteAccount(request);
    }

    public UserDistLedger.BalanceResponse balance(UserDistLedger.BalanceRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.balance(request);
    }

    public UserDistLedger.TransferToResponse transferTo(UserDistLedger.TransferToRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        
        return this.stub.transferTo(request);
    }



    public UserDistLedger.ShareWithOthersResponse shareWithOthers(UserDistLedger.ShareWithOthersRequest request, String server) {
        System.out.println("USER SERVICE ");
        return this.stub.shareWithOthers(request);
    }

    public void shutdown() {
        channel.shutdown();
    }
}
