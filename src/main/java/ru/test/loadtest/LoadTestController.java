package ru.test.loadtest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(value = "await", defaultValue = "0") long await

    ) {
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

        long wallTime = System.currentTimeMillis() - startWall;

        return ResponseEntity.ok(String.format(
                "Target CPU: %d ms, Async: %s, Target wait %d ms, Used CPU: %d ms, Wall time: %d ms",
                cpuMillis, isAsync, await, usedCpu, wallTime
        ));
    }
}
