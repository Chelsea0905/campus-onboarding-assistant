package com.yupi.yuaiagent.campus;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DocumentTextExtractor {
    public String extract(String filename, byte[] bytes) throws IOException {
        if (filename == null) throw new IllegalArgumentException("缺少文件名");
        String lower = filename.toLowerCase();
        if (lower.endsWith(".md") || lower.endsWith(".txt")) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (lower.endsWith(".pdf")) {
            try (PdfDocument document = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
                StringBuilder content = new StringBuilder();
                for (int page = 1; page <= document.getNumberOfPages(); page++) {
                    content.append("## 第 ").append(page).append(" 页\n")
                            .append(PdfTextExtractor.getTextFromPage(document.getPage(page))).append("\n");
                }
                return content.toString();
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("PDF 内容无法读取", exception);
            }
        }
        throw new IllegalArgumentException("仅支持 Markdown、TXT 和可提取文字的 PDF");
    }
}
