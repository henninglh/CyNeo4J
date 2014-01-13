package nl.maastrichtuniversity.networklibrary.CyNetLibSync.internal;

import org.cytoscape.application.CyApplicationManager;
import org.osgi.framework.BundleContext;

public class Plugin{
	private CyApplicationManager cyApplicationManager;
	private BundleContext context;
	
	private Neo4jConnectionHandler connHandler;

	public Plugin(CyApplicationManager cyApplicationManager, BundleContext context, CyActivator cyActivator) {
		super();
		this.cyApplicationManager = cyApplicationManager;
		this.context = context;
		
	}
	
	public Neo4jConnectionHandler getNeo4jConnectionHandler() {
		if(connHandler == null)
			connHandler = new Neo4jConnectionHandler();
		
		return connHandler;
	}
	
	
	public BundleContext getContext() {
		return context;
	}

	public CyApplicationManager getCyApplicationManager() {
		return cyApplicationManager;
	}


}
