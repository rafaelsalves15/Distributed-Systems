package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;

public class AdminService {

    private final ManagedChannel channel;
    private final AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String target) {
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    // Operations supported are:
    // 1. Activate
    // 2. Deactivate
    // 3. Get Ledger State

    public AdminDistLedger.ActivateResponse activate(AdminDistLedger.ActivateRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.activate(request);
    }

    public AdminDistLedger.DeactivateResponse deactivate(AdminDistLedger.DeactivateRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.deactivate(request);
    }

    public AdminDistLedger.GetLedgerStateResponse getLedgerState(AdminDistLedger.GetLedgerStateRequest request, String server) {
        // in the first phase we ignore the server because there is only one
        return this.stub.getLedgerState(request);
    }

    public AdminDistLedger.GossipResponse gossip(AdminDistLedger.GossipRequest request, String server) {
        return this.stub.gossip(request);
        
    }
    

    public void shutdown() {
        channel.shutdown();
    }
}
