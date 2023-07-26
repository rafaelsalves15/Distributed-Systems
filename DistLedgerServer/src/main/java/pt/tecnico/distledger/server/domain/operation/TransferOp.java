package pt.tecnico.distledger.server.domain.operation;
import java.util.List;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;
    private List<Integer> prev;
    public TransferOp(String fromAccount, String destAccount, int amount , List<Integer> prev) {
        super(fromAccount);
        this.destAccount = destAccount;
        this.amount = amount; 
        this.prev = prev; 
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    public List<Integer> getPrevTS(){
        return prev;
    }

    public void setPrevTS(List<Integer> prevTS){
        this.prev = prevTS;
    }

    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() {
        
        DistLedgerCommonDefinitions.Operation.Builder grpcOperation = DistLedgerCommonDefinitions.Operation.newBuilder();
        grpcOperation.setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO);
        grpcOperation.setUserId(getAccount());
        grpcOperation.setDestUserId(this.destAccount);
        grpcOperation.setAmount(this.amount);
        grpcOperation.addPrevTimeStamp(this.getPrevTS().get(0));
        grpcOperation.addPrevTimeStamp(this.getPrevTS().get(1));
        return grpcOperation.build();
    }
}
