package com.yupi.yuaiagent.campus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CampusDocumentStore {
    private final ObjectMapper mapper;
    private final Path path;

    public CampusDocumentStore(ObjectMapper mapper,
                               @Value("${campus.storage.path:./tmp/campus-documents.json}") String storagePath) {
        this.mapper = mapper;
        this.path = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public synchronized List<CampusDocument> list() throws IOException {
        if (!Files.exists(path)) return List.of();
        return mapper.readValue(path.toFile(), new TypeReference<>() {});
    }

    public synchronized CampusDocument add(String title, String sourceUrl, String school, String campus,
                                           String audience, LocalDate publishedAt, LocalDate validUntil,
                                           List<String> keywords, String content) throws IOException {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("标题和正文不能为空");
        }
        if (publishedAt != null && validUntil != null && validUntil.isBefore(publishedAt)) {
            throw new IllegalArgumentException("有效期不能早于发布日期");
        }
        String checksum = sha256(content);
        var all = new ArrayList<>(list());
        int version = 1;
        for (int i = 0; i < all.size(); i++) {
            var old = all.get(i);
            if (!old.title().equals(title.trim()) || !old.school().equals(school) ||
                    !old.campus().equals(campus) || !old.audience().equals(audience)) continue;
            if (checksum.equals(old.checksum() == null ? sha256(old.content()) : old.checksum())) {
                throw new IllegalArgumentException("相同资料已经上传");
            }
            version = Math.max(version, Math.max(old.version(), 1) + 1);
            if (old.active()) all.set(i, copyWithActive(old, false));
        }
        var document = new CampusDocument(UUID.randomUUID().toString(), title.trim(), sourceUrl,
                school, campus, audience, publishedAt, validUntil, keywords, content, true,
                checksum, version);
        all.add(document);
        save(all);
        return document;
    }

    public synchronized boolean deactivate(String id) throws IOException {
        var all = new ArrayList<>(list());
        for (int i = 0; i < all.size(); i++) {
            var current = all.get(i);
            if (current.id().equals(id) && current.active()) {
                all.set(i, copyWithActive(current, false));
                save(all);
                return true;
            }
        }
        return false;
    }

    private void save(List<CampusDocument> all) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "campus-documents-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), all);
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private CampusDocument copyWithActive(CampusDocument document, boolean active) {
        return new CampusDocument(document.id(), document.title(), document.sourceUrl(),
                document.school(), document.campus(), document.audience(), document.publishedAt(),
                document.validUntil(), document.keywords(), document.content(), active,
                document.checksum(), document.version());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record CampusDocument(String id, String title, String sourceUrl, String school,
                                 String campus, String audience, LocalDate publishedAt,
                                 LocalDate validUntil, List<String> keywords, String content,
                                 boolean active, String checksum, int version) {}
}
