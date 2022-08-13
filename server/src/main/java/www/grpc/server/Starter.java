package www.grpc.server;

public class Starter {
    public static void main(String[] args) {
        Server s = new Server();
        s.start();
        Runtime.getRuntime().addShutdownHook(new Thread(s::stop));
    }
}
