package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider;

import com.sun.istack.internal.NotNull;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.Plugin;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.Extension;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.ExtensionCall;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.ExtensionParameter;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.neo4j.Neo4jExtParam;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.neo4j.Neo4jExtension;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.extension.ExtensionLocationsHandler;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.extension.ExtensionParametersResponseHandler;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.extension.PassThroughResponseHandler;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.general.Neo4jPingHandler;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.sync.SyncDownTaskFactory;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider.sync.SyncUpTaskFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Neo4jRESTServer implements Neo4jServer {

	private static final String DATA_URL = "/db/data/";
	private static final String CYPHER_URL = DATA_URL + "cypher";
	private static final String EXT_URL = DATA_URL + "ext/";

	protected String instanceLocation = null;
	protected String auth64EncodedInfo = null;

	private Plugin plugin;
	private Map<String,AbstractCyAction> localExtensions;

	protected ExecutorService threadpool;
	protected Async async;

	public Neo4jRESTServer(Plugin plugin){
		this.plugin = plugin;

	}

	@Override
	public boolean connect(String instanceLocation, String username, String password) {
		boolean connectionSuccess;

		if(instanceLocation != null){
			disconnect();
		}

		setInstanceLocation(instanceLocation);
		connectionSuccess = validConnection();
		auth64EncodedInfo = createAuth64EncodedInfo(username, password);
		System.out.println(auth64EncodedInfo);

		if(connectionSuccess){
			registerExtension();
		} else {
			disconnect(); // cleanup
		}

		return connectionSuccess;
	}

	protected void registerExtension() {
		for(Extension ext : getExtensions()){
			getPlugin().registerAction(localExtensions.get(ext.getName()));
		}
	}

	@Override
	public void disconnect() {
		instanceLocation = null;
		auth64EncodedInfo = null;
		unregisterExtensions();
	}

	private void unregisterExtensions() {
		getPlugin().unregisterActions();
	}

	@Override
	public boolean isConnected() {
		return validConnection();
	}

	@Override
	public String getInstanceLocation() {
		return instanceLocation;
	}

	protected void setInstanceLocation(String instanceLocation) {
		this.instanceLocation = instanceLocation;
	}

	protected String createAuth64EncodedInfo(@NotNull String username, @NotNull String password) {
		if (!username.equals("") && !password.equals("")) {
			return Base64.encodeBase64String((username + ":" + password).getBytes());
		}
		return null;
	}

	@Override
	public void syncDown(boolean mergeInCurrent) {

		TaskIterator it = new SyncDownTaskFactory(this, getPlugin().getCyNetworkManager(),
				mergeInCurrent,
				getPlugin().getCyNetworkFactory(),
				getInstanceLocation(),
				getCypherURL(),
				getPlugin().getCyNetViewMgr(),
				getPlugin().getCyNetworkViewFactory(),
				getPlugin().getCyLayoutAlgorithmManager(),
				getPlugin().getVisualMappingManager()
				).createTaskIterator();

		plugin.getDialogTaskManager().execute(it);
	}

	@Override
	public List<Extension> getExtensions() {
		List<Extension> res = new ArrayList<Extension>();

		Extension cypherExt = new Neo4jExtension();
		cypherExt.setName("cypher");
		cypherExt.setEndpoint(getCypherURL());

		List<ExtensionParameter> params = new ArrayList<ExtensionParameter>();

		ExtensionParameter queryParam = new Neo4jExtParam("cypherQuery", "Cypher Endpoint", false,String.class);
		params.add(queryParam);

		cypherExt.setParameters(params);

		if(localExtensions.containsKey("cypher")){
			res.add(cypherExt);
		}
		try {
			Set<String> extNames = Request.Get(getInstanceLocation() + EXT_URL)
					.addHeader("Authorization", auth64EncodedInfo)
					.execute().handleResponse(new ExtensionLocationsHandler());

			for(String extName : extNames){
				List<Extension> serverSupportedExt = Request.Get(getInstanceLocation() + EXT_URL + extName)
						.addHeader("Authorization", auth64EncodedInfo)
						.execute().handleResponse(new ExtensionParametersResponseHandler(getInstanceLocation() +
								EXT_URL + extName));

				for(Extension ext : serverSupportedExt){
					if(localExtensions.containsKey(ext.getName())){
						res.add(ext);
					}
				}
			}

		} catch (ClientProtocolException e) {
			System.err.println("Error: ClientProtocol exception in Neo4jRESTServer.getExtensions()");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error: IOException exception in Neo4jRESTServer.getExtensions()");
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public void syncUp(boolean wipeRemote, CyNetwork curr) {
		TaskIterator it = new SyncUpTaskFactory(this, wipeRemote,getCypherURL(),getPlugin().getCyApplicationManager().getCurrentNetwork()).createTaskIterator();
		plugin.getDialogTaskManager().execute(it);

	}

	private String getCypherURL() {
		return getInstanceLocation() + CYPHER_URL;
	}

	protected void setupAsync(){
		threadpool = Executors.newFixedThreadPool(2);
		async = Async.newInstance().use(threadpool);
	}

	@Override
	public Object executeExtensionCall(ExtensionCall call, boolean doAsync) {
		Object retVal = null;

		if(doAsync){
			setupAsync();

//			System.out.println("executing call: " + call.getUrlFragment());
//			System.out.println("using payload: " + call.getPayload());
			String url = call.getUrlFragment();
			Request req = Request.Post(url)
					.addHeader("Authorization", auth64EncodedInfo)
					.bodyString(call.getPayload(), ContentType.APPLICATION_JSON);

			async.execute(req);
		} else {


			try {
//				System.out.println("executing call: " + call.getUrlFragment());
//				System.out.println("using payload: " + call.getPayload());
				String url = call.getUrlFragment();
				retVal = Request.Post(url)
						.addHeader("Authorization", auth64EncodedInfo)
						.bodyString(call.getPayload(), ContentType.APPLICATION_JSON)
						.execute().handleResponse(new PassThroughResponseHandler());

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return retVal;
	}

	@Override
	public boolean validConnection() {
		try {
			return instanceLocation != null && Request.Get(instanceLocation)
					.addHeader("Authorization", auth64EncodedInfo)
					.execute().handleResponse(new Neo4jPingHandler());
		} catch (ClientProtocolException e) {
			System.err.println("ClientProtocolException in Neo4jRESTServer.validConnection()");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException in Neo4jRESTServer.validConnection()");
			e.printStackTrace();
		}
		return false;
	}

	protected Plugin getPlugin() {
		return plugin;
	}

	@Override
	public Extension supportsExtension(String name) {
		List<Extension> extensions = getExtensions();

		for(Extension extension : extensions){
			if(extension.getName().equals(name)){
				return extension;
			}
		}

		return null;
	}

	@Override
	public void setLocalSupportedExtension(Map<String,AbstractCyAction> localExtensions) {
		this.localExtensions = localExtensions;
	}

	@Override
	public String getBasic64AuthInfo() {
		return auth64EncodedInfo;
	}
}
