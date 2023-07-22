package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;


public class ShareWithOthersOP extends Operation {
    private String name;
    private int value;

        public ShareWithOthersOP(String name, int value) {
            super(name);
            this.value = value;
        }


        // Getters: 

       
        public int getValue(){
            return value;
        }

        // Setters: 
    
        public void setValue(int value) {
            this.value = value;
        }


    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() { 
        // Builder
        DistLedgerCommonDefinitions.Operation.Builder grpcOperation = DistLedgerCommonDefinitions.Operation.newBuilder();
        // Atribuir valores corretos 
        grpcOperation.setType(DistLedgerCommonDefinitions.OperationType.OP_SHARE_WITH_OTHERS);
        grpcOperation.setUserId(getAccount());
        grpcOperation.setTypeValue(this.value);
        return grpcOperation.build();
    }
   
}
