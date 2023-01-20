package brunotot.createyourownebookapi.itext.service;

import brunotot.createyourownebookapi.domain.PDFSection;
import org.springframework.core.io.InputStreamResource;

public interface PDFService {
    InputStreamResource createPDF(PDFSection skeleton) throws Exception;
}
