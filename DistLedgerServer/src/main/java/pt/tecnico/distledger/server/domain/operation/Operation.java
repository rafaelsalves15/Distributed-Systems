package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

public abstract class Operation {
    private String account;
    private OperationType type;
    


    public Operation(String fromAccount ) {
        this.account = fromAccount;
        
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
    
    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type){
        this.type = type;
    }

    
  
    


    public abstract DistLedgerCommonDefinitions.Operation toGrpc();
}
