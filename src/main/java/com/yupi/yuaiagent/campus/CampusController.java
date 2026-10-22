package com.yupi.yuaiagent.campus;

import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campus")
public class CampusController {
    private final CampusAnswerService answerService;

    public CampusController(CampusAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/ask")
    public DemoKnowledgeService.Answer ask(@RequestParam String q,
                                           @RequestParam(defaultValue = "") String campus,
                                           @RequestParam(defaultValue = "") String audience) throws IOException {
        return answerService.ask(q, campus, audience);
    }
}
