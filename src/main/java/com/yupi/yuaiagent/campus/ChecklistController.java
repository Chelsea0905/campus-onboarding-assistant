package com.yupi.yuaiagent.campus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/campus/checklist")
public class ChecklistController {
    private final ChecklistService service;

    public ChecklistController(ChecklistService service) {
        this.service = service;
    }

    @GetMapping
    public ChecklistService.Checklist create(@RequestParam(defaultValue = "全部校区") String campus,
                                             @RequestParam(defaultValue = "全体新生") String audience,
                                             @RequestParam(defaultValue = "false") boolean housing) throws IOException {
        return service.create(campus, audience, housing);
    }
}
