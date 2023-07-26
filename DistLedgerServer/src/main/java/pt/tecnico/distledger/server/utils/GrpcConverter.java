package pt.tecnico.distledger.server.utils;


import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.operation.ShareWithOthersOP;


import java.util.ArrayList;
import java.util.List;

public class GrpcConverter {

    public static Operation grpcStateToDomain(DistLedgerCommonDefinitions.Operation operation) {


        String username = operation.getUserId();

        if (operation.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO) {
            return new TransferOp(username, operation.getDestUserId(), operation.getAmount() , operation.getPrevTimeStampList());
        } else if (operation.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT) {
            return new CreateOp(username , operation.getPrevTimeStampList());
        } else if (operation.getType() == DistLedgerCommonDefinitions.OperationType.OP_SHARE_WITH_OTHERS){ 
            return new ShareWithOthersOP(username, operation.getValue() , operation.getPrevTimeStampList() );
        } else {
            return new DeleteOp(username);
        }

    }


    public static List<DistLedgerCommonDefinitions.Operation> listDomainStateToGrpc(List<Operation> operations) {
        ArrayList<DistLedgerCommonDefinitions.Operation> list = new ArrayList<>();
        for (Operation operation : operations) {
            list.add(operation.toGrpc());
        }
        return list;

    }

    public static List<Operation> listGrpcStateToDomain(List<DistLedgerCommonDefinitions.Operation> operations) {
        ArrayList<Operation> list = new ArrayList<>();

        for (DistLedgerCommonDefinitions.Operation op : operations) {
            list.add(grpcStateToDomain(op));
        }
        return list;
    }

}