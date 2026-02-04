package ru.test.loadtest;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class LoadTestService {

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor();

    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(availableProcessors + 1);

    private static final byte[] IO_BOUND = new byte[1024 * 1024];

    public LoadTestService() {
        threadMXBean.setThreadCpuTimeEnabled(true);
    }

    public long burnCPU(long targetMillis) {
        long targetNanos = targetMillis * 1_000_000;
        long startCpuNanos = threadMXBean.getCurrentThreadCpuTime();

        long waste = 0;
        while ((threadMXBean.getCurrentThreadCpuTime() - startCpuNanos) < targetNanos) {
            waste += System.nanoTime();
            waste += wasteSomeCpu(waste);
        }

        long usedCpuNanos = threadMXBean.getCurrentThreadCpuTime() - startCpuNanos;
        return usedCpuNanos / 1_000_000;
    }

    public List<Long> asyncBurnCPU(long targetMillis) {
        var results = new ArrayList<CompletableFuture<Long>>();

        for (var i = 0; i < availableProcessors; i++) {
            results.add(
                    CompletableFuture.supplyAsync(() -> burnCPU(targetMillis), fixedThreadPool)
            );
        }

        CompletableFuture.allOf(
                results.toArray(new CompletableFuture[0])
        ).join();

        return results.stream().map(CompletableFuture::join).toList();
    }

    public long virtualBurnCPU(long targetMillis)  {
        var result = CompletableFuture.supplyAsync(() -> burnCPU(targetMillis), virtualPool);
        return result.join();
    }

    public long ioBound(long targetMillis) throws IOException {
        var start = System.currentTimeMillis();

        int summaryBytesRead = 0;
        while (System.currentTimeMillis() - start < targetMillis) {
            var in = generateInputStream();
            var read = in.read(IO_BOUND);

            if (read == -1) {
                in = generateInputStream();
                read = in.read(IO_BOUND);
            }
            summaryBytesRead += read;
        }

        return summaryBytesRead;
    }

    private long wasteSomeCpu(long seed) {
        double x = seed;
        for (int i = 0; i < 100; i++) {
            x = Math.pow(Math.sin(x), 2.0) + Math.sqrt(x + 1);
        }
        return (long) x;
    }

    private InputStream generateInputStream() {
        return new ByteArrayInputStream(IO_BOUND);
    }
}
