package io.foldright.demo;

import io.foldright.cffu.Cffu;
import io.foldright.cffu.CffuFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.foldright.cffu.CffuFactoryBuilder.newCffuFactoryBuilder;


public class Demo {
    private static final ExecutorService myBizThreadPool = Executors.newFixedThreadPool(42);

    // Create a CffuFactory with configuration of the customized thread pool
    private static final CffuFactory cffuFactory = newCffuFactoryBuilder(myBizThreadPool).build();

    public static void main(String[] args) throws Exception {
        // Run in myBizThreadPool
        Cffu<Integer> cf0 = cffuFactory.supplyAsync(() -> 21);

        Cffu<Integer> cf42 = cf0.thenApply(n -> n * 2);

        // Run in myBizThreadPool
        Cffu<Integer> longTaskA = cf42.thenApplyAsync(n -> {
            sleep(1001);
            return n / 2;
        });

        // Run in myBizThreadPool
        Cffu<Integer> longTaskB = cf42.thenApplyAsync(n -> {
            sleep(1002);
            return n / 2;
        });


        Cffu<Integer> finalCf = longTaskA.thenCombine(longTaskB, Integer::sum);

        Integer result = finalCf.get();
        System.out.println(result);

        ////////////////////////////////////////
        // cleanup
        myBizThreadPool.shutdown();
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
