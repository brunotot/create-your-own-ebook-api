package brunotot.createyourownebookapi.openai.service;

import brunotot.createyourownebookapi.domain.PDFStructure;
import brunotot.createyourownebookapi.domain.PDFSection;

public interface ChatBotService {
    String ask(String prompt);
    PDFStructure getPDFStructure(String pdfTitle, String additionalInfo, String language);
    PDFStructure getPDFStructure(String pdfTitle);
    PDFSection getPDFSection(PDFStructure pdfStructure, String language);
}
