package converter.wdg.ibm.com;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class CodeConverterBak {
	
	private static Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();	
	private static Map<String, List<String>> generatedCode = new HashMap<String, List<String>>();
	
	private static String startId = "";
	
	public static void main(String[] args) {

		try {
			File inputFile = new File("test.bpmn");
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			// Get the BPMN Start Id
			startId = getStartId(doc);
			
			getTasks(doc);
			getGateways(doc);
			
			String taskId = startId;
			
			generateWDGCode(taskId, "root");
			
			writeWDGFile(generatedCode);
						
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateWDGCode(String taskId, String parentName) throws IOException {
		
		BpmnTask task = taskMap.get(taskId);
		System.out.println(task);

		if (task != null) {
			switch (task.getType()) {
			case TASK:
				generateTaskCode(task, parentName);
				break;
			case GATEWAY:
				generateGatewayCode(task, parentName);
				break;
			default:
				// code block
			}
			
		}
	}

	private static void generateTaskCode(BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.name);

		String gosubStr = "goSub --label " + name;
		addCode(parentName, gosubStr);

	    generateWDGCode(task.getOutgoingId(0), name);
		
	}

	private static void addCode(String parentName, String gosubStr) {
		List<String> functionCode = generatedCode.get(parentName);
		
		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(gosubStr);	
	}
	
	private static void generateGatewayCode(BpmnTask task, String parentName) throws IOException {
	
		String ifStr = "if --left \"${result}\" --operator \"Is_True\"";
		addCode(parentName, ifStr);
		
		BpmnTask taskA = taskMap.get(task.getOutgoingId(0));
		System.out.println("A: " + taskA);
		generateGosubCode(taskA, parentName);
		
		String elseStr = "else";
		addCode(parentName, elseStr);
		
		BpmnTask taskB = taskMap.get(task.getOutgoingId(1));
		System.out.println("B: " + taskB);
		generateGosubCode(taskB, parentName);	

		String endifStr = "endif";
		addCode(parentName, endifStr);

	}

	private static void generateGosubCode(BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		
		String gosubStr = "goSub --label " + name;
		addCode(parentName, gosubStr);
		
		generateWDGCode(task.getOutgoingId(0), parentName);
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
	

	private static String getStartId(Document doc) {
		
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
				if ( link.getNodeName().equals("incoming") ) {
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
	
	private static String getOutgoingId(NodeList childNodes) {
		
		String outgoingId = "";
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node link = childNodes.item(i);
							
			if (link.getNodeType() == Node.ELEMENT_NODE) {
				Element linkNode = (Element) link;
				String linkId = linkNode.getTextContent();
				if ( link.getNodeName().equals("outgoing") ) {
				   System.out.println("Outgoing Id :" + linkId);
				   outgoingId = linkId;
			    }
			}
		}
		
		return outgoingId;
	}


	public static void writeWDGFile(Map<String, List<String>> generatedCode) throws IOException {
		String filename = "generatedBotCode.txt";
		FileOutputStream outputStream = new FileOutputStream(filename);
		
		StringBuilder strBuilder = new StringBuilder();

		/*
        String code1 = Character.toString((char)18);	
        String code2 = Character.toString((char)38 );
        String code3 = Character.toString((char)35 );
        String code4 = Character.toString((char)54 );
        String code5 = Character.toString((char)53 );
        String code6 = Character.toString((char)53 );
        String code7 = Character.toString((char)51 );
        String code8 = Character.toString( (char)51);
        String code9 = Character.toString( (char)59);
        String code10 = Character.toString((char)2);       
        strBuilder.append(code1 + code2 + code3 + code4 + code5 + code6 + code7 + code8 + code9 + code10);
        */
		
		strBuilder.append("defVar --name result --type Boolean --value True");
		strBuilder.append('\n');
		
		for (String key : generatedCode.keySet()) {
			System.out.println("Key: " + key + ", Value: " + generatedCode.get(key));

			List<String> functionCode = generatedCode.get(key);

			strBuilder.append('\n');
			strBuilder.append("// " + key + '\n');
			
			for (String codeLine : functionCode) {
				strBuilder.append(codeLine);
				strBuilder.append('\n');
			}

			strBuilder.append('\n');
		}
	    
		
        // String code11 = Character.toString((char)8);
		// strBuilder.append('*' + code11 + "20.5.2.0");
		
		byte[] strToBytes = strBuilder.toString().getBytes();
		outputStream.write(strToBytes);
		

		outputStream.close();
	}
}
