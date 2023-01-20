package brunotot.createyourownebookapi.itext.service.impl;

import brunotot.createyourownebookapi.domain.PDFSection;
import brunotot.createyourownebookapi.itext.service.PDFService;
import brunotot.createyourownebookapi.manager.PDFManager;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

@Service
public class PDFServiceImpl implements PDFService {
    @Override
    public InputStreamResource createPDF(final PDFSection pdfSection) throws Exception {
        return createAndSavePDF(pdfSection);
    }

    private InputStreamResource createAndSavePDF(final PDFSection bookContent) throws Exception {
        var pdfManager = new PDFManager();
        pdfManager.addPageNumberingEventHandler();
        pdfManager.addBookContent(bookContent);
        pdfManager.addTableOfContents(bookContent);
        pdfManager.close();
        return pdfManager.getInputStreamResource();
    }
}
