package converter.wdg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import converter.common.BpmnTask;
import converter.common.TaskType;

public class WdgBpmnParser {

	private Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();
	private Map<String, String> sequenceMap = new HashMap<String, String>();
	private String startId;

	public WdgBpmnParser(File inputFile) throws SAXException, IOException, ParserConfigurationException {
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();
		
		getTasks(doc);
		getGateways(doc);
		getSequenceFlow(doc);
		startId = getStartId(doc);
	}
	
	public Map<String, BpmnTask> getTaskMap() {
		return taskMap;
	}

	public Map<String, String> getSequenceMap() {
		return sequenceMap;
	}
	
	public String getStartId() {
		return startId;
	}
	
	public BpmnTask getTask(String taskId) {
		return taskMap.get(taskId);
	}

	private void getTasks(Document doc) {
		NodeList nList = doc.getElementsByTagName("task");
		// System.out.println("----------------------------");

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
				getNodeAttributes(TaskType.TASK, name, id, childNodes);
			}
		}
	}

	private void getGateways(Document doc) {
		NodeList nList = doc.getElementsByTagName("exclusiveGateway");
		// System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			// System.out.println("\nGateway :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				// System.out.println("Name :" + name);
				// System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				getNodeAttributes(TaskType.GATEWAY, name, id, childNodes);
			}
		}
	}
	
	private Map<String, String> getSequenceFlow(Document doc) {
		NodeList nList = doc.getElementsByTagName("sequenceFlow");
		// System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				// System.out.println("Name :" + name);
				// System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				getSequenceAttributes(name, id, childNodes);
			}
		}
		
		return sequenceMap;
	}

	private String getStartId(Document doc) {

		String startId = "";
		NodeList startEvents = doc.getElementsByTagName("startEvent");
		for (int i = 0; i < startEvents.getLength(); i++) {
			Node nNode = startEvents.item(i);
			// System.out.println("\nstartEvent :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				// System.out.println("Name :" + name);
				// System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				startId = getOutgoingId(childNodes);
			}
		}

		return startId;
	}

	private void getNodeAttributes(TaskType type, String name, String id, NodeList childNodes) {

		String incomingId = "";
		String outgoingId = "";
		BpmnTask task = new BpmnTask();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String linkId = linkNode.getTextContent();
				if (link.getNodeName().equals("incoming")) {
					System.out.println("Incoming Id :" + linkId);
					incomingId = linkId;
					task.addIncomingId(incomingId);
					taskMap.put(incomingId, task);
				} else {
					// System.out.println("Outgoing Id :" + linkId);
					outgoingId = linkId;
					task.addOutgoingId(outgoingId);
				}
			}
		}
		
		task.setId(id);
		task.setType(type);
		task.setName(name);

	}
	
	private void getSequenceAttributes(String name, String id, NodeList childNodes) {

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String conditionExpression = linkNode.getTextContent();
				// System.out.println("Condition :" + conditionExpression);
				if (conditionExpression.contains("true")) {
					sequenceMap.put(id, "");
				}
			}
		}
	}

	private String getOutgoingId(NodeList childNodes) {

		String outgoingId = "";

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String linkId = linkNode.getTextContent();
				if (link.getNodeName().equals("outgoing")) {
					// System.out.println("Outgoing Id :" + linkId);
					outgoingId = linkId;
				}
			}
		}

		return outgoingId;
	}
}
