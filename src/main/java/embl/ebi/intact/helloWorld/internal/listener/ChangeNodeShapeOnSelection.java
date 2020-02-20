package embl.ebi.intact.helloWorld.internal.listener;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;

import java.util.Collection;

public class ChangeNodeShapeOnSelection implements RowsSetListener {

    private final CyEventHelper eventHelper;
    private final CyNetworkViewManager viewManager;
    private final CyNetworkTableManager networkTableManager;
    private static boolean ignoreSelection = false;

    public ChangeNodeShapeOnSelection(CyEventHelper eventHelper, CyNetworkViewManager viewManager, CyNetworkTableManager networkTableManager) {
        this.eventHelper = eventHelper;
        this.viewManager = viewManager;
        this.networkTableManager = networkTableManager;
    }

    @Override
    public void handleEvent(RowsSetEvent e) {
        if (!e.containsColumn(CyNetwork.SELECTED) || ignoreSelection)
            return;
        ignoreSelection = true;

        for (CyNetwork network : networkTableManager.getNetworkSet()) {
            if (network.getDefaultNodeTable() == e.getSource()) {
                for (CyNetworkView netwokView : viewManager.getNetworkViews(network)) {
                    for (RowSetRecord record : e.getColumnRecords(CyNetwork.SELECTED)) {
                        Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
                        View<CyNode> nView = netwokView.getNodeView(network.getNode(suid));
                        if ((boolean) record.getValue()) {
                            nView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.TRIANGLE);
                        } else {
                            nView.clearValueLock(BasicVisualLexicon.NODE_SHAPE);
                        }
                    }
                }
            }
        }
        eventHelper.flushPayloadEvents();
        ignoreSelection =false;
    }
}

