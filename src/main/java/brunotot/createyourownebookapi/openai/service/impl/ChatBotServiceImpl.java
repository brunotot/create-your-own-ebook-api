package brunotot.createyourownebookapi.openai.service.impl;

import brunotot.createyourownebookapi.domain.ChatPromptBuilder;
import brunotot.createyourownebookapi.domain.PDFSection;
import brunotot.createyourownebookapi.domain.PDFStructure;
import brunotot.createyourownebookapi.openai.service.ChatBotService;
import brunotot.createyourownebookapi.parser.ChatBotParser;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import retrofit2.HttpException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ChatBotServiceImpl implements ChatBotService {
    private static final long OPENAI_SLEEP_TIMEOUT_MS = 5000;
    private static final Integer OPENAI_TIMEOUT_RETRIES = 10;
    private static final String OPENAI_SERVICE_UNAVAILABLE_MESSAGE_FORMAT =
            "503 OpenAI Service Unavailable. Retrying again in %s ms (%d/%d)";
    private final Integer maxTokens;
    private final String model;
    private final OpenAiService openAiService;

    public ChatBotServiceImpl(
            final OpenAiService openAiService,
            final @Value("${openai.gpt.model}") String model,
            final @Value("${openai.gpt.maxTokens}") Integer maxTokens
    ) {
        this.openAiService = openAiService;
        this.maxTokens = maxTokens;
        this.model = model;
    }

    public String ask(final String prompt) {
        return this.createCompletion(this.buildRequest(prompt))
                .getChoices()
                .stream()
                .map(CompletionChoice::getText)
                .findFirst()
                .orElseThrow()
                .strip();
    }

    @Override
    public PDFStructure getPDFStructure(final String pdfTitle) {
        return this.getPDFStructure(pdfTitle, "");
    }

    @Override
    public PDFStructure getPDFStructure(final String bookTitle, final String additionalInfo) {
        var prompt = ChatPromptBuilder.buildBookOutlinePrompt(bookTitle);
        var response = this.ask(prompt + additionalInfo);
        return ChatBotParser.parsePDFStructure(bookTitle, response);
    }

    @Override
    public PDFSection getPDFSection(final PDFStructure pdfStructure) {
        PDFSection skeleton = pdfStructure.getAsPDFSection();
        final var bookTitle = pdfStructure.getTitle();
        final var recursiveLength = skeleton.getRecursiveLength();
        AtomicReference<Double> totalTime = new AtomicReference<>((double) 0);
        AtomicInteger ordinal = new AtomicInteger(1);
        skeleton.forEach((section) -> {
            var chapterTitle = section.getTitle();
            var prompt = ChatPromptBuilder.buildChapterContentPrompt(bookTitle, chapterTitle);
            StopWatch watch = new StopWatch();
            watch.start();
            var content = this.ask(prompt);
            watch.stop();
            var timeOfExecution = watch.getTotalTimeSeconds();
            totalTime.updateAndGet(v -> v + timeOfExecution);
            section.setContent(content);
            log.info(String.format("Finished chapter: \"%s\" in %.0f seconds. Progress: %d/%d",
                    chapterTitle,
                    timeOfExecution,
                    ordinal.getAndIncrement(),
                    recursiveLength));
        });
        final var average = totalTime.get() / recursiveLength;
        log.info("Total time of execution: " + totalTime.get() + " seconds. Average: " + average + " seconds");
        return skeleton;
    }

    private CompletionRequest buildRequest(final String prompt) {
        return CompletionRequest.builder()
                .prompt(prompt)
                .maxTokens(this.maxTokens)
                .model(this.model)
                .build();
    }

    private CompletionResult createCompletion(final CompletionRequest request) {
        return this.createCompletion(request, 0);
    }

    private CompletionResult createCompletion(final CompletionRequest request, final Integer retryCount) {
        try {
            return this.openAiService.createCompletion(request);
        } catch (final HttpException httpException) {
            if (HttpStatus.SERVICE_UNAVAILABLE.value() == httpException.code() || retryCount > OPENAI_TIMEOUT_RETRIES) {
                try {
                    log.warn(buildServiceUnavailableLogMessage(retryCount));
                    Thread.sleep(OPENAI_SLEEP_TIMEOUT_MS);
                    return this.createCompletion(request, retryCount + 1);
                } catch (final Exception ignored) {
                    throw httpException;
                }
            } else {
                throw httpException;
            }
        }
    }

    private static String buildServiceUnavailableLogMessage(final int retryCounter) {
        return String.format(OPENAI_SERVICE_UNAVAILABLE_MESSAGE_FORMAT,
                OPENAI_SLEEP_TIMEOUT_MS,
                retryCounter,
                OPENAI_TIMEOUT_RETRIES);
    }
}
