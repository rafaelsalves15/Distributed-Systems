
package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;




public class CrossServerServiceImpl extends  DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
    
    private ServerState server ;
    private CrossServerService crossServerService;

    public CrossServerServiceImpl(ServerState server) {
        this.server = server ;
    }

    @Override
    public synchronized void propagateState(PropagateStateRequest request , StreamObserver <PropagateStateResponse> responseObserver){
        LedgerState ledgerState = request.getState();
        server.gossip(ledgerState);
        
        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    
    }

}
