package com.yupi.yuaiagent.campus;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CampusAnswerService {
    private static final Pattern REFERENCE = Pattern.compile("\\[(\\d+)\\]");
    private final DemoKnowledgeService knowledge;
    private final List<AiAnswerGenerator> generators;

    public CampusAnswerService(DemoKnowledgeService knowledge, List<AiAnswerGenerator> generators) {
        this.knowledge = knowledge;
        this.generators = generators;
    }

    public DemoKnowledgeService.Answer ask(String question) throws IOException {
        return ask(question, "", "");
    }

    public DemoKnowledgeService.Answer ask(String question, String campus, String audience) throws IOException {
        var extracted = knowledge.ask(question, campus, audience);
        if (extracted.insufficientEvidence() || generators.isEmpty()) return extracted;
        try {
            String answer = generators.getFirst().generate(question, extracted.answer());
            if (!hasValidReferences(answer, extracted.citations().size())) return extracted;
            return new DemoKnowledgeService.Answer(answer, extracted.citations(), false);
        } catch (RuntimeException exception) {
            return extracted;
        }
    }

    private boolean hasValidReferences(String answer, int citationCount) {
        if (answer == null || answer.isBlank()) return false;
        var matcher = REFERENCE.matcher(answer);
        boolean found = false;
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index < 1 || index > citationCount) return false;
            found = true;
        }
        return found;
    }
}
