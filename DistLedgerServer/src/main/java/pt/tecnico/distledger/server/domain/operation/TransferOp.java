package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount) {
        super(fromAccount);
        this.destAccount = destAccount;
        this.amount = amount;
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

    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() {

        DistLedgerCommonDefinitions.Operation.Builder grpcOperation = DistLedgerCommonDefinitions.Operation.newBuilder();
        grpcOperation.setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO);
        grpcOperation.setUserId(getAccount());
        grpcOperation.setDestUserId(this.destAccount);
        grpcOperation.setAmount(this.amount);
        return grpcOperation.build();
    }
}
