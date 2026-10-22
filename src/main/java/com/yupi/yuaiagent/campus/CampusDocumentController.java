package com.yupi.yuaiagent.campus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/campus/documents")
public class CampusDocumentController {
    private final CampusDocumentStore store;
    private final DocumentTextExtractor extractor;
    private final String adminToken;

    public CampusDocumentController(CampusDocumentStore store, DocumentTextExtractor extractor,
                                    @Value("${campus.admin-token:}") String adminToken) {
        this.store = store;
        this.extractor = extractor;
        this.adminToken = adminToken;
    }

    @GetMapping
    public List<DocumentSummary> list(@RequestHeader("X-Admin-Token") String token) throws IOException {
        requireAdmin(token);
        return store.list().stream().map(DocumentSummary::from).toList();
    }

    @PostMapping
    public DocumentSummary upload(@RequestHeader("X-Admin-Token") String token,
                                  @RequestParam String title,
                                  @RequestParam(defaultValue = "") String sourceUrl,
                                  @RequestParam(defaultValue = "演示学校") String school,
                                  @RequestParam(defaultValue = "全部校区") String campus,
                                  @RequestParam(defaultValue = "全体新生") String audience,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedAt,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validUntil,
                                  @RequestParam(defaultValue = "") String keywords,
                                  @RequestParam MultipartFile file) throws IOException {
        requireAdmin(token);
        if (file.isEmpty() || file.getSize() > 2_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件须为非空且不超过 2 MB");
        }
        String name = file.getOriginalFilename();
        if (!sourceUrl.isBlank() && !(sourceUrl.startsWith("https://") || sourceUrl.startsWith("http://"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "原文链接必须是 http 或 https 地址");
        }
        try {
            String content = extractor.extract(name, file.getBytes());
            var document = store.add(title, sourceUrl, school, campus, audience, publishedAt,
                    validUntil, Arrays.stream(keywords.split("[,，]")).map(String::trim)
                            .filter(value -> !value.isEmpty()).toList(), content);
            return DocumentSummary.from(document);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/{id}/deactivate")
    public void deactivate(@RequestHeader("X-Admin-Token") String token,
                           @org.springframework.web.bind.annotation.PathVariable String id) throws IOException {
        requireAdmin(token);
        if (!store.deactivate(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void requireAdmin(String token) {
        if (adminToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "请先配置 CAMPUS_ADMIN_TOKEN");
        }
        if (!adminToken.equals(token)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public record DocumentSummary(String id, String title, String sourceUrl, String school,
                                  String campus, String audience, LocalDate publishedAt,
                                  LocalDate validUntil, List<String> keywords, boolean active,
                                  int version) {
        static DocumentSummary from(CampusDocumentStore.CampusDocument document) {
            return new DocumentSummary(document.id(), document.title(), document.sourceUrl(),
                    document.school(), document.campus(), document.audience(), document.publishedAt(),
                    document.validUntil(), document.keywords(), document.active(), document.version());
        }
    }
}
