package pt.tecnico.distledger.userclient;

import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.userclient.grpc.UserService;

import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";
    private static final String SHAREWITHOTHERS = "shareWithOthers";

    private final UserService userService;

    public CommandParser(UserService userService) {
        this.userService = userService;
    }

    public void parseInput() {

        try (Scanner scanner = new Scanner(System.in)) {
            boolean exit = false;

            while (!exit) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                String cmd = line.split(SPACE)[0];

                try {
                    switch (cmd) {
                        case CREATE_ACCOUNT:
                            this.createAccount(line);
                            break;

                        case DELETE_ACCOUNT:
                            this.deleteAccount(line);
                            break;

                        case TRANSFER_TO:
                            this.transferTo(line);
                            break;

                        case BALANCE:
                            this.balance(line);
                            break;
                        
                        case SHAREWITHOTHERS:
                            this.shareWithOthers(line);
                            break;


                        case EXIT:
                            exit = true;
                            break;


                        case HELP:

                        default:
                            this.printUsage();
                            break;
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void invalidCommand() {
        System.out.println("Invalid command. Type 'help' for a list of available commands.");
    }

    private void createAccount(String input) {
        String[] args = input.split(SPACE);
        if (args.length != 3) {
            this.invalidCommand();
            return;
        }
        String server = args[1];

        String userId = args[2];
        UserDistLedger.CreateAccountRequest.Builder builder = UserDistLedger.CreateAccountRequest.newBuilder();
        builder.setUserId(userId);
        UserDistLedger.CreateAccountRequest request = builder.build();

        try {
            this.userService.createAccount(request, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }

    }

    private void deleteAccount(String input) {
        String[] args = input.split(SPACE);
        if (args.length != 3) {
            this.invalidCommand();
            return;
        }

        String server = args[1];

        String userId = args[2];
        UserDistLedger.DeleteAccountRequest.Builder builder = UserDistLedger.DeleteAccountRequest.newBuilder();
        builder.setUserId(userId);
        UserDistLedger.DeleteAccountRequest request = builder.build();

        try {
            this.userService.deleteAccount(request, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void transferTo(String input) {
        String[] args = input.split(SPACE);
        if (args.length != 5) {
            this.invalidCommand();
            return;
        }
        String server = args[1];

        String fromUserId = args[2];
        String toUserId = args[3];
        int amount = Integer.parseInt(args[4]);

        UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest.newBuilder()
                .setAccountFrom(fromUserId)
                .setAccountTo(toUserId)
                .setAmount(amount)
                .build();

        try {
            this.userService.transferTo(request, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void balance(String input) {
        String[] args = input.split(SPACE);
        if (args.length != 3) {
            this.invalidCommand();
            return;
        }

        String server = args[1];
        String userId = args[2];
        UserDistLedger.BalanceRequest.Builder builder = UserDistLedger.BalanceRequest.newBuilder();
        builder.setUserId(userId);
        UserDistLedger.BalanceRequest request = builder.build();

        try {
            UserDistLedger.BalanceResponse response = this.userService.balance(request, server);
            System.out.println("OK\n" + response.getValue());
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void shareWithOthers(String line){
        String[] args = line.split(SPACE);  

        // tratar de input (line)
        String qualifier = args[1];    
        String name = args[2];
        int value = Integer.parseInt(args[3]);

        //  Construir Request com os devidos argumentos
        UserDistLedger.ShareWithOthersRequest.Builder builder = UserDistLedger.ShareWithOthersRequest.newBuilder();
        builder.setName(name);
        builder.setValue(value);
        UserDistLedger.ShareWithOthersRequest request = builder.build();
        
        try {
            this.userService.shareWithOthers(request, qualifier);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("CP catch");
            System.out.println(e.getStatus().getDescription());
        }


    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- createAccount <server> <username>\n" +
                "- deleteAccount <server> <username>\n" +
                "- balance <server> <username>\n" +
                "- transferTo <server> <username_from> <username_to> <amount>\n" +
                "- shareWithOthers <server>\n" +
                "- exit\n");
    }
}
