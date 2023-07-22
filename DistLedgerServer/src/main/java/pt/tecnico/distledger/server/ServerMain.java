package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        // Get the port number from the command line, if provided
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        String qualifier = args[1];

        ServerState serverState = new ServerState(qualifier);

        // Admin implementation
        BindableService adminService = new AdminServiceImpl(serverState);
        // User implementation
        BindableService userService = new UserServiceImpl(serverState);

        // Create the server
        Server server = ServerBuilder.forPort(port)
                .addService(adminService)
                .addService(userService)
                .build();

        // Start the server
        try {
            server.start();
            System.out.println("Server started on port " + port);
            server.awaitTermination();
        } catch (IOException e) {
            System.out.println("Server failed to start: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Server was interrupted: " + e.getMessage());
            // Rethrow the exception to signal the system that something is wrong
            Thread.currentThread().interrupt();
        } finally {
            server.shutdown();
        }
    }
}