package converter.bwl;

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

public class BwlBpmnParser {

	private Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();
	private Map<String, String> sequenceMap = new HashMap<String, String>();
	private String startId;

	public BwlBpmnParser(File inputFile) throws SAXException, IOException, ParserConfigurationException {

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
				setTaskNodeAttributes(name, id, childNodes);
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

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				setGatewayNodeAttributes(name, id);
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

				String id = eElement.getAttribute("id");
				String incomingId = eElement.getAttribute("sourceRef");
				String outgoingId = eElement.getAttribute("targetRef");

				System.out.println("Id :" + id);
				System.out.println("incomingId :" + incomingId);
				System.out.println("outgoingId :" + outgoingId);

				BpmnTask task = taskMap.get(outgoingId);
				if (task != null) {
					task.addIncomingId(incomingId);
				}

				task = taskMap.get(incomingId);
				if (task != null) {
					task.addOutgoingId(outgoingId);
				} else {
					task = new BpmnTask();
					task.setType(TaskType.START);
					task.addOutgoingId(outgoingId);
					taskMap.put(incomingId, task);
				}
			}
		}

		return sequenceMap;
	}

	private String getStartId(Document doc) {

		String startId = "";
		NodeList startEvents = doc.getElementsByTagName("startEvent");
		for (int i = 0; i < startEvents.getLength(); i++) {
			Node nNode = startEvents.item(i);
			System.out.println("\nstartEvent :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				startId = id;
			}
		}

		return startId;
	}

	private void setTaskNodeAttributes(String name, String id, NodeList childNodes) {

		String documentation = "";
		BpmnTask task = new BpmnTask();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				if (link.getNodeName().equals("documentation")) {
					documentation = link.getTextContent();
					System.out.println("Documentation :" + documentation);
				}
			}
		}

		task.setId(id);
		task.setType(TaskType.TASK);
		task.setName(name);
		task.setDocumentation(documentation);

		taskMap.put(id, task);
	}

	private void setGatewayNodeAttributes(String name, String id) {

		BpmnTask task = new BpmnTask();

		task.setId(id);
		task.setType(TaskType.GATEWAY);
		task.setName(name);

		taskMap.put(id, task);
	}
}