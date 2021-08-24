package ru.agentlab.changetracking.sail;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ChangeTrackingEvents {

    private final Sinks.Many<TransactionChanges> sink;
    private final Scheduler defaultScheduler;

    public ChangeTrackingEvents() {
        sink = Sinks.many().multicast().onBackpressureBuffer();
        defaultScheduler = Schedulers.newSingle("changetracking");
    }

    public Sinks.EmitResult nextEvent(TransactionChanges changes) {
        return sink.tryEmitNext(changes);
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
