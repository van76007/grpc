package www.grpc.server;

public class Starter {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
