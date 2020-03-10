package embl.ebi.intact.callingTune.internal.task;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.*;

import java.util.HashMap;
import java.util.Map;

public class CallingTuneTask extends AbstractTask {

    private final AvailableCommands availableCommands;
    private final CommandExecutorTaskFactory commandExecutorTaskFactory;
    private final TaskManager taskManager;
    private final TaskObserver taskObserver;

    public CallingTuneTask(AvailableCommands availableCommands, CommandExecutorTaskFactory commandExecutorTaskFactory, TaskManager taskManager, TaskObserver taskObserver) {
        this.availableCommands = availableCommands;
        this.commandExecutorTaskFactory = commandExecutorTaskFactory;
        this.taskManager = taskManager;
        this.taskObserver = taskObserver;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        if (availableCommands.getNamespaces().contains("helloWorld")) {
            Map<String, Object> args = new HashMap<String, Object>(){{
                put("firstColor", "Blue");
                put("lastColor", "Red");
                put("nodeShape", "Ellipse");
            }};
            TaskIterator taskIterator = commandExecutorTaskFactory.createTaskIterator("helloWorld", "tune", args, taskObserver);
            taskManager.execute(taskIterator);

        }
    }
}
