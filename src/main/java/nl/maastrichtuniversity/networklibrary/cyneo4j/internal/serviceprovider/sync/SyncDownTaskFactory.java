package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.sync;

import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.Neo4jRESTServer;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SyncDownTaskFactory extends AbstractTaskFactory {

	private Neo4jRESTServer server;
	private CyNetworkManager cyNetworkMgr;
	private boolean mergeInCurrent;
	private CyNetworkFactory cyNetworkFactory;
	private String instanceLocation;
	private String cypherURL;
	private CyNetworkViewManager cyNetworkViewMgr;
	private CyNetworkViewFactory cyNetworkViewFactory;
	private CyLayoutAlgorithmManager cyLayoutAlgorithmMgr;
	private VisualMappingManager visualMappingMgr;

	public SyncDownTaskFactory(Neo4jRESTServer neo4jRESTServer, CyNetworkManager cyNetworkMgr,
							   boolean mergeInCurrent, CyNetworkFactory cyNetworkFactory,
							   String instanceLocation, String cypherURL,
							   CyNetworkViewManager cyNetworkViewMgr,
							   CyNetworkViewFactory cyNetworkViewFactory,
							   CyLayoutAlgorithmManager cyLayoutAlgorithmMgr,
							   VisualMappingManager visualMappingMgr) {
		super();
		this.cyNetworkMgr = cyNetworkMgr;
		this.mergeInCurrent = mergeInCurrent;
		this.cyNetworkFactory = cyNetworkFactory;
		this.instanceLocation = instanceLocation;
		this.cypherURL = cypherURL;
		this.cyNetworkViewMgr = cyNetworkViewMgr;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyLayoutAlgorithmMgr = cyLayoutAlgorithmMgr;
		this.visualMappingMgr = visualMappingMgr;
		this.server = neo4jRESTServer;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SyncDownTask(server, mergeInCurrent, cypherURL, instanceLocation, cyNetworkFactory, cyNetworkMgr,cyNetworkViewMgr,cyNetworkViewFactory,cyLayoutAlgorithmMgr,visualMappingMgr));
	}

}
