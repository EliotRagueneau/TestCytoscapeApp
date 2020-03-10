package embl.ebi.intact.callingTune.internal.task;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class CallingTuneTaskFactory extends AbstractTaskFactory {

    private final AvailableCommands availableCommands;
    private final CommandExecutorTaskFactory commandExecutorTaskFactory;
    private final TaskManager taskManager;
    private final TaskObserver taskObserver;

    public CallingTuneTaskFactory(AvailableCommands availableCommands, CommandExecutorTaskFactory commandExecutorTaskFactory, TaskManager taskManager, TaskObserver taskObserver) {
        this.availableCommands = availableCommands;
        this.commandExecutorTaskFactory = commandExecutorTaskFactory;
        this.taskManager = taskManager;
        this.taskObserver = taskObserver;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new CallingTuneTask(availableCommands, commandExecutorTaskFactory, taskManager, taskObserver));
    }
}
