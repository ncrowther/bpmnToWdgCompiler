package converter.wdg.ibm.com;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BpmnParser {

	private static Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();
	private static Map<String, String> conditionMap = new HashMap<String, String>();
	
	public static Map<String, BpmnTask> getNodes(Document doc) {
		getTasks(doc);
		getGateways(doc);	
		return taskMap;
	}

	private static void getTasks(Document doc) {
		NodeList nList = doc.getElementsByTagName("task");
		System.out.println("----------------------------");

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

	private static void getGateways(Document doc) {
		NodeList nList = doc.getElementsByTagName("exclusiveGateway");
		System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("\nGateway :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				getNodeAttributes(TaskType.GATEWAY, name, id, childNodes);
			}
		}
	}
	
	public static Map<String, String> getSequenceFlow(Document doc) {
		NodeList nList = doc.getElementsByTagName("sequenceFlow");
		System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("SequenceFlow :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String name = eElement.getAttribute("name");
				String id = eElement.getAttribute("id");

				System.out.println("Name :" + name);
				System.out.println("Id :" + id);

				NodeList childNodes = eElement.getChildNodes();
				getSequenceAttributes(name, id, childNodes);
			}
		}
		
		return conditionMap;
	}

	public static String getStartId(Document doc) {

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

				NodeList childNodes = eElement.getChildNodes();
				startId = getOutgoingId(childNodes);
			}
		}

		return startId;
	}

	private static void getNodeAttributes(TaskType type, String name, String id, NodeList childNodes) {

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
				} else {
					System.out.println("Outgoing Id :" + linkId);
					outgoingId = linkId;
					task.addOutgoingId(outgoingId);
				}
			}
		}

		task.id = id;
		task.type = type;
		task.name = name;
		task.incomingId = incomingId;

		taskMap.put(incomingId, task);
	}
	
	private static void getSequenceAttributes(String name, String id, NodeList childNodes) {

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String conditionExpression = linkNode.getTextContent();
				System.out.println("Condition :" + conditionExpression);
				if (conditionExpression.contains("true")) {
					conditionMap.put(id, "");
				}
			}
		}
	}

	private static String getOutgoingId(NodeList childNodes) {

		String outgoingId = "";

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);

			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String linkId = linkNode.getTextContent();
				if (link.getNodeName().equals("outgoing")) {
					System.out.println("Outgoing Id :" + linkId);
					outgoingId = linkId;
				}
			}
		}

		return outgoingId;
	}
}
