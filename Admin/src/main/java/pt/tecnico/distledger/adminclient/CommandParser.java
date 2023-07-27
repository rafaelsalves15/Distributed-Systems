package pt.tecnico.distledger.adminclient;

import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.contract.admin.AdminDistLedger;

import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";


    private final AdminService adminService;

    public CommandParser(AdminService adminService) {
        this.adminService = adminService;
    }

    void parseInput() {

        try (Scanner scanner = new Scanner(System.in)) {
            boolean exit = false;

            while (!exit) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                String cmd = line.split(SPACE)[0];

                switch (cmd) {
                    case ACTIVATE:
                        this.activate(line);
                        break;

                    case DEACTIVATE:
                        this.deactivate(line);
                        break;

                    case GET_LEDGER_STATE:
                        this.getLedgerState(line);
                        break;

                    case GOSSIP:
                        this.gossip(line);
                        break;

                    case EXIT:
                        exit = true;
                        break;
                    
                    case HELP:

                    default:
                        this.printUsage();
                        break;
                }

            }
        }
    }

    private void invalidCommand() {
        System.out.println("Invalid command. Type 'help' for a list of available commands.");
    }

    private void activate(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.invalidCommand();
            return;
        }
        String server = split[1];

        AdminDistLedger.ActivateRequest.Builder builder = AdminDistLedger.ActivateRequest.newBuilder();
        AdminDistLedger.ActivateRequest request = builder.build();

        try {
            this.adminService.activate(request, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void deactivate(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.invalidCommand();
            return;
        }
        String server = split[1];

        AdminDistLedger.DeactivateRequest.Builder builder = AdminDistLedger.DeactivateRequest.newBuilder();
        AdminDistLedger.DeactivateRequest request = builder.build();
        try {
            this.adminService.deactivate(request, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void getLedgerState(String line) {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.invalidCommand();
            return;
        }
        String server = split[1];

        AdminDistLedger.GetLedgerStateRequest.Builder builder = AdminDistLedger.GetLedgerStateRequest.newBuilder();
        AdminDistLedger.GetLedgerStateRequest request = builder.build();
        try {
            AdminDistLedger.GetLedgerStateResponse response = this.adminService.getLedgerState(request, server);
            System.out.println("OK");
            System.out.println(response);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }


    private void gossip(String line) {
        String[] args = line.split(SPACE);

        // Obter server a partir dos argumentos
        String server = args[1];

        // Construir Request
    
        AdminDistLedger.GossipRequest.Builder builder = AdminDistLedger.GossipRequest.newBuilder();
        builder.setQualifier(server);
        AdminDistLedger.GossipRequest request = builder.build();

        this.adminService.gossip(request, server);
    

    
    }



    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}
