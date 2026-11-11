package com.pm.lovable_clone.controller;



import com.pm.lovable_clone.service.AiCodeGeneratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/generator")
@CrossOrigin(origins = "*")
public class GeneratorController {

    private final AiCodeGeneratorService aiCodeGeneratorService;

    // Spring automatically injects MockAiCodeGeneratorService
    public GeneratorController(AiCodeGeneratorService aiCodeGeneratorService) {
        this.aiCodeGeneratorService = aiCodeGeneratorService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCodeGeneration(@RequestParam String prompt) {
        SseEmitter emitter = new SseEmitter(180_000L);
        aiCodeGeneratorService.streamAppGeneration(prompt, emitter);
        return emitter;
    }
}
//Key Concepts in This Code:
//MediaType.TEXT_EVENT_STREAM_VALUE: Sets the HTTP header Content-Type: text/event-stream. This instructs the browser to keep the connection open and process data as a continuous stream.
//SseEmitter: Spring's built-in tool for pushing events from the server to the client.
//Virtual Threads (newVirtualThreadPerTaskExecutor): Java's lightweight threads (Project Loom) handle the wait/sleep time with minimal memory overhead compared to traditional OS threads.