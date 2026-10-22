package com.yupi.yuaiagent.campus;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTextExtractorTest {
    private final DocumentTextExtractor extractor = new DocumentTextExtractor();

    @Test
    void extractsPageNumberAndTextFromPdf() throws IOException {
        var output = new ByteArrayOutputStream();
        try (var document = new Document(new PdfDocument(new PdfWriter(output)))) {
            document.add(new Paragraph("Campus registration notice"));
        }
        String text = extractor.extract("notice.pdf", output.toByteArray());
        assertThat(text).contains("第 1 页", "Campus registration notice");
    }
}
