package brunotot.createyourownebookapi.parser;

import brunotot.createyourownebookapi.domain.PDFStructure;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ChatBotParser {
    private ChatBotParser() {
        // NOOP
    }

    public static PDFStructure parsePDFStructure(final String bookTitle, final String pdfStructureString) {
        List<String> chunks = Stream
                .of(pdfStructureString.split("\n"))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .toList();
        var pdfStructure = PDFStructure.build(bookTitle);
        PDFStructure headingStructure = null;
        boolean lastMainHeading = false;
        for (var chunk : chunks) {
            var firstCharacter = chunk.charAt(0);
            var isMainHeading = Character.isDigit(firstCharacter);
            var chunkTitle = parseBookOutlineChunkTitle(chunk);
            if (isMainHeading) {
                if (!Objects.isNull(headingStructure)) {
                    pdfStructure.addSection(headingStructure);
                }
                headingStructure = PDFStructure.build(chunkTitle);
                lastMainHeading = true;
            } else {
                if (!Objects.isNull(headingStructure)) {
                    headingStructure.addSection(chunkTitle);
                }
                lastMainHeading = false;
            }
        }
        if (lastMainHeading) {
            pdfStructure.addSection(headingStructure);
        }
        return pdfStructure;
    }

    private static String parseBookOutlineChunkTitle(final String bookOutlineChunk) {
        return bookOutlineChunk.replaceFirst("^([^.]+)\\. *", "");
    }
}
