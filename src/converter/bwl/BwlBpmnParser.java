package converter.bwl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import converter.common.BpmnTask;
import converter.common.StringUtils;
import converter.common.TaskType;

public class BwlBpmnParser {

	private Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();
	private Map<String, String> sequenceMap = new HashMap<String, String>();
	private Stack<BpmnTask> startNodes = new Stack<BpmnTask>();

	public BwlBpmnParser(File inputFile, String outputFilename) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();

		startNodes = getStartIds(doc, outputFilename);
		getTasks(doc);
		getGateways(doc);
		getProcesses(doc);
		getSubProcesses(doc);
		getSequenceFlow(doc);
	}

	public Map<String, BpmnTask> getTaskMap() {
		return taskMap;
	}

	public Map<String, String> getSequenceMap() {
		return sequenceMap;
	}

	public Stack<BpmnTask> getStartIds() {
		return startNodes;
	}

	public BpmnTask getTask(String taskId) {
		return taskMap.get(taskId);
	}

	private void getTasks(Document doc) {
		NodeList nList = doc.getElementsByTagName("task");
		getTasksGeneric(nList);

		nList = doc.getElementsByTagName("businessRuleTask");
		getTasksGeneric(nList);
		
		nList = doc.getElementsByTagName("serviceTask");
		getTasksGeneric(nList);	
		
		nList = doc.getElementsByTagName("userTask");
		getTasksGeneric(nList);		
		
		nList = doc.getElementsByTagName("callActivity");
		getLinkedTasks(nList);		
	}

	private void getTasksGeneric(NodeList nList) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("\nCurrent Element :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				setTaskNodeAttributes(name, id, childNodes);
			}
		}
	}
	
	private void setTaskNodeAttributes(String name, String id, NodeList childNodes) {

		String documentation = "";
		BpmnTask task = new BpmnTask();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				if (link.getNodeName().equals("documentation")) {
					documentation = link.getTextContent();
				}
			}
		}

		task.setId(id);
		task.setType(TaskType.TASK);
		task.setName(name);
		task.setDocumentation(documentation);

		taskMap.put(id, task);
	}	
	
	private void getLinkedTasks(NodeList nList) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				setLinkedNodeAttributes(eElement);
			}
		}
	}
	
	private void setLinkedNodeAttributes(Element eElement) {

		String documentation = "";
		BpmnTask task = new BpmnTask();
		
		String name = eElement.getAttribute("name");
		String id = eElement.getAttribute("id");

		task.setId(id);
		task.setType(TaskType.METABOT);
		task.setName(name);
		task.setDocumentation(documentation);

		taskMap.put(id, task);
	}		
	
	
	private void getProcesses(Document doc) {
		NodeList nList = doc.getElementsByTagName("process");
		// System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("\nSubProcess :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				setSubProcessAttributes(name, id, childNodes);
			}
		}
	}
	
	private void getSubProcesses(Document doc) {
		NodeList nList = doc.getElementsByTagName("subProcess");
		// System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("\nSubProcess :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				setSubProcessAttributes(name, id, childNodes);
			}
		}
	}	

	private void getGateways(Document doc) {
		NodeList nList = doc.getElementsByTagName("exclusiveGateway");
		getGatewaysGeneric(nList);

		nList = doc.getElementsByTagName("inclusiveGateway");
		getGatewaysGeneric(nList);
		
		nList = doc.getElementsByTagName("parallelGateway");
		getGatewaysGeneric(nList);
		
		nList = doc.getElementsByTagName("complexGateway");
		getGatewaysGeneric(nList);		
	}

	private void getGatewaysGeneric(NodeList nList) {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			// System.out.println("\nGateway :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				setGatewayNodeAttributes(name, id);
			}
		}
	}

	private Map<String, String> getSequenceFlow(Document doc) {
		NodeList nList = doc.getElementsByTagName("sequenceFlow");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String incomingId = eElement.getAttribute("sourceRef");
				String outgoingId = eElement.getAttribute("targetRef");

				BpmnTask task = taskMap.get(outgoingId);
						    
				if (task != null) {
					System.out.println("*OutType :" + task.getType());
					task.addIncomingId(incomingId);
				}

				task = taskMap.get(incomingId);
				if (task != null) {
					System.out.println("*InType :" + task.getType());
					task.addOutgoingId(outgoingId);
				} 
			}
		}

		return sequenceMap;
	}

	private Stack<BpmnTask> getStartIds(Document doc, String outputFileName) {

		NodeList startEvents = doc.getElementsByTagName("startEvent");
		for (int i = 0; i < startEvents.getLength(); i++) {
			Node nNode = startEvents.item(i);
			System.out.println("\nstartEvent :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String id = eElement.getAttribute("id");
				
				BpmnTask startNode = taskMap.get(id);	
				if (startNode == null) {
					startNode = new BpmnTask();
					startNode.setId(id);
					startNode.setName(outputFileName); // This will be overridden if its a sub task
					startNode.setType(TaskType.START);
					
					this.taskMap.put(id, startNode);
				} else {
					System.out.println("Parent Name :" + startNode.getName());
				}
				
				startNodes.push(startNode);
			}
		}

		return startNodes;
	}
	
	private void setSubProcessAttributes(String name, String id, NodeList childNodes) {

		String subprocessName = "";
		String documentation = "";
		String startId = "";
		
		BpmnTask task = new BpmnTask();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				if (link.getNodeName().equals("startEvent")) {
					subprocessName =  StringUtils.convertToTitleCaseIteratingChars(name);
					System.out.println("subprocessName :" + subprocessName);
					
					Element eElement = (Element) link;
	
					startId = eElement.getAttribute("id");

					System.out.println("startId :" + startId);
					
					BpmnTask startNode = taskMap.get(startId);
					startNode.setName("generated\\" + subprocessName + ".txt");
				}
				else if (link.getNodeName().equals("documentation")) {
					documentation = link.getTextContent();
					// System.out.println("Documentation :" + documentation);
				}
			}
		}

		task.setId(id);
		task.setType(TaskType.SUBPROCESS);
		task.setName(name);
		task.setDocumentation(documentation);
		task.setStartId(startId);

		taskMap.put(id, task);
	}	

	private void setGatewayNodeAttributes(String name, String id) {

		BpmnTask task = new BpmnTask();

		task.setId(id);
		task.setType(TaskType.GATEWAY);
		task.setName(name);

		taskMap.put(id, task);
	}
	
	public BpmnTask getAssociatedProcess(String id) {
		Iterator<Entry<String, BpmnTask>> it = taskMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			BpmnTask task = (BpmnTask) pair.getValue();
			System.out.println(pair.getKey() + " = " + task);
		
			if (task.getStartId() != null && task.getStartId().equals(id)) {
				return task;
			}
		}
		return null;
	}
		

}
