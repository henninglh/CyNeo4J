package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.sync;

import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.Neo4jRESTServer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SyncUpTaskFactory extends AbstractTaskFactory{

	private boolean wipeRemote;
	private String cypherURL;
	private CyNetwork currNet;
	private Neo4jRESTServer server;
		
	public SyncUpTaskFactory(Neo4jRESTServer neo4jRESTServer, boolean wipeRemote, String cypherURL,
							 CyNetwork currNet) {
		super();
		this.wipeRemote = wipeRemote;
		this.cypherURL = cypherURL;
		this.currNet = currNet;
		this.server = neo4jRESTServer;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SyncUpTask(server, wipeRemote,cypherURL,currNet));
	}

}
