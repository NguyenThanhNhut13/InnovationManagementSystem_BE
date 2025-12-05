package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfGeneratorService {

    // 1. Chuyển HTML (đã chuẩn hóa) sang PDF sử dụng iText 7
    public byte[] convertHtmlToPdf(String htmlContent) {
        return convertHtmlToPdfUsingIText(htmlContent);
    }

    // 2. Chuyển HTML sang PDF sử dụng Microsoft Word COM automation
    public byte[] convertHtmlToPdfUsingWord(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IdInvalidException("Nội dung HTML để chuyển sang PDF đang trống.");
        }

        Path tempDir = null;
        Path htmlFile = null;
        Path pdfFile = null;

        try {
            tempDir = Files.createTempDirectory("word_pdf_");
            String uniqueId = UUID.randomUUID().toString();
            htmlFile = tempDir.resolve(uniqueId + ".html");
            pdfFile = tempDir.resolve(uniqueId + ".pdf");

            String sanitizedHtml = sanitizeHtmlToXhtml(htmlContent);
            Files.write(htmlFile, sanitizedHtml.getBytes(StandardCharsets.UTF_8));

            convertHtmlToPdfUsingWordCom(htmlFile.toString(), pdfFile.toString());

            if (!Files.exists(pdfFile)) {
                throw new IdInvalidException("Không thể tạo file PDF từ Word.");
            }

            return Files.readAllBytes(pdfFile);

        } catch (IdInvalidException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException("Không thể chuyển HTML sang PDF bằng Word: " + errorMessage);
        } finally {
            deleteFileIfExists(htmlFile);
            deleteFileIfExists(pdfFile);
            deleteDirectoryIfExists(tempDir);
        }
    }

    // 3. Chuyển HTML sang PDF sử dụng iText 7 (hỗ trợ tốt Unicode và tiếng Việt)
    public byte[] convertHtmlToPdfUsingIText(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IdInvalidException("Nội dung HTML để chuyển sang PDF đang trống.");
        }

        String sanitizedHtml;
        try {
            sanitizedHtml = sanitizeHtmlToXhtml(htmlContent);
            // Thêm CSS font Times New Roman vào HTML
            sanitizedHtml = injectTimesNewRomanFont(sanitizedHtml);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException("Không thể chuẩn hóa HTML: " + errorMessage);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);

            ConverterProperties converterProperties = new ConverterProperties();
            DefaultFontProvider fontProvider = new DefaultFontProvider(false, false, false);

            // Thêm font hệ thống để có Times New Roman
            try {
                fontProvider.addSystemFonts();
            } catch (Exception e) {
                // Ignore nếu không load được system fonts
            }

            converterProperties.setFontProvider(fontProvider);

            HtmlConverter.convertToPdf(sanitizedHtml, pdfDocument, converterProperties);

            return outputStream.toByteArray();
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException("Không thể chuyển HTML sang PDF: " + errorMessage);
        }
    }

    // Helper method để inject font Times New Roman vào HTML (giống văn bản hành
    // chính)
    private String injectTimesNewRomanFont(String html) {
        String fontCss = "<style>" +
                "* { font-family: 'Times New Roman', Times, serif !important; font-size: 13pt; }" +
                "body { font-family: 'Times New Roman', Times, serif !important; font-size: 13pt; }" +
                "p, div, span { font-family: 'Times New Roman', Times, serif !important; font-size: 13pt; }" +
                "table, th, td { font-family: 'Times New Roman', Times, serif !important; font-size: 13pt; }" +
                "h1, h2, h3, h4, h5, h6 { font-family: 'Times New Roman', Times, serif !important; }" +
                "</style>";

        // Chèn CSS vào trước thẻ </head> hoặc đầu <body>
        if (html.contains("</head>")) {
            html = html.replace("</head>", fontCss + "</head>");
        } else if (html.contains("<body")) {
            int bodyIndex = html.indexOf("<body");
            int bodyEndIndex = html.indexOf(">", bodyIndex);
            if (bodyEndIndex != -1) {
                html = html.substring(0, bodyEndIndex + 1) + fontCss + html.substring(bodyEndIndex + 1);
            }
        } else {
            html = fontCss + html;
        }

        return html;
    }

    // 4. Chuyển HTML sang PDF sử dụng openhtmltopdf (phương pháp cũ - giữ lại để
    // backup)
    public byte[] convertHtmlToPdfUsingOpenHtmlToPdf(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IdInvalidException("Nội dung HTML để chuyển sang PDF đang trống.");
        }

        String sanitizedHtml;
        try {
            sanitizedHtml = sanitizeHtmlToXhtml(htmlContent);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException("Không thể chuẩn hóa HTML: " + errorMessage);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(210, 297,
                    com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PageSizeUnits.MM);
            builder.withHtmlContent(sanitizedHtml, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            throw new IdInvalidException("Không thể chuyển HTML sang PDF: " + errorMessage);
        }
    }

    // 5. Gọi Word COM automation để chuyển HTML sang PDF
    private void convertHtmlToPdfUsingWordCom(String htmlFilePath, String pdfFilePath) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            throw new IdInvalidException("Chức năng chuyển đổi bằng Word chỉ hỗ trợ trên Windows.");
        }

        String htmlPathEscaped = htmlFilePath.replace("'", "''").replace("\\", "\\\\");
        String pdfPathEscaped = pdfFilePath.replace("'", "''").replace("\\", "\\\\");

        String powershellScript = String.format(
                "$ErrorActionPreference = 'Stop'; " +
                        "try { " +
                        "  $word = New-Object -ComObject Word.Application; " +
                        "  $word.Visible = $false; " +
                        "  $word.DisplayAlerts = 0; " +
                        "  $htmlPath = '%s'; " +
                        "  $pdfPath = '%s'; " +
                        "  if (-not (Test-Path $htmlPath)) { " +
                        "    throw \"File not found: $htmlPath\"; " +
                        "  } " +
                        "  $doc = $word.Documents.Open($htmlPath); " +
                        "  if ($doc -eq $null) { " +
                        "    throw \"Failed to open document: $htmlPath\"; " +
                        "  } " +
                        "  $doc.ExportAsFixedFormat($pdfPath, 17); " +
                        "  $doc.Close($false); " +
                        "  $word.Quit($false); " +
                        "  [System.Runtime.Interopservices.Marshal]::ReleaseComObject($doc) | Out-Null; " +
                        "  [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null; " +
                        "  [System.GC]::Collect(); " +
                        "  [System.GC]::WaitForPendingFinalizers(); " +
                        "} catch { " +
                        "  if ($word) { try { $word.Quit($false) } catch {} } " +
                        "  Write-Error $_.Exception.Message; " +
                        "  exit 1; " +
                        "}",
                htmlPathEscaped,
                pdfPathEscaped);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-Command", powershellScript);

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IdInvalidException("Quá trình chuyển đổi bằng Word bị timeout (quá 60 giây).");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IdInvalidException(
                    "Lỗi khi chuyển đổi bằng Word. Exit code: " + exitCode + ". Error: " + errorOutput);
        }
    }

    // 5. Xóa file nếu tồn tại
    private void deleteFileIfExists(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    // 6. Xóa thư mục nếu tồn tại
    private void deleteDirectoryIfExists(Path path) {
        if (path != null) {
            try {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                // Ignore
                            }
                        });
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    // 7. Chuẩn hóa HTML về XHTML hợp lệ để tránh lỗi font và tag
    private String sanitizeHtmlToXhtml(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        // Loại bỏ khoảng trắng thừa và các element rỗng ở đầu body
        Element body = document.body();
        if (body != null) {
            // Loại bỏ text node rỗng ở đầu body
            List<org.jsoup.nodes.Node> nodesToRemove = new ArrayList<>();
            for (org.jsoup.nodes.Node node : body.childNodes()) {
                if (node instanceof TextNode) {
                    String text = ((TextNode) node).text().trim();
                    if (text.isEmpty()) {
                        nodesToRemove.add(node);
                    }
                }
            }
            for (org.jsoup.nodes.Node node : nodesToRemove) {
                node.remove();
            }

            // Loại bỏ các element rỗng ở đầu body
            Elements children = body.children();
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                String text = child.text().trim();
                String html = child.html().trim();

                // Loại bỏ element rỗng hoặc chỉ chứa whitespace
                if (text.isEmpty() && (html.isEmpty() || html.matches("^\\s*$"))) {
                    child.remove();
                    i--;
                } else if (!text.isEmpty() || !child.children().isEmpty()) {
                    // Dừng lại khi gặp element có nội dung
                    break;
                }
            }

            // Loại bỏ page-break-before ở element đầu tiên
            Element firstChild = body.children().first();
            if (firstChild != null) {
                String style = firstChild.attr("style");
                if (style != null && !style.isEmpty()) {
                    style = style.replaceAll("page-break-before\\s*:\\s*[^;]+;?", "");
                    style = style.replaceAll("page-break-before\\s*:\\s*[^;]+", "");
                    firstChild.attr("style", style.trim());
                }
            }

            // Xử lý các chuỗi ký tự "¾" lặp lại thành gạch chân
            processThreeQuartersUnderline(body);
        }

        // Đảm bảo có thẻ head
        Element head = document.head();
        if (head == null) {
            head = document.prependElement("head");
        }

        // Thêm meta charset UTF-8 nếu chưa có
        Element metaCharset = head.selectFirst("meta[charset]");
        if (metaCharset == null) {
            metaCharset = head.selectFirst("meta[http-equiv=Content-Type]");
            if (metaCharset == null) {
                head.prependElement("meta").attr("charset", "UTF-8");
            } else {
                metaCharset.attr("content", "text/html; charset=UTF-8");
            }
        } else {
            metaCharset.attr("charset", "UTF-8");
        }

        // Cấu hình output settings
        document.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8)
                .prettyPrint(false);

        String html = document.outerHtml();

        // Loại bỏ khoảng trắng thừa ở đầu và cuối HTML
        html = html.trim();

        // Đảm bảo không có page break và khoảng trắng thừa ở đầu body
        if (html.contains("<body")) {
            int bodyStart = html.indexOf("<body");
            int bodyContentStart = html.indexOf(">", bodyStart) + 1;
            String beforeBody = html.substring(0, bodyContentStart);
            String bodyContent = html.substring(bodyContentStart);

            // Loại bỏ whitespace, br, và div rỗng ở đầu body
            bodyContent = bodyContent.replaceFirst("^\\s+", "");
            bodyContent = bodyContent.replaceFirst("^<br\\s*/?>\\s*", "");
            bodyContent = bodyContent.replaceFirst("^<div[^>]*>\\s*</div>\\s*", "");
            bodyContent = bodyContent.replaceFirst("^<p[^>]*>\\s*</p>\\s*", "");

            // Loại bỏ page-break-before trong style attribute
            bodyContent = bodyContent.replaceFirst(
                    "(<[^>]+\\s+style\\s*=\\s*[\"'][^\"']*?)page-break-before\\s*:\\s*[^;\"']+[;\"']", "$1");

            html = beforeBody + bodyContent;
        }

        return html;
    }

    // 8. Xử lý các chuỗi ký tự "¾" lặp lại thành gạch chân
    private void processThreeQuartersUnderline(Element body) {
        if (body == null) {
            return;
        }

        // Thu thập tất cả text nodes
        List<org.jsoup.nodes.Node> textNodes = new ArrayList<>();
        collectTextNodes(body, textNodes);

        // Xử lý từng text node
        for (org.jsoup.nodes.Node node : textNodes) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                String text = textNode.text();

                // Kiểm tra nếu text chỉ chứa ký tự "¾" lặp lại (có thể có khoảng trắng)
                if (text != null && text.trim().matches("^[¾\\s]+$") && text.trim().length() > 0) {
                    org.jsoup.nodes.Node parent = textNode.parent();
                    if (parent instanceof Element) {
                        Element parentElement = (Element) parent;

                        // Nếu parent chỉ chứa text node này và không có children khác
                        boolean onlyThisText = parentElement.childNodes().size() == 1
                                && parentElement.childNodes().get(0) == textNode;

                        if (onlyThisText) {
                            // Thay thế toàn bộ element bằng span có border-bottom với độ dài bằng text
                            String currentStyle = parentElement.attr("style");
                            int threeQuartersCount = text.trim().replaceAll("\\s", "").length();
                            // Tính độ rộng dựa trên số ký tự "¾" (mỗi ký tự khoảng 1em)
                            String borderStyle = "border-bottom: 1px solid black; display: inline-block; width: "
                                    + (threeQuartersCount * 1) + "em; height: 0; margin: 2px 0;";

                            if (currentStyle == null || currentStyle.isEmpty()) {
                                parentElement.attr("style", borderStyle);
                            } else if (!currentStyle.contains("border-bottom")) {
                                parentElement.attr("style", currentStyle + "; " + borderStyle);
                            }

                            // Xóa text content
                            parentElement.text("");

                            // Đổi tag thành span để inline-block hoạt động đúng
                            if (parentElement.tagName().equals("div") || parentElement.tagName().equals("p")) {
                                Element newSpan = new Element("span");
                                newSpan.attr("style", parentElement.attr("style"));
                                newSpan.attr("class", parentElement.attr("class"));
                                parentElement.replaceWith(newSpan);
                            }
                        } else {
                            // Nếu có children khác, thay thế text node bằng span có border-bottom
                            int threeQuartersCount = text.trim().replaceAll("\\s", "").length();
                            Element underlineElement = new Element("span");
                            underlineElement.attr("style",
                                    "border-bottom: 1px solid black; display: inline-block; width: "
                                            + (threeQuartersCount * 0.5) + "em; height: 0; margin: 0;");
                            textNode.replaceWith(underlineElement);
                        }
                    }
                } else if (text != null && text.contains("¾")) {
                    // Nếu text chứa "¾" nhưng không chỉ có "¾", xử lý từng phần
                    String[] parts = text.split("(¾+)");
                    if (parts.length > 1) {
                        // Có chứa chuỗi "¾", cần xử lý phức tạp hơn
                        org.jsoup.nodes.Node parent = textNode.parent();
                        if (parent instanceof Element) {
                            Element parentElement = (Element) parent;
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(¾+)");
                            java.util.regex.Matcher matcher = pattern.matcher(text);

                            List<org.jsoup.nodes.Node> replacements = new ArrayList<>();
                            int lastEnd = 0;

                            while (matcher.find()) {
                                // Thêm text trước chuỗi "¾"
                                if (matcher.start() > lastEnd) {
                                    String beforeText = text.substring(lastEnd, matcher.start());
                                    if (!beforeText.isEmpty()) {
                                        replacements.add(new TextNode(beforeText));
                                    }
                                }

                                // Thêm element gạch chân cho chuỗi "¾"
                                Element underlineSpan = new Element("span");
                                underlineSpan.attr("style",
                                        "border-bottom: 1px solid black; display: inline-block; width: "
                                                + (matcher.group(1).length() * 0.5) + "em; height: 0; margin: 0;");
                                replacements.add(underlineSpan);

                                lastEnd = matcher.end();
                            }

                            // Thêm text còn lại
                            if (lastEnd < text.length()) {
                                String afterText = text.substring(lastEnd);
                                if (!afterText.isEmpty()) {
                                    replacements.add(new TextNode(afterText));
                                }
                            }

                            // Thay thế text node bằng các node mới
                            if (!replacements.isEmpty()) {
                                for (int i = 0; i < replacements.size(); i++) {
                                    if (i == 0) {
                                        textNode.replaceWith(replacements.get(i));
                                    } else {
                                        parentElement.appendChild(replacements.get(i));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Xử lý các element chỉ chứa text "¾"
        Elements allElements = body.getAllElements();
        for (Element element : allElements) {
            String text = element.ownText();
            // Nếu element chỉ chứa chuỗi "¾" và không có children khác
            if (text != null && !text.isEmpty() && element.children().isEmpty()) {
                String trimmedText = text.trim();
                if (trimmedText.matches("^[¾\\s]+$") && trimmedText.length() > 0) {
                    int threeQuartersCount = trimmedText.replaceAll("\\s", "").length();
                    element.text("");
                    String currentStyle = element.attr("style");
                    String borderStyle = "border-bottom: 1px solid black; display: inline-block; width: "
                            + (threeQuartersCount * 0.5) + "em; height: 0; margin: 2px 0;";

                    if (currentStyle == null || currentStyle.isEmpty()) {
                        element.attr("style", borderStyle);
                    } else if (!currentStyle.contains("border-bottom")) {
                        element.attr("style", currentStyle + "; " + borderStyle);
                    }

                    // Đổi tag thành span để inline-block hoạt động đúng
                    if (element.tagName().equals("div") || element.tagName().equals("p")) {
                        Element newSpan = new Element("span");
                        newSpan.attr("style", element.attr("style"));
                        newSpan.attr("class", element.attr("class"));
                        element.replaceWith(newSpan);
                    }
                }
            }
        }
    }

    // 9. Helper method để thu thập tất cả text nodes
    private void collectTextNodes(org.jsoup.nodes.Node node, List<org.jsoup.nodes.Node> textNodes) {
        if (node instanceof TextNode) {
            textNodes.add(node);
        }
        for (org.jsoup.nodes.Node child : node.childNodes()) {
            collectTextNodes(child, textNodes);
        }
    }
}
