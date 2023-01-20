package brunotot.createyourownebookapi.manager;

import brunotot.createyourownebookapi.domain.PDFSection;
import brunotot.createyourownebookapi.itext.eventhandler.PageNumberEventHandler;
import brunotot.createyourownebookapi.itext.renderer.UpdatePageRenderer;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.properties.TabAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static brunotot.createyourownebookapi.domain.PDFSection.TITLE_SEPARATOR;
import static brunotot.createyourownebookapi.domain.PDFSection.TITLE_SEPARATOR_REGEX;

public class PDFManager {
    private static final PdfFont FONT_TIMES_ROMAN = createFont(StandardFonts.TIMES_ROMAN);
    private static final PdfFont FONT_BOLD = createFont(StandardFonts.HELVETICA_BOLD);
    private static final String TOC_TITLE = "Table of Contents";
    private static final String TOC_DEST = "toc";
    private static final Integer TOC_TAB_POSITION = 580;
    private static final float TOC_MARGIN_LEFT_DELTA = 15;
    private static final List<TabStop> TAB_STOPS = List.of(new TabStop(
            TOC_TAB_POSITION,
            TabAlignment.RIGHT,
            new DottedLine()
    ));

    private final PageNumberEventHandler pageNumberingEventHandler;
    private final PdfDocument pdfDoc;
    private final Document doc;
    private final List<AbstractMap.SimpleEntry<String, Integer>> tocPageNumbers;
    private final ByteArrayOutputStream baos;

    private PdfOutline outline;

    @SuppressWarnings("all")
    public PDFManager() throws Exception {
        this.baos = new ByteArrayOutputStream();
        this.pdfDoc = new PdfDocument(new PdfWriter(this.baos));
        this.pageNumberingEventHandler = new PageNumberEventHandler();
        this.doc = new Document(this.pdfDoc);
        this.outline = this.pdfDoc.getOutlines(false);
        this.addOutline(TOC_TITLE, TOC_DEST, false);
        this.setupDocumentBaseTextStyle();
        this.tocPageNumbers = new ArrayList<>();
    }

    public InputStreamResource getInputStreamResource() {
        return new InputStreamResource(new ByteArrayInputStream(this.baos.toByteArray()));
    }

    public void addPageNumberingEventHandler() {
        this.pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, this.pageNumberingEventHandler);
    }

    public void addTableOfContents(final PDFSection content) {
        this.generateTOC(content);
        this.pdfDoc.movePage(this.pdfDoc.getNumberOfPages(), 1);
    }

    public void addOutline(final String title, final String destinationKey, final boolean propagateToDropdown) {
        var createdOutline = this.outline.addOutline(title);
        createdOutline.addDestination(PdfDestination.makeDestination(new PdfString(destinationKey)));
        if (propagateToDropdown) {
            this.outline = createdOutline;
        }
    }

    public void addBookContent(final PDFSection bookContent) {
        this.addBookContent(bookContent, "");
    }

    public void close() {
        this.doc.close();
    }

    private Paragraph buildTocRow(final String title, final int pageNumber, final int indentLevel) {
        return new Paragraph()
                .setMarginLeft(indentLevel * TOC_MARGIN_LEFT_DELTA)
                .addTabStops(TAB_STOPS)
                .add(title)
                .add(new Tab())
                .add(String.valueOf(pageNumber))
                .setAction(PdfAction.createGoTo(title));
    }

    private void generateTOC(final PDFSection content) {
        this.pageNumberingEventHandler.setBuildingTocFinished(true);
        this.doc.add(new AreaBreak());
        this.doc.add(new Paragraph(TOC_TITLE).setFont(FONT_BOLD).setDestination(TOC_DEST));
        this.generateTOC(content, -1);
    }

    private void generateTOC(final PDFSection content, final int indentLevel) {
        var title = content.getTitle();
        if (!title.isBlank()) {
            var tocRow = this.buildTocRow(title, content.getPageNumber(this.tocPageNumbers), indentLevel);
            this.doc.add(tocRow);
        }
        for (var section : content.getChildren()) {
            this.generateTOC(section, indentLevel + 1);
        }
    }

    private void addBookContent(final PDFSection bookContent, final String titlePrefix) {
        for (var section : bookContent.getChildren()) {
            var title = section.getTitle();
            var content = section.getContent();
            var titleKey = titlePrefix + title;

            if (section.hasChildren()) {
                this.addTocRow(titleKey, content, true);
                this.addBookContent(section, titleKey + TITLE_SEPARATOR);
            } else {
                this.addTocRow(titleKey, content, false);
            }
        }
    }

    private void addTocRow(final String titleKey, final String content, final boolean propagateToDropdown) {
        var pdfDoc = this.doc.getPdfDocument();
        var titleKeySplit = titleKey.split(TITLE_SEPARATOR_REGEX);
        var titleKeySplitLength = titleKeySplit.length;
        var title = titleKeySplit[titleKeySplitLength - 1];

        if (!title.isBlank()) {
            Paragraph titleParagraph = new Paragraph(title);
            titleParagraph.setKeepTogether(true);
            this.addOutline(title, titleKey, propagateToDropdown);
            var titlePage = new AbstractMap.SimpleEntry<>(titleKey, pdfDoc.getNumberOfPages());
            titleParagraph.setFont(FONT_BOLD)
                    .setFontSize(12)
                    .setKeepWithNext(true)
                    .setDestination(title)
                    .setNextRenderer(new UpdatePageRenderer(titleParagraph, titlePage));
            this.doc.add(titleParagraph);
            this.tocPageNumbers.add(titlePage);
        }

        if (!content.isBlank()) {
            Paragraph contentParagraph = new Paragraph(content);
            contentParagraph.setKeepTogether(true);
            contentParagraph.setFirstLineIndent(36);
            contentParagraph.setMarginBottom(0);
            this.doc.add(contentParagraph);
        }
    }

    private void setupDocumentBaseTextStyle() {
        this.doc.setTextAlignment(TextAlignment.JUSTIFIED)
                .setFont(FONT_TIMES_ROMAN)
                .setFontSize(11);
    }

    private static PdfFont createFont(final String font) {
        try {
            return PdfFontFactory.createFont(font);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
