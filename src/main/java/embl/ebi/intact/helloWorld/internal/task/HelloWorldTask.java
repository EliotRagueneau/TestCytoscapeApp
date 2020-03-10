package embl.ebi.intact.helloWorld.internal.task;

import org.cytoscape.model.*;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;

import java.util.*;
import java.util.List;

public class HelloWorldTask extends AbstractTask {
    private final CyServiceRegistrar registrar;
    private final CyNetwork network;
    private  CyNetworkView networkView;
    private final TaskManager taskManager;
    private CyTable edgeTable;
    private HideTaskFactory hideTaskFactory;
    private final UnHideTaskFactory unHideTaskFactory;

//
//    @Tunable(description = "First color range")
//    public ListSingleSelection<String> firstColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");
//
//    @Tunable(description = "Last color range")
//    public ListSingleSelection<String> lastColor = new ListSingleSelection<>("Red", "Green", "Blue", "Black");
//
//    @Tunable(description = "Shape of the nodes")
//    public ListSingleSelection<NodeShape> nodeShape = new ListSingleSelection<NodeShape>(ROUND_RECTANGLE, RECTANGLE, TRIANGLE, PARALLELOGRAM, ELLIPSE, HEXAGON, OCTAGON, DIAMOND);

    public HelloWorldTask(CyServiceRegistrar registrar) {
        this.registrar = registrar;

        CyNetworkFactory cnf = registrar.getService(CyNetworkFactory.class);
        CyNetworkViewFactory cnvf = registrar.getService(CyNetworkViewFactory.class);
        CyNetworkViewManager networkViewManager = registrar.getService(CyNetworkViewManager.class);
        CyNetworkManager networkManager = registrar.getService(CyNetworkManager.class);
        taskManager = registrar.getService(TaskManager.class);

        network = cnf.createNetwork();
        networkManager.addNetwork(network);

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

        edgeTable = network.getDefaultEdgeTable();

        hideTaskFactory = registrar.getService(HideTaskFactory.class);
        unHideTaskFactory = registrar.getService(UnHideTaskFactory.class);
    }

    private static class ComparableUndirectedEdge {
        CyNode node1;
        CyNode node2;

        public List<ComparableUndirectedEdge> fromEdgeList(List<CyEdge> edges) {
            List<ComparableUndirectedEdge> comparableUndirectedEdges = new ArrayList<>();
            for (CyEdge edge : edges) {
                comparableUndirectedEdges.add(new ComparableUndirectedEdge(edge));
            }
            return comparableUndirectedEdges;
        }

        ComparableUndirectedEdge(CyEdge edge) {
            this.node1 = edge.getSource();
            this.node2 = edge.getTarget();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ComparableUndirectedEdge)) return false;
            ComparableUndirectedEdge that = (ComparableUndirectedEdge) o;
            return (Objects.equals(node1, that.node1) && Objects.equals(node2, that.node2)) ||
                    (Objects.equals(node1, that.node2) && Objects.equals(node2, that.node1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(node1) + Objects.hash(node2);
        }

        @Override
        public String toString() {
            return  node1 + " - " + node2;
        }
    }

    private List<CyEdge> collapsedEdges = new ArrayList<>();
    private List<CyEdge> expendedEdges = new ArrayList<>();


    private Map<ComparableUndirectedEdge, List<CyEdge>> edgesToCollapse = new HashMap<>();

    public <T> List<T> getColumnValuesOfEdges(String columnName, Class<? extends T> columnType, List<CyEdge> edges) {
        CyTable table = network.getDefaultEdgeTable();
        List<T> columnValues = new ArrayList<>();
        for (CyEdge edge : edges) {
            columnValues.add(table.getRow(edge.getSUID()).get(columnName, columnType, null));
        }
        return columnValues;
    }

    public void setEdgesToCollapse() {
        for (CyEdge edge : expendedEdges) {
            ComparableUndirectedEdge comparableUndirectedEdge = new ComparableUndirectedEdge(edge);
            if (edgesToCollapse.containsKey(comparableUndirectedEdge)) {
                edgesToCollapse.get(comparableUndirectedEdge).add(edge);
            } else {
                List<CyEdge> similarEdges = new ArrayList<>();
                similarEdges.add(edge);
                edgesToCollapse.put(comparableUndirectedEdge, similarEdges);
            }
        }

        for (ComparableUndirectedEdge couple : edgesToCollapse.keySet()) {
            CyEdge summaryEdge = network.addEdge(couple.node1, couple.node2, false);
            collapsedEdges.add(summaryEdge);
            edgeTable.getRow(summaryEdge.getSUID())
                    .set("summary::intact ids",
                            getColumnValuesOfEdges("intact id", String.class, edgesToCollapse.get(couple)));
        }
        collapseEdges();
    }

    private void collapseEdges() {
        insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(networkView, null, expendedEdges));
//        insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(networkView, null, collapsedEdges));
//        taskManager.execute(hideTaskFactory.createTaskIterator(networkView, null, expendedEdges));
//        taskManager.execute(unHideTaskFactory.createTaskIterator(networkView, null, collapsedEdges));
    }

    private void expendEdges() {
        insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(networkView, null, collapsedEdges));
        insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(networkView, null, expendedEdges));

//        taskManager.execute(hideTaskFactory.createTaskIterator(networkView, null, collapsedEdges));
//        taskManager.execute(unHideTaskFactory.createTaskIterator(networkView, null, expendedEdges));
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

        // Create an empty network
        edgeTable = network.getDefaultEdgeTable();
        edgeTable.createColumn("intact id", String.class, false);
        edgeTable.createListColumn("summary::intact ids", String.class, false);


        // add a node to the network
        CyNode node1 = network.addNode();
        CyNode node2 = network.addNode();
        CyEdge edge1 = network.addEdge(node1, node2, false);
        CyEdge edge2 = network.addEdge(node1, node2, false);
        CyEdge edge3 = network.addEdge(node2, node1, false);
        edgeTable.getRow(edge1.getSUID()).set("intact id", "A");
        edgeTable.getRow(edge2.getSUID()).set("intact id", "B");
        edgeTable.getRow(edge3.getSUID()).set("intact id", "C");
        expendedEdges.addAll(Arrays.asList(edge1, edge2, edge3));
        networkView.updateView();

        setEdgesToCollapse();
        System.out.println(edgesToCollapse);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
        expendEdges();

    }

    private void applyLayout() {
        CyLayoutAlgorithm alg = registrar.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
        Object context = alg.createLayoutContext();
        TunableSetter setter = registrar.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        insertTasksAfterCurrentTask(alg.createTaskIterator(networkView, context, nodeViews, null));
    }
}
