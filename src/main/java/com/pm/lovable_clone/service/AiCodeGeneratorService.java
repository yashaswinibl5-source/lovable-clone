package com.pm.lovable_clone.service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
public interface AiCodeGeneratorService {
    void streamAppGeneration(String prompt, SseEmitter emitter);
}