
package com.example.cdc;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cdc")
public class CdcController {

    private final FlinkJobRunner runner;

    public CdcController(FlinkJobRunner runner) {
        this.runner = runner;
    }

    @PostMapping("/start")
    public String start() {
        return runner.startJob();
    }

    @PostMapping("/stop")
    public String stop() {
        return runner.stopJob();
    }
}
