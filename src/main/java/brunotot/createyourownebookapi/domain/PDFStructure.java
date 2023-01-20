package brunotot.createyourownebookapi.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

@Getter
public class PDFStructure extends TraversableTree<PDFStructure> {
    private final String title;

    @Override
    protected Predicate<PDFStructure> recursiveEscapePredicate() {
        return structure -> !structure.getTitle().isBlank();
    }

    private PDFStructure(final String title) {
        super(new ArrayList<>());
        this.title = title;
    }

    public PDFSection getAsPDFSection() {
        return PDFStructure.getAsPDFSection(this, true);
    }

    public PDFStructure addSection(final String title) {
        this.children.add(PDFStructure.build(title));
        return this;
    }

    public PDFStructure addSection(final PDFStructure structure) {
        Objects.requireNonNull(structure);
        this.children.add(structure);
        return this;
    }

    public static PDFStructure build(final String title) {
        return new PDFStructure(title);
    }

    private static PDFSection getAsPDFSection(final PDFStructure pdfStructure, final boolean isRoot) {
        PDFSection.PDFSectionBuilder pdfSectionBuilder = PDFSection.builder();
        if (!isRoot) {
            pdfSectionBuilder.title(pdfStructure.getTitle());
        }
        for (final var child : pdfStructure.getChildren()) {
            pdfSectionBuilder.subSection(getAsPDFSection(child, false));
        }
        return pdfSectionBuilder.build();
    }
}
