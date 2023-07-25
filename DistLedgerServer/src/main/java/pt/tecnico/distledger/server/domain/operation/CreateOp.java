package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class CreateOp extends Operation {
    private List<Integer> prev;
    public CreateOp(String account, List <Integer> prev ) {
        super(account);
        this.prev = prev; 
    }

    public List<Integer> getPrevTS(){
        return prev;
    }

    public void setPrevTS(List<Integer> prevTS){
        this.prev = prevTS;
    }

    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() {

        DistLedgerCommonDefinitions.Operation.Builder grpc_operation = DistLedgerCommonDefinitions.Operation.newBuilder();
        grpc_operation.setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT);
        grpc_operation.setUserId(getAccount());
        grpc_operation.setPrevTimeStamp(0,this.getPrevTS().get(0));
        grpc_operation.setPrevTimeStamp(1,this.getPrevTS().get(1));
        return grpc_operation.build();
    }
}
