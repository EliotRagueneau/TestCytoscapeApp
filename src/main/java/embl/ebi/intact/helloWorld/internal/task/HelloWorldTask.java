package embl.ebi.intact.helloWorld.internal.task;

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

import javax.swing.*;
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
    private CyTable nodeTable;
    private CyNetwork network;
    private CyNetworkView networkView;

    @Tunable(description = "First color range")
    public ListSingleSelection<String> firstColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");

    @Tunable(description = "Last color range")
    public ListSingleSelection<String> lastColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");

    @Tunable(description = "Shape of the nodes")
    public ListSingleSelection<NodeShape> nodeShape = new ListSingleSelection<>(ROUND_RECTANGLE, RECTANGLE, TRIANGLE, PARALLELOGRAM, ELLIPSE, HEXAGON, OCTAGON, DIAMOND);

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
    public void run(TaskMonitor taskMonitor) {
        setupNetwork();
        setupCyNetworkView();
        setupStyle();
    }

    private void setupNetwork() {
        network = this.cnf.createNetwork();

        CyNode node1 = network.addNode();
        CyNode node2 = network.addNode();
        CyEdge edge = network.addEdge(node1, node2, true);

        // set name for the new node
        nodeTable = network.getDefaultNodeTable();
        nodeTable.createListColumn("Hello", String.class, false);
        nodeTable.createColumn("World !", Double.class, false);
        nodeTable.createColumn("Style::Color", Color.class, false);

        List<String> hellos = new ArrayList<String>() {{
            add("Hello");
            add("Bonjour");
        }};


        CyRow node1Row = nodeTable.getRow(node1.getSUID());

        node1Row.set("name", "Node1");
        node1Row.set("Hello", hellos);
        node1Row.set("World !", 1.2d);
        node1Row.set("Style::Color", Color.BLUE);

        CyRow node2Row = nodeTable.getRow(node2.getSUID());

        node2Row.set("name", "Node2");
        node2Row.set("World !", 5.6d);

        network.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", "Edge");

        network.getDefaultNetworkTable().getRow(network.getSUID())
                .set("name", cyNetworkNaming.getSuggestedNetworkTitle("My Network"));

        this.networkManager.addNetwork(network);
    }

    private void setupCyNetworkView() {
        final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
        networkView = null;
        if (views.size() != 0)
            networkView = views.iterator().next();

        if (networkView == null) {
            // create a new view for my network
            networkView = cnvf.createNetworkView(network);
            networkViewManager.addNetworkView(networkView);
        } else {
            System.out.println("networkView already existed.");
        }
    }

    private void setupStyle() {
        VisualStyle style = vmm.getVisualStyle(networkView);
        style.setTitle("French Style");

        setupNodeLabels();
        style.addVisualMappingFunction(getWorldToColorMapping());
        style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, nodeShape.getSelectedValue());

        style.apply(networkView);
        networkView.updateView();
    }

    private void setupNodeLabels() {
        for (CyNode node : network.getNodeList()) {
            List<?> names = nodeTable.getRow(node.getSUID()).get("Hello", List.class);
            String firstHello = "";
            if (names != null && !names.isEmpty()) {
                firstHello = (String) names.get(0);
            }

            View<CyNode> nView = networkView.getNodeView(node);
            nView.setLockedValue(BasicVisualLexicon.NODE_LABEL, firstHello);
        }
    }

    private ContinuousMapping<Double, Paint> getWorldToColorMapping() {
        ContinuousMapping<Double, Paint> mapping = (ContinuousMapping<Double, Paint>) vmfFactoryC.createVisualMappingFunction("World !", Double.class, BasicVisualLexicon.NODE_FILL_COLOR);

        List<Double> worlds = nodeTable.getColumn("World !").getValues(Double.class);
        double min = Collections.min(worlds);
        double max = Collections.max(worlds);

        Paint minColor = getColorByName(firstColor.getSelectedValue(), Color.BLACK);
        Paint maxColor = getColorByName(lastColor.getSelectedValue(), Color.BLACK);

        BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<>(minColor, minColor, minColor);
        mapping.addPoint(min, brv1);

        BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<>(maxColor, maxColor, maxColor);
        mapping.addPoint(max, brv2);
        return mapping;
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
