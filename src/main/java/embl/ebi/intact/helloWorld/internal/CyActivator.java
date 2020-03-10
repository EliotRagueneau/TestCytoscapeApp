package embl.ebi.intact.helloWorld.internal;

import embl.ebi.intact.helloWorld.internal.listener.ChangeNodeShapeOnSelection;
import embl.ebi.intact.helloWorld.internal.task.MyTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.osgi.framework.BundleContext;

import java.util.*;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
    public CyActivator() {
        super();
    }


    public void start(BundleContext bc) {

        CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);


        MyTaskFactory myFactory = new MyTaskFactory(registrar);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.HelloWorld");
        props.setProperty(TITLE, "Hello World");
        props.setProperty(COMMAND_NAMESPACE, "helloWorld");
        props.setProperty(COMMAND, "tune");
        registerService(bc, myFactory, TaskFactory.class, props);


//        CyNetworkTableManager cyNetworkTableManager = getService(bc, CyNetworkTableManager.class);
//        CyEventHelper cyEventHelper = getService(bc, CyEventHelper.class);
//
//        ChangeNodeShapeOnSelection listener = new ChangeNodeShapeOnSelection(cyEventHelper, cnvm,cyNetworkTableManager);
//        registerService(bc, listener, RowsSetListener.class, new Properties());
    }


}

