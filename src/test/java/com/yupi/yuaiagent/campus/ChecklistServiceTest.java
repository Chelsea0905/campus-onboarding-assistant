package com.yupi.yuaiagent.campus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistServiceTest {
    @TempDir Path directory;

    @Test
    void demoChecklistIncludesHousingOnlyWhenRequested() throws IOException {
        var service = new ChecklistService(store());
        assertThat(service.create("东校区", "本科新生", false).items())
                .extracting(ChecklistService.ChecklistItem::title).doesNotContain("核对住宿安排");
        assertThat(service.create("东校区", "本科新生", true).items())
                .extracting(ChecklistService.ChecklistItem::title).contains("核对住宿安排");
    }

    @Test
    void uploadedChecklistRespectsCampusAndDeactivation() throws IOException {
        var store = store();
        var document = store.add("东校区通知", "", "演示学校", "东校区", "全体新生",
                null, null, List.of("报到"),
                "## 报到\n- [ ] 核对报到材料 | 材料：通知书 | 地点：报到点 | 截止：待通知");
        var service = new ChecklistService(store);
        var item = service.create("东校区", "本科新生", false).items().getFirst();
        assertThat(item.title()).isEqualTo("核对报到材料");
        assertThat(item.materials()).isEqualTo("通知书");
        assertThat(item.citation().section()).isEqualTo("报到");
        assertThat(service.create("西校区", "本科新生", false).items()).isEmpty();
        store.deactivate(document.id());
        assertThat(service.create("东校区", "本科新生", false).demo()).isTrue();
    }

    private CampusDocumentStore store() {
        return new CampusDocumentStore(new ObjectMapper().findAndRegisterModules(),
                directory.resolve("documents.json").toString());
    }
}
