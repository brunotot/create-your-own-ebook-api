package brunotot.createyourownebookapi.itext.eventhandler;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

public class PageNumberEventHandler implements IEventHandler {
    private boolean buildingTocFinished = false;

    public void setBuildingTocFinished(final boolean buildingTocFinished) {
        this.buildingTocFinished = buildingTocFinished;
    }

    @Override
    public void handleEvent(final Event event) {
        if (this.buildingTocFinished) {
            return;
        }
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfPage page = docEvent.getPage();
        int pageNumber = docEvent.getDocument().getPageNumber(page);
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(
                page.newContentStreamBefore(),
                page.getResources(),
                docEvent.getDocument()
        );
        Canvas canvas = new Canvas(pdfCanvas, docEvent.getPage().getPageSize());
        String text = String.valueOf(pageNumber);
        canvas.showTextAligned(
                text,
                pageSize.getWidth() / 2,
                15,
                TextAlignment.CENTER,
                VerticalAlignment.BOTTOM,
                0
        );
    }
}