package embl.ebi.intact.helloWorld.internal.task;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.swing.CyColorChooser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.*;

public class HelloWorldTask extends AbstractTask {
    private final CyNetworkFactory cnf;
    private final CyNetworkViewFactory cnvf;
    private final CyNetworkViewManager networkViewManager;
    private final CyNetworkManager networkManager;
    private final CyNetworkNaming cyNetworkNaming;
    private final VisualMappingManager vmm;
    private final VisualMappingFunctionFactory vmfFactoryC;


    @Tunable(description = "First color range")
    public ListSingleSelection<String> firstColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");

    @Tunable(description = "Last color range")
    public ListSingleSelection<String> lastColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");

    @Tunable(description = "Shape of the nodes")
    public ListSingleSelection<NodeShape> nodeShape = new ListSingleSelection<NodeShape>(ROUND_RECTANGLE, RECTANGLE, TRIANGLE, PARALLELOGRAM, ELLIPSE, HEXAGON, OCTAGON, DIAMOND);

    public HelloWorldTask(CyNetworkFactory cnf, CyNetworkViewFactory cnvf, CyNetworkViewManager networkViewManager, CyNetworkManager networkManager, CyNetworkNaming cyNetworkNaming, VisualMappingManager vmm, VisualMappingFunctionFactory vmfFactoryC) {
        this.cnf = cnf;
        this.cnvf = cnvf;
        this.networkViewManager = networkViewManager;
        this.networkManager = networkManager;
        this.cyNetworkNaming = cyNetworkNaming;
        this.vmm = vmm;
        this.vmfFactoryC = vmfFactoryC;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        // Create an empty network
        CyNetwork myNet = this.cnf.createNetwork();

        // add a node to the network
        CyNode node1 = myNet.addNode();
        CyNode node2 = myNet.addNode();
        CyEdge edge = myNet.addEdge(node1, node2, true);

        // set name for the new node
        CyTable nodeTable = myNet.getDefaultNodeTable();
        nodeTable.createListColumn("Hello", String.class, false);
        nodeTable.createColumn("World !", Double.class, false);

        List<String> hellos = new ArrayList<String>();
        hellos.add("Hello");
        hellos.add("Bonjour");

        nodeTable.getRow(node1.getSUID()).set("name", "Node1");
        nodeTable.getRow(node1.getSUID()).set("Hello", hellos);
        nodeTable.getRow(node1.getSUID()).set("World !", 1.2d);
        nodeTable.getRow(node2.getSUID()).set("name", "Node2");
        nodeTable.getRow(node2.getSUID()).set("World !", 5.6d);
        myNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", "Edge");

        myNet.getDefaultNetworkTable().getRow(myNet.getSUID())
                .set("name", cyNetworkNaming.getSuggestedNetworkTitle("My Network"));

        if (myNet == null)
            return;
        this.networkManager.addNetwork(myNet);

        final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNet);
        CyNetworkView myView = null;
        if (views.size() != 0)
            myView = views.iterator().next();

        if (myView == null) {
            // create a new view for my network
            myView = cnvf.createNetworkView(myNet);
            networkViewManager.addNetworkView(myView);
        } else {
            System.out.println("networkView already existed.");
        }


        for (CyNode node : myNet.getNodeList()) {
            List names = nodeTable.getRow(node.getSUID()).get("Hello", List.class);
            String firstHello = "";
            if (names != null && !names.isEmpty()) {
                firstHello = (String) names.get(0);
            }

            View<CyNode> nView = myView.getNodeView(node);
            nView.setLockedValue(BasicVisualLexicon.NODE_LABEL, firstHello);
        }


        ContinuousMapping<Double, Paint> mapping = (ContinuousMapping<Double, Paint>) vmfFactoryC.createVisualMappingFunction("World !", Double.class, BasicVisualLexicon.NODE_FILL_COLOR);

        List<Double> worlds = nodeTable.getColumn("World !").getValues(Double.class);
        double min = Collections.min(worlds);
        double max = Collections.max(worlds);
        Paint minColor = getColorByName(firstColor.getSelectedValue(), Color.BLACK);
        Paint maxColor = getColorByName(lastColor.getSelectedValue(), Color.BLACK);

        Paint color = CyColorChooser.showDialog(null, "Whatever", Color.CYAN);

        BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<>(minColor, minColor, minColor);
        mapping.addPoint(min, brv1);

        BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<>(maxColor, maxColor, maxColor);
        mapping.addPoint(max, brv2);


        VisualStyle style = vmm.getVisualStyle(myView);
        style.setTitle("French Style");
        style.addVisualMappingFunction(mapping);
        style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, nodeShape.getSelectedValue());
        style.apply(myView);
        myView.updateView();


        // Set the variable destroyView to true, the following snippet of code
        // will destroy a view
        boolean destroyView = false;
        if (destroyView) {
            networkViewManager.destroyNetworkView(myView);
        }
    }

    public static Color getColorByName(String name, Color ifNull) {
        try {
            return (Color) Color.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return ifNull;
        }
    }
}
