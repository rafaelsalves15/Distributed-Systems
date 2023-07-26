package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;


public class ShareWithOthersOP extends Operation {
    private int value;
    private List<Integer> prev;
        public ShareWithOthersOP(String name, int value , List<Integer> prev) {
            super(name);
            this.value = value;
            this.prev = prev; 
        }


        // Getters: 

        public List<Integer> getPrevTS(){
            return prev;
        }
        public int getValue(){
            return value;
        }

        // Setters: 
    
        public void setValue(int value) {
            this.value = value;
        }

        public void setPrevTS(List<Integer> prevTS){
            this.prev = prevTS;
        }
    @Override
    public DistLedgerCommonDefinitions.Operation toGrpc() { 
        // Builder
        DistLedgerCommonDefinitions.Operation.Builder grpcOperation = DistLedgerCommonDefinitions.Operation.newBuilder();
        // Atribuir valores corretos 
        grpcOperation.setType(DistLedgerCommonDefinitions.OperationType.OP_SHARE_WITH_OTHERS);
        grpcOperation.setUserId(getAccount());
        grpcOperation.setTypeValue(this.value);
        grpcOperation.addPrevTimeStamp(this.getPrevTS().get(0));
        grpcOperation.addPrevTimeStamp(this.getPrevTS().get(1));
        return grpcOperation.build();
    }
   
}
