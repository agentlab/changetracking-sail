package ru.agentlab.changetracking.sail;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ChangeTrackingEvents {

    private final Sinks.Many<TransactionChanges> sink;
    private final Sinks.EmitFailureHandler retryHandler = (ignore, emitResult) -> emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);
    private final Scheduler defaultScheduler;

    public ChangeTrackingEvents(int eventsBufferSize) {
        sink = Sinks.many().multicast().onBackpressureBuffer(eventsBufferSize, false);
        defaultScheduler = Schedulers.newSingle("changetracking");
    }

    public void nextEvent(TransactionChanges changes) {
        sink.emitNext(changes, retryHandler);
    }

    public Flux<TransactionChanges> events() {
        return sink.asFlux().publishOn(defaultScheduler);
    }

    public Flux<TransactionChanges> events(Scheduler scheduler) {
        return sink.asFlux().publishOn(scheduler);
    }

    public Sinks.EmitResult close() {
        var result = sink.tryEmitComplete();
        defaultScheduler.dispose();
        return result;
    }

}
