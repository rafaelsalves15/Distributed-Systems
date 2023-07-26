package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.ShareWithOthersOP;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

public class ServerState {
    private final ArrayList<Operation> ledger;
    private final ArrayList<Operation> canceledOps;
    private List<Integer> repTS;
    private List<Integer> valueTS;
    private final HashMap<String, Integer> accounts;
    
    private int server;
    private boolean active = true;
    private String qualifier;

    public ServerState(String qualifier) {
        this.ledger = new ArrayList<>();
        this.canceledOps = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.qualifier = qualifier; 
        
        this.valueTS = new ArrayList<Integer>();
        this.valueTS.add(0); // A 
        this.valueTS.add(0); // B


        this.repTS = new ArrayList<Integer>();
        this.repTS.add(0); //  A
        this.repTS.add(0); //  B       
        
        if(qualifier.equals("A")){
            server = 0 ;   // Guardamos em server o valor 0 -> significa que este servidor e o A
        }else {
            server = 1 ; // Caso contrario , guardamos em server o valor 1 -> significa que este servidor e o B
        }


        // Create the broker initial account
        accounts.put("broker", 1000);
    }


    public List<Operation> getLedger() throws InactiveServerException {
        isServerActive();
        return ledger;
    }

    private void isServerActive() throws InactiveServerException {
        if (!this.active) {
            throw new InactiveServerException();
        }
    }

    public List<Operation> getCanceledOps() throws InactiveServerException {
        isServerActive();
        return canceledOps;
    }

    // Ver se frontend está a frente da replica 
    // Retorna 1 se estiver adiantado , 0 caso contrario 
    public int checkIfAhead(List<Integer> prev){
        
        int valueTimeStamp = repTS.get(server);  // Server serve como indice (se server==0 - A  se server==1 - B) 
        int prevValue = prev.get(server);
        
        if( prevValue <= valueTimeStamp && prev.get(server ^ 1) <= repTS.get(server ^ 1) ){
            return 1;
        }
        else{
            return 0;
        }
    }

    public List<Integer> createAccount(String userId ,  List<Integer> prev  ) throws InactiveServerException, AccountAlreadyExistsException {
        printTS(prev);
        
        isServerActive();

        if(checkIfAhead( prev) == 1 ){
            if (accountExists(userId)) {
                throw new AccountAlreadyExistsException();
            }
            repTS.set(server, repTS.get(server) + 1);
            valueTS.set(server, valueTS.get(server) + 1);
            // Create operation
            Operation createAccount = new CreateOp(userId,prev);
            // Add to ledger
            this.ledger.add(createAccount);
            // Create account
            this.accounts.put(userId, 0);
            return repTS; 
        }
        else {
            throw new IllegalStateException("Front-End ahead of Replica");   
        }
    }

    private boolean accountExists(String userId) {
        return this.accounts.containsKey(userId);
    }

    public void deleteAccount(String userId)
            throws InactiveServerException, AccountDoesNotExistException, AccountNotEmptyException {
        isServerActive();
        if (!accountExists(userId)) {
            throw new AccountDoesNotExistException();
        }
        // Get account balance
        int balance = this.accounts.get(userId);

        // Only delete if account has no funds
        if (balance > 0) {
            throw new AccountNotEmptyException();
        }
        // Create operation
        Operation deleteAccount = new DeleteOp(userId);
        // Add to ledger
        this.ledger.add(deleteAccount);
        // Remove from accounts
        this.accounts.remove(userId);
    }

    public synchronized List<Integer> transfer(String userId, String destUserId, int value ,  List<Integer> prev) throws InactiveServerException,
            AccountDoesNotExistException, InsufficientBalanceException, DestAccountDoesNotExistException {
        
        printTS(prev);
        isServerActive();

        if(checkIfAhead(prev) == 1 ){
            
            if (!accountExists(userId)) {
                throw new AccountDoesNotExistException();
            }
            if (!accountExists(destUserId)) {
                throw new DestAccountDoesNotExistException();
            }

            // Get accounts balances
            int srcBalance = this.accounts.get(userId);
            int destBalance = this.accounts.get(destUserId);

            if (value <= 0 || userId.equals(destUserId)) {
                // just ignore
                throw new IllegalArgumentException();
            }

            // Check if account has enough funds
            if (srcBalance < value) {
                throw new InsufficientBalanceException();
            }

            repTS.set(server, repTS.get(server) + 1);
            valueTS.set(server, valueTS.get(server) + 1);

            // Create operation
            Operation transfer = new TransferOp(userId, destUserId, value,prev);
            // Add to ledger
            this.ledger.add(transfer);
            // Update accounts
            this.accounts.put(userId, srcBalance - value);
            this.accounts.put(destUserId, destBalance + value);
            return repTS;
        }
        else {
            throw new IllegalStateException("Front-End ahead of Replica");   
        }
    }

    public synchronized List<Integer> shareWithOthersSvState(String name,  int value , List<Integer> prev) throws AccountDoesNotExistException , ValueNotValidException {
        //isServerActive();
        printTS(prev);


        if(checkIfAhead( prev ) == 1 ){
            // Verificar se conta de user "name" existe --- se n existir -> Lançar excpetion AccountDoesNotExist
            if (!accountExists(name)) {
                throw new AccountDoesNotExistException();
            }
            // Verifiar se value é um inteiro entre 1 e 100 -- caso nao seja -> Lançar exception ValueNotValidException
            if( value < 1  || value > 100  ) {
                throw new ValueNotValidException();
            }

            //Obter saldo da conta "name"
            int nameBalance = this.accounts.get(name);

            repTS.set(server, repTS.get(server) + 1);
            valueTS.set(server, valueTS.get(server) + 1);

            // Criar Operacao 
            Operation transferPercentage = new ShareWithOthersOP(name , value , prev);

            // Atualizar ledger da operacao 
            this.ledger.add(transferPercentage);
            
            // Calcular valor a subtrair da conta name - value é uma PERCENTAGEM e nao o valor absoluto
            double valuee = (double) value; 
            double finalValue =nameBalance * (valuee / 100); 
            int finalValueI = (int) finalValue ; 

            
            // atualizar conta name - retirar lhe valor  
            this.accounts.put(name, nameBalance - finalValueI);

            // Adicionar valor equitativo as contas dos outros users 
            int numAccounts = this.accounts.size();
            double valueToAdd = finalValue / ( numAccounts - 1 )  ; // conta a que subtraimos o valor nao contam pois nao vao receber o dinheiro
            int valueToAddI = (int) valueToAdd ;
            
            // Percorrer o hashMap accounts -- vamos buscar o saldo de cada usuario e adicionar-lhe o valor devido
            for (String nameAccount : this.accounts.keySet()) {
                if (nameAccount.equals(name) ){
                    continue;                //Conta que partilha o dinheiro nao vao ter dinheiro adicionado na sua conta
                }
                int accountBalance = this.accounts.get(nameAccount);
                this.accounts.put(nameAccount, accountBalance + valueToAddI);
            }

            return repTS;
        
        }
        else{
            throw new IllegalStateException("Front-End ahead of Replica");    
        }
    }
    
    public synchronized List<Object> getUserBalance(String userId ,  List<Integer> prev ) throws InactiveServerException, AccountDoesNotExistException {
        printTS(prev);
        isServerActive();

        if (checkIfAhead(prev) == 1 ){
            if (!accountExists(userId)) {
                throw new AccountDoesNotExistException();
            }
            int val =  this.accounts.get(userId);
            List<Object> result = new ArrayList<>();
            result.add(val);
            result.add(repTS);
            return result;
        }
        else{
            throw new IllegalStateException("Front-End ahead of Replica");
        }
    }




    public synchronized void gossip(LedgerState state) throws InactiveServerException, AccountAlreadyExistsException, AccountDoesNotExistException, InsufficientBalanceException, DestAccountDoesNotExistException, ValueNotValidException{
        //Servidor contrario 
        int otherServer = server ^ 1 ;// XOR: 0 passa a 1 ; 1 passa a 0 
        int operationUpdate = repTS.get(otherServer); // Updates da outra replica/servidor 
        int numOperations = state.getLedgerCount(); // Num de operacoes no ledger

        // for each Operation that is not in this replica
        while(  operationUpdate < numOperations ){
            //Obter operacao
            DistLedgerCommonDefinitions.Operation operation = state.getLedger(operationUpdate);


            //Se Operacao for "createAccount" (operacao de escrita)
            if(operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)){
                handleCreateAccontOperation(operation, otherServer);
            }

            //Se Operacao for "transferTo" (operacao de escrita)
            else if(operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)){
                handleTransferToOperation(operation, otherServer);
            }
            
            //Se Operacao for "shareWithOthers" (operacao de escrita)
            else if(operation.getType().equals(DistLedgerCommonDefinitions.OperationType.OP_SHARE_WITH_OTHERS)){
                handleShareWithOthersOperation(operation, otherServer);
            }

            operationUpdate++;
        }
    }

    public void handleCreateAccontOperation(DistLedgerCommonDefinitions.Operation operation , int otherServer) throws InactiveServerException, AccountAlreadyExistsException{
        String name = operation.getUserId();
        createAccount(name, operation.getPrevTimeStampList()); 
        
        ledger.remove(ledger.size()-1); // TODO : check this
        
        repTS.set(server, repTS.get(server) - 1); // Decrementar valor da repTS de Server
        repTS.set(otherServer, repTS.get(otherServer) + 1); // Incrementar valor de repTS do outro Server
        valueTS.set(server, valueTS.get(server) - 1);       // Decrementar valor de valueTS de Server
        valueTS.set(otherServer, valueTS.get(otherServer) + 1); //Incrementar valor de valueTS de outro Server
        
    }

    public void handleTransferToOperation(DistLedgerCommonDefinitions.Operation operation,int otherServer) throws InactiveServerException, AccountDoesNotExistException, InsufficientBalanceException, DestAccountDoesNotExistException{
        
        String accFrom = operation.getUserId();
        String accDest = operation.getDestUserId();
        int amount = operation.getAmount();

        transfer(accFrom, accDest, amount, operation.getPrevTimeStampList());

        ledger.remove(ledger.size()-1); //TODO:CHECK THIS

        repTS.set(server, repTS.get(server) - 1); // Decrementar valor da repTS de Server
        repTS.set(otherServer, repTS.get(otherServer) + 1); // Incrementar valor de repTS do outro Server
        
        valueTS.set(server, valueTS.get(server) - 1);       // Decrementar valor de valueTS de Server
        valueTS.set(otherServer, valueTS.get(otherServer) + 1); //Incrementar valor de valueTS de outro Server
    }

    public void handleShareWithOthersOperation(DistLedgerCommonDefinitions.Operation operation, int otherServer) throws AccountDoesNotExistException, ValueNotValidException{

        String accFrom = operation.getUserId();
        int value = operation.getValue();

        shareWithOthersSvState(accFrom, value, operation.getPrevTimeStampList());

        ledger.remove(ledger.size()-1); //TODO:CHECK THIS

        repTS.set(server, repTS.get(server) - 1); // Decrementar valor da repTS de Server
        repTS.set(otherServer, repTS.get(otherServer) + 1); // Incrementar valor de repTS do outro Server
        
        valueTS.set(server, valueTS.get(server) - 1);       // Decrementar valor de valueTS de Server
        valueTS.set(otherServer, valueTS.get(otherServer) + 1); //Incrementar valor de valueTS de outro Server
    }

    
    public void printTS(List<Integer> prev){
        System.out.println("Prev: <" + prev.get(0) + "," + prev.get(1) +  ">");
        System.out.println("valueTS: <" + valueTS.get(0) + "," + valueTS.get(1) + ">");
        System.out.println("replicaTS: <" + repTS.get(0) + "," + repTS.get(1) + ">");
    }


  public void setState(boolean active) {
        this.active = active;
    }

}