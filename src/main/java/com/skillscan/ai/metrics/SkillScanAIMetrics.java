package com.skillscan.ai.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

// Centralized metrics handler using Micrometer (Prometheus/Grafana integration)
@Component
@RequiredArgsConstructor
public class SkillScanAIMetrics {

    // Core registry to register all metrics
    private final MeterRegistry registry;

    // Counters (increment-only metrics)
    private Counter llmCallCounter;        // counts LLM API calls
    private Counter cacheHitCounter;       // counts cache hits
    private Counter cacheMissCounter;      // counts cache misses
    private Counter analysisRequestCounter;// counts incoming analysis requests

    // Timers (measure execution time)
    private Timer llmResponseTimer;        // tracks LLM response latency
    private Timer analysisTimer;           // tracks full analysis time

    // Gauge (dynamic value)
    private final AtomicLong activeAnalysis = new AtomicLong(0); // active analysis count

    // Initialize and register all metrics
    @PostConstruct
    public void init() {

        llmCallCounter = Counter.builder("skillscanai.metrics.llm.calls")
                .register(registry);

        cacheHitCounter = Counter.builder("skillscanai.metrics.cache.hit")
                .register(registry);

        cacheMissCounter = Counter.builder("skillscanai.metrics.cache.miss")
                .register(registry);

        analysisRequestCounter = Counter.builder("skillscanai.metrics.analysis.requests")
                .register(registry);

        llmResponseTimer = Timer.builder("skillscanai.metrics.llm.response.time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        analysisTimer = Timer.builder("skillscanai.metrics.analysis.time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // Tracks active analysis count
        Gauge.builder("skillscanai.metrics.analysis.active", activeAnalysis, AtomicLong::get)
                .register(registry);
    }

    // Increment LLM call count
    public void recordLlmCall() {
        llmCallCounter.increment();
    }

    // Increment cache hit count
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }

    // Increment cache miss count
    public void recordCacheMiss() {
        cacheMissCounter.increment();
    }

    // Increment analysis request count
    public void recordAnalysisRequest() {
        analysisRequestCounter.increment();
    }

    // Measure LLM execution time
    public <T> T timeLlm(Supplier<T> operation) {
        return llmResponseTimer.record(operation);
    }

    // Measure analysis time + track active executions
    public <T> T timeAnalysis(Supplier<T> operation) {
        activeAnalysis.incrementAndGet();
        try {
            return analysisTimer.record(operation);
        } finally {
            activeAnalysis.decrementAndGet();
        }
    }

    // Record categorized errors using tags
    public void recordError(String type) {
        Counter.builder("skillscanai.metrics.error")
                .tag("type", type)
                .register(registry)
                .increment();
    }
}