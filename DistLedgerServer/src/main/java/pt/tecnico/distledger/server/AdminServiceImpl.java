package pt.tecnico.distledger.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.ServerException;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.CrossServerService;
import pt.tecnico.distledger.server.utils.GrpcConverter;

import java.util.List;
import java.util.logging.Logger;

import com.google.longrunning.Operation;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    private static final Logger LOGGER = Logger.getLogger(AdminServiceGrpc.class.getName());
    private final ServerState serverState;
    private CrossServerService crossServerService;
    public AdminServiceImpl(ServerState serverState) {
        this.serverState = serverState;
        this.crossServerService = new CrossServerService();
    }

    // Operations supported are:
    // 1. Activate
    // 2. Deactivate
    // 3. Get Ledger State
    // 4. Gossip

    @Override
    public synchronized void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        LOGGER.info("Activate request");
        try {

            this.serverState.setState(true);
            ActivateResponse.Builder responseBuilder = ActivateResponse.newBuilder();
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public synchronized void deactivate(DeactivateRequest request,
                                        StreamObserver<DeactivateResponse> responseObserver) {
        LOGGER.info("Deactivate request");
        try {
            this.serverState.setState(false);
            DeactivateResponse.Builder responseBuilder = DeactivateResponse.newBuilder();
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

    public synchronized void getLedgerState(GetLedgerStateRequest request,
                                            StreamObserver<GetLedgerStateResponse> responseObserver) {
        LOGGER.info("getLedgerState request");
        try {
            // Get the server state
            List<DistLedgerCommonDefinitions.Operation> ledger =
                    GrpcConverter.listDomainStateToGrpc(this.serverState.getLedger());
            // Create the builders
            GetLedgerStateResponse.Builder responseBuilder = GetLedgerStateResponse.newBuilder();
            DistLedgerCommonDefinitions.LedgerState.Builder ledgerStateBuilder =
                    DistLedgerCommonDefinitions.LedgerState.newBuilder();
            // Add the operations to the ledger state
            ledgerStateBuilder.addAllLedger(ledger);
            responseBuilder.setLedgerState(ledgerStateBuilder.build());
            // Send the response
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        
        } catch (ServerException e) {
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

  
   
    @Override
     public void gossip(GossipRequest request, StreamObserver <GossipResponse> responseObserver){
        LOGGER.info("gossip request");

        
        try{
            String qualifier = request.getQualifier();
            // Obter ledger list
            List<pt.tecnico.distledger.server.domain.operation.Operation> listaLedger = this.serverState.getLedger();

            //Construir ledger State para mandar para o outro servidor
            LedgerState.Builder ledgerStateBuilder = LedgerState.newBuilder();
            for (pt.tecnico.distledger.server.domain.operation.Operation ledgerAdd : listaLedger) {
                ledgerStateBuilder.addLedger(ledgerAdd.toGrpc());
            }
            LedgerState ledger = ledgerStateBuilder.build();
            
            crossServerService.propagateState(ledger , qualifier); 

            // this.serverState.gossip(ledger);
            GossipResponse res = GossipResponse.newBuilder().build();

            responseObserver.onNext(res);
            responseObserver.onCompleted();

        }catch (ServerException e) {
            responseObserver.onError(e.getStatus().asRuntimeException());
        } catch (RuntimeException e) {
            Status status = Status.UNAVAILABLE;
            responseObserver.onError(status.asRuntimeException());
        }
    }

     

    
}
