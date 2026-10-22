package com.yupi.yuaiagent.campus;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "campus.vector.enabled", havingValue = "true")
public class VectorEvidenceService {
    private final EmbeddingModel embeddingModel;
    private final double minSimilarity;
    private List<DemoKnowledgeService.Section> indexedSections = List.of();
    private SimpleVectorStore index;

    public VectorEvidenceService(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel,
                                 @Value("${campus.vector.min-similarity:0.75}") double minSimilarity) {
        this.embeddingModel = embeddingModel;
        this.minSimilarity = minSimilarity;
    }

    public synchronized List<DemoKnowledgeService.Section> search(
            String question, List<DemoKnowledgeService.Section> sections) {
        if (sections.isEmpty()) return List.of();
        if (index == null || !indexedSections.equals(sections)) {
            var next = SimpleVectorStore.builder(embeddingModel).build();
            next.add(sections.stream().map(section -> new Document(section.content(), Map.of(
                    "title", safe(section.title()),
                    "section", safe(section.section()),
                    "sourceUrl", safe(section.sourceUrl())
            ))).toList());
            index = next;
            indexedSections = List.copyOf(sections);
        }
        return index.similaritySearch(SearchRequest.builder()
                .query(question).topK(1).similarityThreshold(minSimilarity).build()).stream()
                .map(document -> new DemoKnowledgeService.Section(
                        (String) document.getMetadata().get("title"),
                        (String) document.getMetadata().get("section"),
                        (String) document.getMetadata().get("sourceUrl"),
                        List.of(), document.getText()))
                .toList();
    }

    private String safe(String value) { return value == null ? "" : value; }
}
