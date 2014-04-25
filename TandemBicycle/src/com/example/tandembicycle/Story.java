package com.example.tandembicycle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Story {
	private JSONObject rootNode;
	private JSONObject currentNode;
	private String currentName;
	private JSONArray choices;
	
	public Story (InputStream is, String startNode) {
		try{
	
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		String bufferString = new String(buffer);
		
		rootNode = new JSONObject(bufferString);
		currentName = "None";
		
		goToNode(startNode);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public String getPrompt()
	{
		try{
			return currentNode.getString("text");
		} catch (JSONException e) {
			return "";
		}
	}
	
	public String getImageName()
	{
		try{
			return currentNode.getString("image");
		} catch (JSONException e) {
			return "";
		}
	}
	
	public String getMusicName()
	{
		try{
			return currentNode.getString("music");
		} catch (JSONException e) {
			return "";
		}
	}
	
	public List<String> getChoices(){
		try{
			List<String> list = new ArrayList<String>();
			
			int i;
			for (i = 0; i < choices.length(); i++) {
				JSONObject choice = (JSONObject)choices.get(i);
				list.add(choice.getString("text"));
			}
			
			return list;
			
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
	}
	
	public String getNodeName() {
		return currentName;
	}
	
	public void goToNode (String node) {
		try{
			
			/* Go to a random node */
			if (node.startsWith("RandomNode")) {
				JSONArray names = rootNode.names();
				Random r = new Random();
				int index = r.nextInt(names.length());
				
				System.out.println("Random node number "+index);
				
				node = names.getString(index);
			}
			
			if (node.startsWith("Resuscitate?")) {
				Random r = new Random();
				if(r.nextBoolean()){
					node = "ResuscitateSuccess";
				} else {
					node = "ResuscitateFailure";
				}
			}
			
			/* If we receive an invalid node name, go to "startNode" in "Default" */
			if (!rootNode.has(node)) {
				JSONObject defaultNode = rootNode.getJSONObject("Default");
				node = defaultNode.getString ("startNode"); 
			}
			
			JSONObject newNode = rootNode.getJSONObject(node);
			if (newNode != null) {
				currentName = node;
				currentNode = newNode;
			}
			
			/* Pull choices from "Default" if we have no choices */
			if (currentNode.has("choices")) {
				choices = currentNode.getJSONArray("choices");
				if(choices.length() == 0) {
					JSONObject defaultNode = rootNode.getJSONObject("Default");
					choices = defaultNode.getJSONArray("choices");
				}
			} else {
				JSONObject defaultNode = rootNode.getJSONObject("Default");
				choices = defaultNode.getJSONArray("choices");
			}
		
		} catch (JSONException e) {
			
		}
	}
	
	public void choose (int index) {
		try {
			JSONObject choice = (JSONObject)choices.get(index);
			
			/* If our choice doesn't have a node identifier, we go to the start node by feeding a "null" node name */
			if (choice.has("node")) goToNode(choice.getString("node"));
			else goToNode("null");
			
		} catch (JSONException e) {
			
		}
	}
}