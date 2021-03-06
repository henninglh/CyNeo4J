package nl.maastrichtuniversity.networklibrary.cyneo4j.internal.utils;

import nl.maastrichtuniversity.networklibrary.cyneo4j.internal.extensionlogic.neo4j.Neo4jExtParam;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

import java.util.Map;

public class NeoUtils {
	public static Long extractID(String objUrl){
		return Long.valueOf(objUrl.substring(objUrl.lastIndexOf('/')+1));
	}

	public static Neo4jExtParam parseExtParameter(Map<String,Object> json){
		return new Neo4jExtParam((String)json.get("name"),
				(String)json.get("description"),
				(Boolean)json.get("optional"),
				decideParameterType((String)json.get("type")));
	}

	public static Class decideParameterType(String typeStr){
		if(typeStr.equals("string")){
			return String.class;
		} else if(typeStr.equals("integer")){
			return Integer.class;
		} else if(typeStr.equals("strings")){
			return String[].class;
		} else if(typeStr.equals("node")){
			return CyNode.class;
		} else if(typeStr.equals("relationship")){
			return CyEdge.class;
		} else {
			return null;
		}
	}
}
