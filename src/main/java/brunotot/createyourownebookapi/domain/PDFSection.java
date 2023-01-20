package brunotot.createyourownebookapi.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Getter
@Setter
public class PDFSection extends TraversableTree<PDFSection> {
    public static final String TITLE_SEPARATOR = "|||";
    public static final String TITLE_SEPARATOR_REGEX = "\\|\\|\\|";

    private String titlePredecessors = "";
    private String title;
    private String content;

    public int getPageNumber(final List<SimpleEntry<String, Integer>> tocPageNumbers) {
        return tocPageNumbers.stream()
                .filter(entry -> entry.getKey().equals(this.titlePredecessors + this.title))
                .findFirst()
                .map(SimpleEntry::getValue)
                .orElseThrow();
    }

    public static PDFSectionBuilder builder() {
        return new PDFSectionBuilder();
    }

    private PDFSection(final PDFSectionBuilder builder) {
        super(builder.children);
        this.title = builder.title;
        this.content = builder.content;
    }

    @Override
    protected Predicate<PDFSection> recursiveEscapePredicate() {
        return section -> !section.getTitle().isBlank();
    }

    public static class PDFSectionBuilder {
        private String title;
        private String content;
        private final List<PDFSection> children;

        public PDFSectionBuilder() {
            this.title = "";
            this.content = "";
            this.children = new ArrayList<>();
        }

        public PDFSectionBuilder title(final String title) {
            this.title = title;
            return this;
        }

        public PDFSectionBuilder content(final String content) {
            this.content = content;
            return this;
        }

        public PDFSectionBuilder subSection(final PDFSection subSection) {
            if (!this.title.isBlank()) {
                var subSectionTitlePredecessors = subSection.getTitlePredecessors();
                subSection.setTitlePredecessors(subSectionTitlePredecessors + this.title + TITLE_SEPARATOR);
            }
            this.children.add(subSection);
            return this;
        }

        public PDFSection build() {
            return new PDFSection(this);
        }
    }
}
