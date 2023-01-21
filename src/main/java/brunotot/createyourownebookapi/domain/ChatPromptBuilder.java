package brunotot.createyourownebookapi.domain;

public final class ChatPromptBuilder {
    private ChatPromptBuilder() {
        // NOOP
    }

    public static String buildLanguagePrompt(final String language) {
        return String.format("Please answer in %s language.", language);
    }

    public static String buildChapterContentPrompt(final String bookTitle, final String chapterTitle) {
        return String.format("For an ebook called \"%s\" write a chapter for \"%s\" but leave out the chapter title",
                bookTitle,
                chapterTitle
        );
    }

    public static String buildBookOutlinePrompt(final String bookTitle) {
        return String.format("Write me an outline for an ebook on \"%s\" but only answer in a format \"1. Heading\" for headings and \"A. Subheading\" for subheadings and separate both headings and subheadings with a newline.",
                bookTitle);
    }
}
