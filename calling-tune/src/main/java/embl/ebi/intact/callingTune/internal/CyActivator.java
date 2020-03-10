package embl.ebi.intact.callingTune.internal;

import embl.ebi.intact.callingTune.internal.task.CallingTuneTaskFactory;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.*;
import org.osgi.framework.BundleContext;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

/**
 * {@code CyActivator} is a class that is a starting point for OSGi bundles.
 * <p>
 * A quick overview of OSGi: The common currency of OSGi is the <i>service</i>.
 * A service is merely a Java interface, along with objects that implement the
 * interface. OSGi establishes a system of <i>bundles</i>. Most bundles import
 * services. Some bundles export services. Some do both. When a bundle exports a
 * service, it provides an implementation to the service's interface. Bundles
 * import a service by asking OSGi for an implementation. The implementation is
 * provided by some other bundle.
 * <p>
 * When OSGi starts your bundle, it will invoke {@CyActivator}'s
 * {@code start} method. So, the {@code start} method is where
 * you put in all your code that sets up your app. This is where you import and
 * export services.
 * <p>
 * Your bundle's {@code Bundle-Activator} manifest entry has a fully-qualified
 * path to this class. It's not necessary to inherit from
 * {@code AbstractCyActivator}. However, we provide this class as a convenience
 * to make it easier to work with OSGi.
 * <p>
 * Note: AbstractCyActivator already provides its own {@code stop} method, which
 * {@code unget}s any services we fetch using getService().
 */
public class CyActivator extends AbstractCyActivator {
    /**
     * This is the {@code start} method, which sets up your app. The
     * {@code BundleContext} object allows you to communicate with the OSGi
     * environment. You use {@code BundleContext} to import services or ask OSGi
     * about the status of some service.
     */
    @Override
    public void start(BundleContext context) {
        AvailableCommands availableCommands = getService(context, AvailableCommands.class);
        CommandExecutorTaskFactory commandExecutorTaskFactory = getService(context, CommandExecutorTaskFactory.class);
        TaskManager taskManager = getService(context, TaskManager.class);
        TaskObserver taskObserver = new TaskObserver() {
            @Override
            public void taskFinished(ObservableTask task) {
                System.out.println(task.toString() + " finished");
            }

            @Override
            public void allFinished(FinishStatus finishStatus) {
                System.out.println(finishStatus.toString());
            }
        };

        CallingTuneTaskFactory callingTuneTaskFactory = new CallingTuneTaskFactory(availableCommands, commandExecutorTaskFactory, taskManager, taskObserver);

        Properties callingTuneProps = new Properties();
        callingTuneProps.setProperty(PREFERRED_MENU, "Apps.Caller");
        callingTuneProps.setProperty(TITLE, "Call tune");
        callingTuneProps.setProperty(COMMAND_NAMESPACE, "call");
        callingTuneProps.setProperty(COMMAND, "call-tune");

        registerService(context, callingTuneTaskFactory, TaskFactory.class, callingTuneProps);
    }
}
