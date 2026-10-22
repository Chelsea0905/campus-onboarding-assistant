package com.yupi.yuaiagent.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampusAnswerServiceTest {
    @TempDir Path directory;

    @Test
    void acceptsGroundedModelAnswerAndFallsBackWithoutValidCitation() throws IOException {
        var knowledge = knowledge();
        var good = new CampusAnswerService(knowledge, List.of((question, evidence) -> "请核对住宿安排。[1]"));
        assertThat(good.ask("住宿怎么安排？").answer()).isEqualTo("请核对住宿安排。[1]");

        var bad = new CampusAnswerService(knowledge, List.of((question, evidence) -> "9月1日入住。[99]"));
        assertThat(bad.ask("住宿怎么安排？").answer()).contains("以下仅为演示资料");
    }

    @Test
    void skipsModelWhenEvidenceIsMissing() throws IOException {
        var service = new CampusAnswerService(knowledge(),
                List.of((question, evidence) -> { throw new AssertionError("模型不应被调用"); }));
        assertThat(service.ask("游泳池何时开放？").insufficientEvidence()).isTrue();
    }

    private DemoKnowledgeService knowledge() throws IOException {
        var mapper = new ObjectMapper().findAndRegisterModules();
        return new DemoKnowledgeService(mapper,
                new CampusDocumentStore(mapper, directory.resolve("documents.json").toString()));
    }
}
