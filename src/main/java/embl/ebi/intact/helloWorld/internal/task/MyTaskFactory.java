package embl.ebi.intact.helloWorld.internal.task;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MyTaskFactory extends AbstractTaskFactory {

    private final CyNetworkFactory cnf;
    private final CyNetworkViewFactory cnvf;
    private final CyNetworkViewManager networkViewManager;
    private final CyNetworkManager networkManager;
    private final CyNetworkNaming cyNetworkNaming;
    private final VisualMappingManager vmm;
    private final VisualMappingFunctionFactory vmfFactoryC;

    public MyTaskFactory(CyNetworkFactory cnf, CyNetworkManager networkManager, CyNetworkViewFactory cnvf, CyNetworkViewManager networkViewManager, CyNetworkNaming cyNetworkNaming, VisualMappingManager vmm, VisualMappingFunctionFactory vmfFactoryC) {
        this.cnf = cnf;
        this.cnvf = cnvf;
        this.networkViewManager = networkViewManager;
        this.networkManager = networkManager;
        this.cyNetworkNaming = cyNetworkNaming;
        this.vmm = vmm;
        this.vmfFactoryC = vmfFactoryC;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new HelloWorldTask(cnf, cnvf, networkViewManager, networkManager, cyNetworkNaming, vmm, vmfFactoryC));
    }
}
