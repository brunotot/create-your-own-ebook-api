package brunotot.createyourownebookapi.controller;

import brunotot.createyourownebookapi.domain.constants.AppProps;
import brunotot.createyourownebookapi.itext.service.PDFService;
import brunotot.createyourownebookapi.openai.service.ChatBotService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pdf")
public class PDFController {
    private final PDFService pdfService;
    private final ChatBotService chatBotService;

    public PDFController(final PDFService pdfService, final ChatBotService chatBotService) {
        this.pdfService = pdfService;
        this.chatBotService = chatBotService;
    }

    @GetMapping("/ebook")
    public ResponseEntity<InputStreamResource> downloadPDF(
            final @RequestParam String title,
            final @RequestParam(required = false) String additionalInfo,
            final @RequestParam(required = false, defaultValue = AppProps.DEFAULT_CHAT_LANG) String language
    ) throws Exception {
        var pdfStructure = this.chatBotService.getPDFStructure(title, additionalInfo, language);
        var pdfSection = this.chatBotService.getPDFSection(pdfStructure, language);
        var isr = this.pdfService.createPDF(pdfSection);
        return this.buildPDFResponseEntity(isr);
    }

    private ResponseEntity<InputStreamResource> buildPDFResponseEntity(final InputStreamResource isr) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=generated-ebook.pdf");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(isr);
    }
}
