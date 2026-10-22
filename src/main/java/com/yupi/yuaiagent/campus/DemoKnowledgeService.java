package com.yupi.yuaiagent.campus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

@Service
public class DemoKnowledgeService {
    private final List<Section> sections;
    private final CampusDocumentStore documentStore;
    private final List<VectorEvidenceService> vectorServices;

    @Autowired
    public DemoKnowledgeService(ObjectMapper objectMapper, CampusDocumentStore documentStore,
                                List<VectorEvidenceService> vectorServices) throws IOException {
        this.documentStore = documentStore;
        this.vectorServices = vectorServices;
        try (var input = new ClassPathResource("campus/demo-knowledge.json").getInputStream()) {
            sections = objectMapper.readValue(input, new TypeReference<>() {});
        }
    }

    public DemoKnowledgeService(ObjectMapper objectMapper, CampusDocumentStore documentStore) throws IOException {
        this(objectMapper, documentStore, List.of());
    }

    public Answer ask(String question) throws IOException {
        return ask(question, "", "");
    }

    public Answer ask(String question, String campus, String audience) throws IOException {
        if (question == null || question.isBlank()) {
            return new Answer("请先输入想查询的入学问题。", List.of(), true);
        }
        var documents = documentStore.list();
        var activeDocuments = documents.stream()
                .filter(CampusDocumentStore.CampusDocument::active)
                .filter(document -> document.validUntil() == null ||
                        !document.validUntil().isBefore(LocalDate.now()))
                .toList();
        var applicableDocuments = activeDocuments.stream()
                .filter(document -> applies(document.campus(), campus, "全部校区"))
                .filter(document -> applies(document.audience(), audience, "全体新生"))
                .toList();
        var available = activeDocuments.isEmpty() ? sections : applicableDocuments.stream()
                .flatMap(document -> splitSections(document).stream()).toList();
        var matches = available.stream()
                .map(section -> new Match(section, score(question, section)))
                .filter(match -> match.score() > 0)
                .sorted(Comparator.comparingInt(Match::score).reversed())
                .limit(2)
                .map(Match::section)
                .toList();
        if (matches.isEmpty() && !vectorServices.isEmpty()) {
            try {
                var semantic = vectorServices.getFirst().search(question, available);
                if (!semantic.isEmpty()) matches = semantic;
            } catch (RuntimeException ignored) {
                // 本地向量服务不可用时仍保留关键词检索。
            }
        }
        if (matches.isEmpty()) {
            return new Answer("演示资料中没有找到足够依据，请查看学校官方通知或联系学校确认。", List.of(), true);
        }
        StringBuilder text = new StringBuilder(activeDocuments.isEmpty()
                ? "以下仅为演示资料，请勿作为真实学校规定：\n"
                : "以下摘自已上传资料，请核对对应来源：\n");
        for (int i = 0; i < matches.size(); i++) {
            text.append(i + 1).append(". ").append(matches.get(i).content())
                    .append(" [").append(i + 1).append("]\n");
        }
        return new Answer(text.toString().trim(), matches.stream()
                .map(section -> new Citation(section.title(), section.section(), section.sourceUrl())).toList(), false);
    }

    private boolean applies(String documentValue, String requested, String universal) {
        return requested == null || requested.isBlank() || documentValue == null ||
                documentValue.isBlank() || documentValue.equals(universal) || documentValue.equals(requested);
    }

    private int score(String question, Section section) {
        return section.keywords().stream()
                .filter(question::contains)
                .mapToInt(keyword -> keyword.length() *
                        (section.content().contains(keyword) || section.section().contains(keyword) ? 3 : 1))
                .sum();
    }

    private List<Section> splitSections(CampusDocumentStore.CampusDocument document) {
        return Arrays.stream(document.content().split("(?m)(?=^##\\s+)"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    String firstLine = value.lines().findFirst().orElse("");
                    String sectionName = firstLine.startsWith("## ")
                            ? firstLine.substring(3).trim() : "开头";
                    String cleanContent = value.replaceAll("(?m)^#{1,6}\\s*", "").trim();
                    return new Section(document.title(), sectionName, document.sourceUrl(),
                            document.keywords(), cleanContent);
                }).toList();
    }

    public record Section(String title, String section, String sourceUrl, List<String> keywords, String content) {}
    public record Citation(String title, String section, String sourceUrl) {}
    public record Answer(String answer, List<Citation> citations, boolean insufficientEvidence) {}
    private record Match(Section section, int score) {}
}
