package embl.ebi.intact.helloWorld.internal.model;

import embl.ebi.intact.helloWorld.internal.task.HelloWorldTask;

import java.time.Duration;
import java.util.function.Supplier;

public enum Functions {
    JSON_FILE(HelloWorldTask::queryLocalNeo4jServerWithJsonFile),
    JSON_STREAM(HelloWorldTask::queryLocalNeo4jServerWithJsonStreamed),
    CSV_FILE(HelloWorldTask::queryLocalNeo4jServerWithCSVFile),
    CSV_STREAM(HelloWorldTask::queryLocalNeo4jServerWithCSVStreamed);

    private final Supplier<Duration> function;

    Functions(Supplier<Duration> function) {
        this.function = function;
    }

    public Duration execute() {
        return function.get();
    }
}
