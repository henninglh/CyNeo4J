package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.serviceprovider;

import java.util.List;
import java.util.Map;

import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.Extension;
import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.ExtensionCall;

import org.apache.http.client.fluent.Executor;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

public interface Neo4jServer {

	// general house keeping
	boolean connect(String instanceLocation, String username, String password);
	boolean	validateConnection();
	void disconnect();
	boolean isConnected();
	String getInstanceLocation();
	String getUsername();
	String getPassword();
	Executor getExecutor();

	// full sync interface
	void syncUp(boolean wipeRemote, CyNetwork curr);
	void syncDown(boolean mergeInCurrent);

	// extension interface
	void setLocalSupportedExtension(Map<String,AbstractCyAction> localExtensions);
	List<Extension> getExtensions();
	Extension supportsExtension(String name);
	Object executeExtensionCall(ExtensionCall call, boolean async);
}
