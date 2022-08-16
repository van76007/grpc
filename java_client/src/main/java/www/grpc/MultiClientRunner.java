package www.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiClientRunner {
    public static void main(String[] args)
    {
        CountDownLatch latch = new CountDownLatch(1);
        MyThread t1 = new MyThread(latch);
        MyThread t2 = new MyThread(latch);
        new Thread(t1).start();
        new Thread(t2).start();
        latch.countDown();          //This will inform all the threads to start
    }
}

class MyThread implements Runnable
{
    CountDownLatch latch;
    public MyThread(CountDownLatch latch)
    {
        this.latch = latch;
    }
    @Override
    public void run()
    {
        final int NUM_CONCURRENCY = 16;

        // Use e.g. "dns:///192.168.1.35:8090" if server running on another machine
        final String GRPC_SERVER = "dns:///192.168.1.34:8090";

        try
        {
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(GRPC_SERVER).usePlaintext().build();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long DURATION_SECONDS = 100;
        KvClient client = null;
        try {
            AtomicBoolean done = new AtomicBoolean();
            client = new KvClient(channel, NUM_CONCURRENCY, String.valueOf(Thread.currentThread().getId()));
            System.out.println("Starting");
            scheduler.schedule(() -> done.set(true), DURATION_SECONDS, TimeUnit.SECONDS);
            client.doClientWork(done);
            long requestCount = client.getRpcCount();
            double qps = (double) requestCount / DURATION_SECONDS;
            System.out.println("Completed " + qps + " RPCs/s");
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            scheduler.shutdownNow();
            channel.shutdownNow();
        }
    }
}
