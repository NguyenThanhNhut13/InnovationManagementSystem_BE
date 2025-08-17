package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.FileConversionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

@Service
@Slf4j
public class FileConversionService {

    public FileConversionResponse convertDocxToHtml(MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IdInvalidException("File không được để trống");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".docx")) {
                throw new IdInvalidException("Chỉ hỗ trợ file .docx");
            }

            // Convert file to HTML
            String htmlContent = processDocxFile(file.getInputStream());

            return FileConversionResponse.builder()
                    .htmlContent(htmlContent)
                    .originalFileName(fileName)
                    .fileSize(formatFileSize(file.getSize()))
                    .conversionStatus("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi file: {}", e.getMessage(), e);
            return FileConversionResponse.builder()
                    .htmlContent(null)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(formatFileSize(file.getSize()))
                    .conversionStatus("ERROR")
                    .build();
        }
    }

    private String processDocxFile(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder htmlBuilder = new StringBuilder();

            // Add HTML header with comprehensive CSS for Word-like styling
            htmlBuilder.append("<!DOCTYPE html><html><head>");
            htmlBuilder.append("<meta charset=\"UTF-8\">");
            htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            htmlBuilder.append("<title>Document converted from Word</title>");
            htmlBuilder.append("<style>");
            htmlBuilder.append("body {");
            htmlBuilder.append("    font-family: 'Times New Roman', serif;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    background: #f0f0f0;");
            htmlBuilder.append("    text-rendering: optimizeLegibility;");
            htmlBuilder.append("    -webkit-font-smoothing: antialiased;");
            htmlBuilder.append("    min-height: 100vh;");
            htmlBuilder.append("    display: flex;");
            htmlBuilder.append("    justify-content: center;");
            htmlBuilder.append("    align-items: flex-start;");
            htmlBuilder.append("    padding: 20px;");
            htmlBuilder.append("}");
            htmlBuilder.append(".page-container {");
            htmlBuilder.append("    width: 210mm;");
            htmlBuilder.append("    height: 297mm;");
            htmlBuilder.append("    background: white;");
            htmlBuilder.append("    box-shadow: 0 0 10px rgba(0,0,0,0.3);");
            htmlBuilder.append("    margin: 0 auto;");
            htmlBuilder.append("    padding: 20mm;");
            htmlBuilder.append("    box-sizing: border-box;");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("    overflow: hidden;");
            htmlBuilder.append("}");
            htmlBuilder.append(".page-content {");
            htmlBuilder.append("    width: 100%;");
            htmlBuilder.append("    height: 100%;");
            htmlBuilder.append("    overflow: hidden;");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-text {");
            htmlBuilder.append("    font-size: 13pt;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    clear: both;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    margin-bottom: 2mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-text.underline {");
            htmlBuilder.append("    text-decoration: underline;");
            htmlBuilder.append("    text-decoration-thickness: 1px;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-text {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-text + br + .header-text {");
            htmlBuilder.append("    margin-top: 3mm;");
            htmlBuilder.append("}");
            htmlBuilder.append("br {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    content: \"\";");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    height: 0;");
            htmlBuilder.append("    line-height: 0;");
            htmlBuilder.append("}");
            htmlBuilder.append("br + .header-text {");
            htmlBuilder.append("    margin-top: 2mm;");
            htmlBuilder.append("}");
            htmlBuilder.append("p {");
            htmlBuilder.append("    margin: 0 0 6pt 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    text-align: justify;");
            htmlBuilder.append("    text-indent: 15mm;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    orphans: 2;");
            htmlBuilder.append("    widows: 2;");
            htmlBuilder.append("}");
            htmlBuilder.append("p.no-indent { text-indent: 0; }");
            htmlBuilder.append("p.center { text-align: center; }");
            htmlBuilder.append("p.right { text-align: right; }");
            htmlBuilder.append("p.left { text-align: left; }");
            htmlBuilder.append("p.justify { text-align: justify; }");
            htmlBuilder.append("h1, h2, h3, h4, h5, h6 {");
            htmlBuilder.append("    margin: 12pt 0 6pt 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    text-align: center;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    page-break-after: avoid;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("}");
            htmlBuilder.append("h1 { font-size: 18pt; }");
            htmlBuilder.append("h2 { font-size: 16pt; }");
            htmlBuilder.append("h3 { font-size: 14pt; }");
            htmlBuilder.append("h4 { font-size: 13pt; }");
            htmlBuilder.append("h5 { font-size: 12pt; }");
            htmlBuilder.append("h6 { font-size: 11pt; }");
            htmlBuilder.append("table {");
            htmlBuilder.append("    border-collapse: collapse;");
            htmlBuilder.append("    width: 100%;");
            htmlBuilder.append("    margin: 12pt 0;");
            htmlBuilder.append("    border: 1px solid #000;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    table-layout: fixed;");
            htmlBuilder.append("    word-wrap: break-word;");
            htmlBuilder.append("}");
            htmlBuilder.append("td, th {");
            htmlBuilder.append("    border: 1px solid #000;");
            htmlBuilder.append("    padding: 6pt;");
            htmlBuilder.append("    text-align: left;");
            htmlBuilder.append("    vertical-align: top;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("}");
            htmlBuilder.append("th {");
            htmlBuilder.append("    background-color: transparent;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    text-align: center;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    font-size: 11pt;");
            htmlBuilder.append("}");
            htmlBuilder.append("img {");
            htmlBuilder.append("    max-width: 100%;");
            htmlBuilder.append("    height: auto;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    margin: 12pt auto;");
            htmlBuilder.append("}");
            htmlBuilder.append("ul, ol {");
            htmlBuilder.append("    margin: 12pt 0;");
            htmlBuilder.append("    padding-left: 15mm;");
            htmlBuilder.append("    list-style: none;");
            htmlBuilder.append("    counter-reset: list-item;");
            htmlBuilder.append("}");
            htmlBuilder.append("li {");
            htmlBuilder.append("    margin: 6pt 0;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("}");
            htmlBuilder.append("li:before {");
            htmlBuilder.append("    content: \"- \";");
            htmlBuilder.append("    position: absolute;");
            htmlBuilder.append("    left: 0;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("}");
            htmlBuilder.append("ul li:before {");
            htmlBuilder.append("    content: \"- \";");
            htmlBuilder.append("}");
            htmlBuilder.append("ol li:before {");
            htmlBuilder.append("    content: counter(list-item) \". \";");
            htmlBuilder.append("    counter-increment: list-item;");
            htmlBuilder.append("}");
            htmlBuilder.append(".underline { text-decoration: underline; }");
            htmlBuilder.append(".strike { text-decoration: line-through; }");
            htmlBuilder.append(".superscript { vertical-align: super; font-size: smaller; }");
            htmlBuilder.append(".subscript { vertical-align: sub; font-size: smaller; }");
            htmlBuilder.append(".footnote-number {");
            htmlBuilder.append("    vertical-align: super;");
            htmlBuilder.append("    font-size: 8pt;");
            htmlBuilder.append("    color: #666;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    line-height: 1;");
            htmlBuilder.append("}");
            htmlBuilder.append(".footnote-reference {");
            htmlBuilder.append("    vertical-align: super;");
            htmlBuilder.append("    font-size: 10pt;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    line-height: 1;");
            htmlBuilder.append("}");
            htmlBuilder.append(".page-break { page-break-before: always; }");
            htmlBuilder.append(".header {");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("    margin-bottom: 20mm;");
            htmlBuilder.append("    height: auto;");
            htmlBuilder.append("    min-height: 25mm;");
            htmlBuilder.append("    border: none;");
            htmlBuilder.append("    background: transparent;");
            htmlBuilder.append("    display: flex;");
            htmlBuilder.append("    justify-content: space-between;");
            htmlBuilder.append("    align-items: flex-start;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-left {");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("    text-align: left;");
            htmlBuilder.append("    font-size: 13pt;");
            htmlBuilder.append("    width: 48%;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    border: none;");
            htmlBuilder.append("    background: transparent;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-right {");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("    text-align: right;");
            htmlBuilder.append("    font-size: 13pt;");
            htmlBuilder.append("    width: 48%;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    border: none;");
            htmlBuilder.append("    background: transparent;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-right small {");
            htmlBuilder.append("    font-size: 9pt;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    margin-top: 2mm;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    border: none;");
            htmlBuilder.append("    background: transparent;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header * {");
            htmlBuilder.append("    border: none !important;");
            htmlBuilder.append("    background: transparent !important;");
            htmlBuilder.append("    box-shadow: none !important;");
            htmlBuilder.append("    outline: none !important;");
            htmlBuilder.append("}");
            htmlBuilder.append("div, p, span {");
            htmlBuilder.append("    border: none !important;");
            htmlBuilder.append("    outline: none !important;");
            htmlBuilder.append("}");
            htmlBuilder.append("* {");
            htmlBuilder.append("    border: none !important;");
            htmlBuilder.append("    outline: none !important;");
            htmlBuilder.append("    box-shadow: none !important;");
            htmlBuilder.append("}");
            htmlBuilder.append("div, p, span, h1, h2, h3, h4, h5, h6 {");
            htmlBuilder.append("    border: none !important;");
            htmlBuilder.append("    outline: none !important;");
            htmlBuilder.append("    box-shadow: none !important;");
            htmlBuilder.append("    background: transparent !important;");
            htmlBuilder.append("}");
            htmlBuilder.append(".title {");
            htmlBuilder.append("    text-align: center;");
            htmlBuilder.append("    font-size: 14pt;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    margin: 20mm auto;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("    text-transform: uppercase;");
            htmlBuilder.append("    letter-spacing: 1pt;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    width: 100%;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    page-break-after: avoid;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    max-width: 150mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".main-title {");
            htmlBuilder.append("    text-align: center;");
            htmlBuilder.append("    font-size: 14pt;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    margin: 20mm auto;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("    text-transform: uppercase;");
            htmlBuilder.append("    letter-spacing: 1pt;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    width: 100%;");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    page-break-after: avoid;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    max-width: 150mm;");
            htmlBuilder.append("    word-wrap: break-word;");
            htmlBuilder.append("    hyphens: auto;");
            htmlBuilder.append("}");
            htmlBuilder.append(".form-field {");
            htmlBuilder.append("    border-bottom: 2px dotted #000;");
            htmlBuilder.append("    display: inline-block;");
            htmlBuilder.append("    min-width: 88mm;");
            htmlBuilder.append("    height: 6mm;");
            htmlBuilder.append("    margin: 0 1.5mm;");
            htmlBuilder.append("    vertical-align: bottom;");
            htmlBuilder.append("}");
            htmlBuilder.append(".field-with-dots {");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("    display: inline-block;");
            htmlBuilder.append("    margin-right: 0;");
            htmlBuilder.append("}");
            htmlBuilder.append(".field-with-dots:after {");
            htmlBuilder.append("    content: \"---\";");
            htmlBuilder.append("    position: absolute;");
            htmlBuilder.append("    left: 100%;");
            htmlBuilder.append("    top: 0;");
            htmlBuilder.append("    font-size: 12pt;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("    font-weight: normal;");
            htmlBuilder.append("    white-space: nowrap;");
            htmlBuilder.append("    width: calc(100vw - 100% - 40mm);");
            htmlBuilder.append("    overflow: hidden;");
            htmlBuilder.append("    letter-spacing: 1px;");
            htmlBuilder.append("}");
            htmlBuilder.append(".bullet-point {");
            htmlBuilder.append("    margin: 3mm 0;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("    line-height: 1.4;");
            htmlBuilder.append("    padding-left: 15mm;");
            htmlBuilder.append("    position: relative;");
            htmlBuilder.append("}");
            htmlBuilder.append(".bullet-point:before {");
            htmlBuilder.append("    content: \"- \";");
            htmlBuilder.append("    position: absolute;");
            htmlBuilder.append("    left: 0;");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    color: #000;");
            htmlBuilder.append("}");
            htmlBuilder.append(".special-field {");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    margin: 3mm 0;");
            htmlBuilder.append("    text-indent: 0;");
            htmlBuilder.append("}");
            htmlBuilder.append(".signature-line {");
            htmlBuilder.append("    border-bottom: 1px solid #000;");
            htmlBuilder.append("    display: inline-block;");
            htmlBuilder.append("    min-width: 70mm;");
            htmlBuilder.append("    height: 7mm;");
            htmlBuilder.append("    margin: 0 1.5mm;");
            htmlBuilder.append("    vertical-align: bottom;");
            htmlBuilder.append("}");
            htmlBuilder.append(".signature-section {");
            htmlBuilder.append("    text-align: center;");
            htmlBuilder.append("    margin: 20mm 0;");
            htmlBuilder.append("}");
            htmlBuilder.append(".signature-title {");
            htmlBuilder.append("    font-weight: bold;");
            htmlBuilder.append("    margin: 10mm 0 5mm 0;");
            htmlBuilder.append("}");
            htmlBuilder.append("@media print {");
            htmlBuilder.append("    body { margin: 0; padding: 0; background: white; }");
            htmlBuilder.append("    .page-container { box-shadow: none; margin: 0; padding: 20mm; }");
            htmlBuilder.append("    .page-break { page-break-before: always; }");
            htmlBuilder.append("    table { page-break-inside: avoid; border: 1px solid #000; }");
            htmlBuilder.append("    td, th { border: 1px solid #000; }");
            htmlBuilder.append("    h1, h2, h3, h4, h5, h6 { page-break-after: avoid; }");
            htmlBuilder.append("    .header { page-break-inside: avoid; }");
            htmlBuilder.append("    .title { page-break-inside: avoid; }");
            htmlBuilder.append("    .main-title { page-break-inside: avoid; }");
            htmlBuilder.append("}");
            htmlBuilder.append("@media screen and (max-width: 768px) {");
            htmlBuilder.append("    body { padding: 10px; }");
            htmlBuilder.append("    .page-container {");
            htmlBuilder.append("        width: 100%;");
            htmlBuilder.append("        height: auto;");
            htmlBuilder.append("        min-height: 297mm;");
            htmlBuilder.append("        padding: 15mm;");
            htmlBuilder.append("    }");
            htmlBuilder.append("    .main-title {");
            htmlBuilder.append("        font-size: 12pt;");
            htmlBuilder.append("        max-width: 95%;");
            htmlBuilder.append("        margin: 15mm auto;");
            htmlBuilder.append("    }");
            htmlBuilder.append("    .header-text {");
            htmlBuilder.append("        font-size: 11pt;");
            htmlBuilder.append("    }");
            htmlBuilder.append("    .header-left, .header-right {");
            htmlBuilder.append("        font-size: 11pt;");
            htmlBuilder.append("    }");
            htmlBuilder.append("}");
            htmlBuilder.append(".word-like-spacing {");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    line-height: 1.15;");
            htmlBuilder.append("    text-align: justify;");
            htmlBuilder.append("    margin-bottom: 2mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-line-break {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    margin-top: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-line-break:after {");
            htmlBuilder.append("    content: \"\";");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    height: 0;");
            htmlBuilder.append("    clear: both;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header p, .header div {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    margin-bottom: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-left p, .header-left div {");
            htmlBuilder.append("    text-align: left;");
            htmlBuilder.append("    margin-bottom: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-right p, .header-right div {");
            htmlBuilder.append("    text-align: right;");
            htmlBuilder.append("    margin-bottom: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".force-line-break {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    clear: both;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    margin-top: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append(".header-text-separator {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    line-height: 1.3;");
            htmlBuilder.append("    page-break-inside: avoid;");
            htmlBuilder.append("    height: 0;");
            htmlBuilder.append("    clear: both;");
            htmlBuilder.append("    margin-top: 1mm;");
            htmlBuilder.append("}");
            htmlBuilder.append("br.header-text-separator {");
            htmlBuilder.append("    display: block;");
            htmlBuilder.append("    content: \"\";");
            htmlBuilder.append("    margin: 0;");
            htmlBuilder.append("    padding: 0;");
            htmlBuilder.append("    height: 0;");
            htmlBuilder.append("    line-height: 0;");
            htmlBuilder.append("    clear: both;");
            htmlBuilder.append("}");
            htmlBuilder.append("</style>");
            htmlBuilder.append("</head><body>");
            htmlBuilder.append("<div class=\"page-container\">");
            htmlBuilder.append("<div class=\"page-content\">");

            // Process document body
            processDocumentBody(document, htmlBuilder);

            htmlBuilder.append("</div>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("</body></html>");
            return htmlBuilder.toString();
        }
    }

    private void processDocumentBody(XWPFDocument document, StringBuilder htmlBuilder) {
        // Process document in order (paragraphs, tables, etc.)
        List<IBodyElement> bodyElements = document.getBodyElements();

        boolean inList = false;
        boolean inTable = false;

        for (IBodyElement element : bodyElements) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                boolean isList = isListParagraph(paragraph);

                // Handle list start/end
                if (isList && !inList) {
                    htmlBuilder.append("<ul>");
                    inList = true;
                } else if (!isList && inList) {
                    htmlBuilder.append("</ul>");
                    inList = false;
                }

                htmlBuilder.append(processParagraph(paragraph));

            } else if (element instanceof XWPFTable) {
                // Close list if we're in one
                if (inList) {
                    htmlBuilder.append("</ul>");
                    inList = false;
                }

                htmlBuilder.append(processTable((XWPFTable) element));
                inTable = true;
            }
        }

        // Close any remaining list
        if (inList) {
            htmlBuilder.append("</ul>");
        }

        // Process images separately
        processImages(document, htmlBuilder);
    }

    private String processParagraph(XWPFParagraph paragraph) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Get paragraph properties
        ParagraphAlignment alignment = paragraph.getAlignment();
        int leftIndent = paragraph.getIndentationLeft();
        int rightIndent = paragraph.getIndentationRight();
        int firstLineIndent = paragraph.getIndentationFirstLine();
        int spacingBefore = paragraph.getSpacingBefore();
        int spacingAfter = paragraph.getSpacingAfter();

        // Check if it's a heading
        String headingTag = getHeadingTag(paragraph.getStyle());

        // Check if it's a bullet point or numbered list
        boolean isList = isListParagraph(paragraph);

        // Get paragraph text first
        String paragraphText = paragraph.getText();

        // Determine paragraph tag and classes
        String tag = isList ? "li" : "p";
        StringBuilder classes = new StringBuilder();

        if (headingTag != null) {
            tag = headingTag;
            classes.append("title");
        } else if (paragraphText != null && isMainTitle(paragraphText)) {
            tag = "h3";
            classes.append("main-title");
        } else if (!isList) {
            // Add alignment classes for non-list paragraphs
            if (alignment != null) {
                switch (alignment) {
                    case CENTER:
                        classes.append("center");
                        break;
                    case RIGHT:
                        classes.append("right");
                        break;
                    case LEFT:
                        classes.append("left");
                        break;
                    case BOTH:
                    default:
                        classes.append("justify");
                        break;
                }
            }

            // Add indent classes
            if (firstLineIndent == 0) {
                classes.append(" no-indent");
            }

            // Check if it's a bullet point (starts with dash or bullet)
            if (paragraphText != null
                    && (paragraphText.trim().startsWith("-") || paragraphText.trim().startsWith("•"))) {
                classes.append(" bullet-point");
            }

            // Check if it's a field that needs dots (ends with colon or contains specific
            // patterns)
            if (paragraphText != null && (paragraphText.trim().endsWith(":") ||
                    paragraphText.trim().endsWith("1:") ||
                    paragraphText.trim().endsWith("2:") ||
                    paragraphText.trim().endsWith("3:") ||
                    paragraphText.trim().endsWith("4:") ||
                    paragraphText.trim().endsWith("5:") ||
                    paragraphText.contains("KHOA/VIỆN") ||
                    paragraphText.contains("Kính gửi") ||
                    paragraphText.contains("ghi tên dưới đây") ||
                    paragraphText.contains("Là tác giả") ||
                    paragraphText.contains("Chủ đầu tư") ||
                    paragraphText.contains("Lĩnh vựa áp dụng") ||
                    paragraphText.contains("Những thông tin cần được bảo mật") ||
                    paragraphText.contains("Các điều kiện cần thiết") ||
                    paragraphText.contains("Đánh giá lợi ích"))) {
                classes.append(" field-with-dots");
            }

            // Check if it's a special field (signature, date, etc.)
            if (paragraphText != null && (paragraphText.contains("Ký và ghi rõ họ tên") ||
                    paragraphText.contains("ngày") && paragraphText.contains("tháng") && paragraphText.contains("năm")
                    ||
                    paragraphText.contains("Tác giả sáng kiến"))) {
                classes.append(" special-field");
            }

            // Check if it needs line break (specific phrases that should be on new lines)
            if (paragraphText != null && (paragraphText.contains("Độc lập - Tự do - Hạnh phúc") ||
                    paragraphText.contains("KHOA/VIỆN") ||
                    paragraphText.contains("THÀNH PHỐ HỒ CHÍ MINH") ||
                    paragraphText.contains("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM"))) {
                classes.append(" header-line-break");
            }

            // Force line breaks for specific text patterns
            if (paragraphText != null && (paragraphText.contains("HỒ CHÍ MINH") ||
                    paragraphText.contains("VIỆT NAM"))) {
                classes.append(" force-line-break");
            }
        }

        // Add spacing before paragraph
        if (spacingBefore > 0) {
            htmlBuilder.append("<div style=\"margin-top: ").append(spacingBefore / 20.0).append("pt;\"></div>");
        }

        // Build opening tag with classes
        htmlBuilder.append("<").append(tag);
        if (classes.length() > 0) {
            htmlBuilder.append(" class=\"").append(classes.toString().trim()).append("\"");
        }

        // Add inline styles for specific formatting
        StringBuilder inlineStyles = new StringBuilder();
        if (leftIndent > 0) {
            inlineStyles.append("margin-left: ").append(leftIndent / 20.0).append("pt; ");
        }
        if (rightIndent > 0) {
            inlineStyles.append("margin-right: ").append(rightIndent / 20.0).append("pt; ");
        }
        if (spacingAfter > 0) {
            inlineStyles.append("margin-bottom: ").append(spacingAfter / 20.0).append("pt; ");
        }

        if (inlineStyles.length() > 0) {
            htmlBuilder.append(" style=\"").append(inlineStyles.toString().trim()).append("\"");
        }

        htmlBuilder.append(">");

        // Process runs (text with formatting)
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && !text.trim().isEmpty()) {
                // Apply comprehensive formatting
                applyTextFormatting(htmlBuilder, run, text);
            }
        }

        htmlBuilder.append("</").append(tag).append(">");

        // Add spacing after paragraph
        if (spacingAfter > 0 && headingTag == null && !isList) {
            htmlBuilder.append("<div style=\"margin-bottom: ").append(spacingAfter / 20.0).append("pt;\"></div>");
        }

        return htmlBuilder.toString();
    }

    private boolean isListParagraph(XWPFParagraph paragraph) {
        // Check if paragraph is part of a list
        BigInteger numId = paragraph.getNumID();
        return numId != null;
    }

    private boolean isMainTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String trimmedText = text.trim();

        // Check if it's a main title based on common patterns
        // 1. All uppercase text (likely a main title)
        if (trimmedText.equals(trimmedText.toUpperCase()) && trimmedText.length() > 5) {
            return true;
        }

        // 2. Contains specific Vietnamese title keywords
        String[] titleKeywords = {
                "ĐƠN ĐỀ NGHỊ", "CÔNG NHẬN", "SÁNG KIẾN",
                "BÁO CÁO", "MÔ TẢ", "SÁNG KIẾN",
                "ĐỀ ÁN", "DỰ ÁN", "KẾ HOẠCH",
                "CHIẾN LƯỢC", "PHƯƠNG ÁN", "GIẢI PHÁP",
                "NGHIÊN CỨU", "KHẢO SÁT", "ĐIỀU TRA",
                "THỐNG KÊ", "PHÂN TÍCH", "ĐÁNH GIÁ"
        };

        for (String keyword : titleKeywords) {
            if (trimmedText.contains(keyword)) {
                return true;
            }
        }

        // 3. Check if it's centered and has title-like formatting
        // This will be handled by the CSS class application

        return false;
    }

    private void applyTextFormatting(StringBuilder htmlBuilder, XWPFRun run, String text) {
        // Start formatting tags with inline styles
        StringBuilder spanStyles = new StringBuilder();

        // Font size
        int fontSize = run.getFontSize();
        if (fontSize > 0) {
            spanStyles.append("font-size: ").append(fontSize / 2).append("pt; ");
        }

        // Font family
        String fontFamily = run.getFontFamily();
        if (fontFamily != null && !fontFamily.isEmpty()) {
            spanStyles.append("font-family: '").append(fontFamily).append("', serif; ");
        }

        // Font color
        String color = run.getColor();
        if (color != null && !color.isEmpty()) {
            spanStyles.append("color: #").append(color).append("; ");
        }

        // Background color - not available in XWPFRun, skip for now

        // Start span with styles if needed
        if (spanStyles.length() > 0) {
            htmlBuilder.append("<span style=\"").append(spanStyles.toString().trim()).append("\">");
        }

        // Start formatting tags
        if (run.isBold()) {
            htmlBuilder.append("<strong>");
        }
        if (run.isItalic()) {
            htmlBuilder.append("<em>");
        }
        if (run.getUnderline() != UnderlinePatterns.NONE) {
            htmlBuilder.append("<span class=\"underline\">");
        }
        if (run.isStrike()) {
            htmlBuilder.append("<span class=\"strike\">");
        }

        // Add text with proper handling
        if (text != null) {
            // Replace newlines with spaces to avoid breaking layout
            text = text.replace("\n", " ").replace("\r", "");

            // Check for superscript numbers (like ², ³, ⁴, ⁵)
            if (text.matches(".*[²³⁴⁵].*")) {
                text = text.replace("²", "<span class=\"footnote-number\">2</span>")
                        .replace("³", "<span class=\"footnote-number\">3</span>")
                        .replace("⁴", "<span class=\"footnote-number\">4</span>")
                        .replace("⁵", "<span class=\"footnote-number\">5</span>");
            }

            // Process header text line by line - EXACTLY as in the template
            if (text.contains("TRƯỜNG ĐẠI HỌC CÔNG NGHIỆP") && !htmlBuilder.toString().contains("header-left")) {
                htmlBuilder.append("<div class=\"header-left\">");
                htmlBuilder.append("<span class=\"header-text\">TRƯỜNG ĐẠI HỌC CÔNG NGHIỆP</span>");
                htmlBuilder.append("<br><span class=\"header-text\">THÀNH PHỐ HỒ CHÍ MINH</span>");
                htmlBuilder.append("<br><span class=\"header-text underline\">KHOA/VIỆN : ---</span>");
                htmlBuilder.append("</div>");
            } else if (text.contains("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    && !htmlBuilder.toString().contains("header-right")) {
                htmlBuilder.append("<div class=\"header-right\">");
                htmlBuilder.append("<span class=\"header-text\">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</span>");
                htmlBuilder.append("<br><span class=\"header-text underline\">Độc lập - Tự do - Hạnh phúc</span>");
                htmlBuilder.append("</div>");
            } else if (text.contains("Tác giả sáng kiến")) {
                htmlBuilder.append("<div class=\"signature-section\">");
                htmlBuilder.append("<div class=\"signature-title\">" + text + "</div>");
                htmlBuilder.append("<div>(Ký và ghi rõ họ tên)</div>");
                htmlBuilder.append("<div><span class=\"signature-line\"></span></div>");
                htmlBuilder.append("</div>");
            } else if (text.contains("Ký và ghi rõ họ tên")) {
                htmlBuilder.append(text + " <span class=\"signature-line\"></span>");
            } else if (text.contains("ngày") && text.contains("tháng") && text.contains("năm")) {
                htmlBuilder.append(text.replace("ngày ... tháng ... năm",
                        "ngày <span class=\"signature-line\"></span> tháng <span class=\"signature-line\"></span> năm"));
            } else {
                htmlBuilder.append(text);
            }
        }

        // Close formatting tags in reverse order
        if (run.isStrike()) {
            htmlBuilder.append("</span>");
        }
        if (run.getUnderline() != UnderlinePatterns.NONE) {
            htmlBuilder.append("</span>");
        }
        if (run.isItalic()) {
            htmlBuilder.append("</em>");
        }
        if (run.isBold()) {
            htmlBuilder.append("</strong>");
        }

        // Close span with styles if needed
        if (spanStyles.length() > 0) {
            htmlBuilder.append("</span>");
        }
    }

    private String getHeadingTag(String style) {
        if (style == null)
            return null;

        style = style.toLowerCase();
        if (style.contains("heading 1") || style.contains("title"))
            return "h1";
        if (style.contains("heading 2"))
            return "h2";
        if (style.contains("heading 3"))
            return "h3";
        if (style.contains("heading 4"))
            return "h4";
        if (style.contains("heading 5"))
            return "h5";
        if (style.contains("heading 6"))
            return "h6";

        return null;
    }

    private String processTable(XWPFTable table) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Get table properties
        htmlBuilder.append("<table");

        // Add table styling
        StringBuilder tableStyles = new StringBuilder();
        if (table.getWidth() > 0) {
            tableStyles.append("width: ").append(table.getWidth() / 20.0).append("pt; ");
        }

        if (tableStyles.length() > 0) {
            htmlBuilder.append(" style=\"").append(tableStyles.toString().trim()).append("\"");
        }

        htmlBuilder.append(">");

        // Process table rows
        for (XWPFTableRow row : table.getRows()) {
            htmlBuilder.append("<tr");

            // Add row styling
            StringBuilder rowStyles = new StringBuilder();
            if (row.getHeight() > 0) {
                rowStyles.append("height: ").append(row.getHeight() / 20.0).append("pt; ");
            }

            if (rowStyles.length() > 0) {
                htmlBuilder.append(" style=\"").append(rowStyles.toString().trim()).append("\"");
            }

            htmlBuilder.append(">");

            // Process table cells
            for (XWPFTableCell cell : row.getTableCells()) {
                // Check if it's a header cell
                boolean isHeader = isHeaderCell(cell);
                String cellTag = isHeader ? "th" : "td";

                htmlBuilder.append("<").append(cellTag);

                // Add cell styling
                StringBuilder cellStyles = new StringBuilder();

                // Cell width
                if (cell.getWidth() > 0) {
                    cellStyles.append("width: ").append(cell.getWidth() / 20.0).append("pt; ");
                }

                // Cell background color
                CTTc ctTc = cell.getCTTc();
                if (ctTc != null && ctTc.getTcPr() != null && ctTc.getTcPr().getShd() != null) {
                    Object fill = ctTc.getTcPr().getShd().getFill();
                    if (fill != null && !fill.toString().isEmpty()) {
                        cellStyles.append("background-color: #").append(fill.toString()).append("; ");
                    }
                }

                // Cell borders - Keep borders for tables
                if (ctTc != null && ctTc.getTcPr() != null) {
                    // Keep default borders for table cells
                    // cellStyles.append("border: 1px solid #000; ");
                }

                if (cellStyles.length() > 0) {
                    htmlBuilder.append(" style=\"").append(cellStyles.toString().trim()).append("\"");
                }

                htmlBuilder.append(">");

                // Process cell content (paragraphs)
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    // Apply text formatting
                    for (XWPFRun run : paragraph.getRuns()) {
                        String text = run.getText(0);
                        if (text != null) {
                            // Apply comprehensive formatting
                            applyTextFormatting(htmlBuilder, run, text);
                        }
                    }
                }

                htmlBuilder.append("</").append(cellTag).append(">");
            }

            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</table>");
        return htmlBuilder.toString();
    }

    private boolean isHeaderCell(XWPFTableCell cell) {
        // Check if cell is in first row or has header styling
        XWPFTableRow row = cell.getTableRow();
        if (row != null) {
            int rowIndex = row.getTable().getRows().indexOf(row);
            return rowIndex == 0; // First row is considered header
        }
        return false;
    }

    private void processImages(XWPFDocument document, StringBuilder htmlBuilder) {
        // Process images in the document
        for (XWPFPictureData pictureData : document.getAllPictures()) {
            String imageType = pictureData.suggestFileExtension();
            if (imageType != null && (imageType.equals("png") || imageType.equals("jpg") || imageType.equals("jpeg")
                    || imageType.equals("gif"))) {
                String base64Image = java.util.Base64.getEncoder().encodeToString(pictureData.getData());
                htmlBuilder.append("<img src=\"data:image/").append(imageType).append(";base64,").append(base64Image)
                        .append("\" alt=\"Image\" />");
            }
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
