package brunotot.createyourownebookapi.itext.renderer;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.renderer.ParagraphRenderer;

import java.util.AbstractMap.SimpleEntry;

public class UpdatePageRenderer extends ParagraphRenderer {
    protected SimpleEntry<String, Integer> entry;

    public UpdatePageRenderer(final Paragraph modelElement, final SimpleEntry<String, Integer> entry) {
        super(modelElement);
        this.entry = entry;
    }

    @Override
    public LayoutResult layout(final LayoutContext layoutContext) {
        LayoutResult result = super.layout(layoutContext);
        entry.setValue(layoutContext.getArea().getPageNumber());
        return result;
    }
}
