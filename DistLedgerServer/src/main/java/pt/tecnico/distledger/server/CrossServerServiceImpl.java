
package pt.tecnico.distledger.server;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.AccountAlreadyExistsException;
import pt.tecnico.distledger.server.domain.exceptions.AccountDoesNotExistException;
import pt.tecnico.distledger.server.domain.exceptions.AccountHasNoMoneyToShareException;
import pt.tecnico.distledger.server.domain.exceptions.DestAccountDoesNotExistException;
import pt.tecnico.distledger.server.domain.exceptions.InactiveServerException;
import pt.tecnico.distledger.server.domain.exceptions.InsufficientBalanceException;
import pt.tecnico.distledger.server.domain.exceptions.ValueNotValidException;
import pt.tecnico.distledger.server.domain.exceptions.ViolationOfCausalityException;
import pt.tecnico.distledger.server.grpc.*;
import io.grpc.stub.StreamObserver;


public class CrossServerServiceImpl extends  DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
    
    private ServerState server ;
    private CrossServerService crossServerService;

    public CrossServerServiceImpl(ServerState server) {
        this.server = server ;
    }

    @Override
    public synchronized void propagateState(PropagateStateRequest request , StreamObserver<PropagateStateResponse> responseObserver){
        LedgerState ledgerState = request.getState();
        try {
            
            server.gossip(ledgerState);


        } catch (InactiveServerException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (AccountAlreadyExistsException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (AccountDoesNotExistException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (InsufficientBalanceException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (DestAccountDoesNotExistException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (ValueNotValidException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (ViolationOfCausalityException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (AccountHasNoMoneyToShareException e) {
            System.out.println(e.getStatus().getDescription());
        }
        
        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    
    }

}
