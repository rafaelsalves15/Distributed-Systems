package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;




import java.util.List;



public class CrossServerService {
    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub;
    private ManagedChannel channel;
    

    public CrossServerService(){
    }
    

    public void propagateState(LedgerState ledger, String qualifier){
        if(qualifier.equals("A")){

            // Server to propagate -> B
            int port = 2002;                // port de servidor B 
            String host = "localhost" ;     //host de servidor B 
            
            String target = host + ":" + port;

            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel); 

        }
        else{
            // Server to propagate -> A
            int port = 2001;                //port de servidor A
            String host = "localhost" ;     //host de servidor A
            
            String target = host + ":" + port;

            this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel); 
        }
        
        try{
            PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledger).build(); 
            this.stub.propagateState(request);
        }
        catch(StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }
}
