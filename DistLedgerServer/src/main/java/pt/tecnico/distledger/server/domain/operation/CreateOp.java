package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class CreateOp extends Operation {

    public CreateOp(String account) {
        super(account);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() {

        DistLedgerCommonDefinitions.Operation.Builder grpc_operation = DistLedgerCommonDefinitions.Operation.newBuilder();
        grpc_operation.setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT);
        grpc_operation.setUserId(getAccount());
        return grpc_operation.build();
    }
}
