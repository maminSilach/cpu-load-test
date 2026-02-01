package ru.test.loadtest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;

    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @GetMapping("/load/{cpuMillis}")
    public ResponseEntity<String> loadTest(@PathVariable("cpuMillis") long cpuMillis) {
        loadTestService.burnCPU(cpuMillis);
    }
}
