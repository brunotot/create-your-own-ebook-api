package brunotot.createyourownebookapi.controller;

import brunotot.createyourownebookapi.openai.service.ChatBotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatBotController {
    private final ChatBotService chatBotService;

    public ChatBotController(final ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @GetMapping("/ask")
    public String ask(final @RequestParam String prompt) {
        return this.chatBotService
                .ask(prompt)
                .replaceAll("[\\t\\r\\n]+", "\n")
                .replace("\n", "<br>");
    }
}
