package ru.test.loadtest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;

    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @GetMapping("/load/{cpuMillis}")
    public ResponseEntity<String> loadTest(
            @PathVariable("cpuMillis") long cpuMillis,
            @RequestParam(value = "isAsync", defaultValue = "false") boolean isAsync,
            @RequestParam(value = "isVirtual", defaultValue = "false") boolean isVirtual,
            @RequestParam(value = "ioMillis", defaultValue = "0") long ioMillis

    ) throws IOException {
        long startWall = System.currentTimeMillis();

        long usedCpu;

        if (isAsync) {
            if (isVirtual) {
                usedCpu = loadTestService.virtualBurnCPU(cpuMillis);
            } else {
                var summaryUsedCpu = loadTestService.asyncBurnCPU(cpuMillis);
                usedCpu = summaryUsedCpu.stream().reduce(0L, Long::sum);
            }
        } else {
            usedCpu = loadTestService.burnCPU(cpuMillis);
        }

        long awaitTimeStart = System.currentTimeMillis();
        if (ioMillis != 0) {
            loadTestService.ioBound(ioMillis);
        }
        long awaitTime = System.currentTimeMillis() - awaitTimeStart;

        long wallTime = System.currentTimeMillis() - startWall;

        return ResponseEntity.ok(String.format(
                "Target CPU: %d ms, Async: %s, Target wait %d ms, Used CPU: %d ms, Wall time: %d ms, Waiting time: %d ms",
                cpuMillis, isAsync, ioMillis, usedCpu, wallTime, awaitTime
        ));
    }

    @GetMapping("/load/io/{await}")
    public ResponseEntity<String> loadTestIoBound(@PathVariable("ioMillis") long ioMillis) throws IOException {
        long startWall = System.currentTimeMillis();

        long readMB = loadTestService.ioBound(ioMillis) / (1024 * 1024);

        long wallTime = System.currentTimeMillis() - startWall;

        return ResponseEntity.ok(String.format(
                "Target IO: %d ms, Target wait %d ms, Read : %d mb, Wall time: %d ms",
                ioMillis, ioMillis, readMB, wallTime
        ));
    }
}
