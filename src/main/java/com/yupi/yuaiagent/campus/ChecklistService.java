package com.yupi.yuaiagent.campus;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChecklistService {
    private final CampusDocumentStore store;

    public ChecklistService(CampusDocumentStore store) {
        this.store = store;
    }

    public Checklist create(String campus, String audience, boolean housing) throws IOException {
        var activeDocuments = store.list().stream()
                .filter(CampusDocumentStore.CampusDocument::active)
                .filter(document -> document.validUntil() == null ||
                        !document.validUntil().isBefore(LocalDate.now()))
                .toList();
        if (activeDocuments.isEmpty()) {
            List<ChecklistItem> demoItems = new ArrayList<>();
            demoItems.add(demo("核对当年报到通知", "演示资料：报到说明", "材料准备"));
            demoItems.add(demo("查看校园卡办理说明", "演示资料：校园卡说明", "校园卡"));
            if (housing) demoItems.add(demo("核对住宿安排", "演示资料：住宿说明", "住宿办理"));
            return new Checklist(true, demoItems,
                    "当前为演示清单，所有地点、材料和时间均需以目标学校官方通知核实。");
        }
        List<ChecklistItem> items = new ArrayList<>();
        for (var document : activeDocuments) {
            if (!applies(document.campus(), campus, "全部校区")) continue;
            if (!applies(document.audience(), audience, "全体新生") &&
                    !(housing && "住宿生".equals(document.audience()))) continue;
            String section = "开头";
            for (String line : document.content().split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("## ")) section = trimmed.substring(3).trim();
                if (!trimmed.startsWith("- [ ] ")) continue;
                String[] parts = trimmed.substring(6).split("\\|");
                String title = parts[0].trim();
                if (title.isBlank()) continue;
                String materials = null, location = null, deadline = null;
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.startsWith("材料：")) materials = part.substring(3).trim();
                    if (part.startsWith("地点：")) location = part.substring(3).trim();
                    if (part.startsWith("截止：")) deadline = part.substring(3).trim();
                }
                items.add(new ChecklistItem(title, materials, location, deadline,
                        new DemoKnowledgeService.Citation(document.title(), section, document.sourceUrl())));
            }
        }
        return new Checklist(false, items, items.isEmpty()
                ? "适用资料中尚无清单事项。请在 Markdown 中使用“- [ ] 事项”标记，或查看原文。"
                : "以下事项摘自已上传资料；未填写的材料、地点和截止时间需要查阅原文核实。");
    }

    private boolean applies(String documentValue, String requestValue, String allValue) {
        return documentValue == null || documentValue.isBlank() || documentValue.equals(allValue)
                || documentValue.equals(requestValue);
    }

    private ChecklistItem demo(String title, String document, String section) {
        return new ChecklistItem(title, null, null, null,
                new DemoKnowledgeService.Citation(document, section, ""));
    }

    public record Checklist(boolean demo, List<ChecklistItem> items, String note) {}
    public record ChecklistItem(String title, String materials, String location, String deadline,
                                DemoKnowledgeService.Citation citation) {}
}
