package com.yupi.yuaiagent.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoKnowledgeServiceTest {
    @TempDir Path directory;
    private DemoKnowledgeService service;
    private CampusDocumentStore store;

    @BeforeEach
    void setUp() throws IOException {
        var mapper = new ObjectMapper().findAndRegisterModules();
        store = new CampusDocumentStore(mapper, directory.resolve("documents.json").toString());
        service = new DemoKnowledgeService(mapper, store);
    }

    @Test
    void answersKnownQuestionWithCitation() throws IOException {
        var answer = service.ask("住宿生报到要准备什么材料？");
        assertThat(answer.insufficientEvidence()).isFalse();
        assertThat(answer.citations()).isNotEmpty();
        assertThat(answer.answer()).contains("演示资料");
    }

    @Test
    void refusesUnsupportedQuestion() throws IOException {
        var answer = service.ask("游泳池什么时候开放？");
        assertThat(answer.insufficientEvidence()).isTrue();
        assertThat(answer.citations()).isEmpty();
    }

    @Test
    void uploadedDocumentReplacesDemoAndCanBeDeactivated() throws IOException {
        var document = store.add("演示自定义通知", "https://example.org/notice", "演示学校",
                "东校区", "全体新生", LocalDate.now(), null,
                List.of("体检"), "# 演示通知\n\n## 体检安排\n演示资料要求核对体检安排。");
        assertThat(service.ask("体检如何安排？").answer()).contains("核对体检安排");
        assertThat(service.ask("体检如何安排？").citations().getFirst().section()).isEqualTo("体检安排");
        assertThat(service.ask("住宿怎么办？").insufficientEvidence()).isTrue();
        assertThat(store.deactivate(document.id())).isTrue();
        assertThat(service.ask("体检如何安排？").insufficientEvidence()).isTrue();
        assertThat(service.ask("住宿怎么办？").answer()).contains("演示资料");
    }

    @Test
    void newVersionReplacesOldAndIdenticalUploadIsRejected() throws IOException {
        var first = store.add("报到通知", "", "演示学校", "东校区", "本科新生",
                LocalDate.now(), null, List.of("报到"), "旧版报到要求");
        assertThatThrownBy(() -> store.add("报到通知", "", "演示学校", "东校区", "本科新生",
                LocalDate.now(), null, List.of("报到"), "旧版报到要求"))
                .isInstanceOf(IllegalArgumentException.class);
        var second = store.add("报到通知", "", "演示学校", "东校区", "本科新生",
                LocalDate.now(), null, List.of("报到"), "新版报到要求");
        assertThat(second.version()).isEqualTo(2);
        assertThat(store.list().stream().filter(CampusDocumentStore.CampusDocument::active))
                .extracting(CampusDocumentStore.CampusDocument::id).containsExactly(second.id());
        assertThat(first.version()).isEqualTo(1);
    }

    @Test
    void filtersQuestionsByCampusAndAudience() throws IOException {
        store.add("东校区报到", "", "演示学校", "东校区", "本科新生",
                null, null, List.of("报到"), "东校区报到地点在甲楼。");
        store.add("西校区报到", "", "演示学校", "西校区", "研究生",
                null, null, List.of("报到"), "西校区报到地点在乙楼。");
        var answer = service.ask("报到地点在哪里？", "东校区", "本科新生");
        assertThat(answer.answer()).contains("甲楼").doesNotContain("乙楼");
        assertThat(answer.citations()).extracting(DemoKnowledgeService.Citation::title)
                .containsExactly("东校区报到");
        assertThat(service.ask("报到地点在哪里？", "南校区", "本科新生").insufficientEvidence())
                .isTrue();
    }

    @Test
    void expiredDocumentDoesNotSupportAnswer() throws IOException {
        store.add("过期体检通知", "", "演示学校", "全部校区", "全体新生",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
                List.of("体检"), "体检安排已过期。");
        assertThat(service.ask("体检怎么安排？").insufficientEvidence()).isTrue();
    }

    @Test
    void metadataKeywordCanServeAsAnAlias() throws IOException {
        store.add("接驳通知", "", "演示学校", "东校区", "本科新生",
                null, null, List.of("接驳", "班车"), "接驳车在07:30发车。");
        var answer = service.ask("班车几点发车？", "东校区", "本科新生");
        assertThat(answer.insufficientEvidence()).isFalse();
        assertThat(answer.answer()).contains("07:30");
    }
}
