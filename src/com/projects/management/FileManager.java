package com.projects.management;

import com.projects.helper.DeviceType;
import com.projects.helper.StructureType;
import com.projects.helper.Utils;
import com.projects.models.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.System;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Dan on 5/27/2015.
 */
class FileManager
{

    public FileManager()
    {
    }

    public Template readTemplate(File file)
    {
        Template template = null;

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);

            NodeList structureNodes = doc.getElementsByTagName("structures");
            List<Structure> structures = readStructures(structureNodes);
            NodeList deviceNodes = doc.getElementsByTagName("devices");
            List<Device> devices = readDevices(deviceNodes);

            template = new Template(devices, structures);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return template;
    }

    public List<Structure> readStructures(NodeList structuresList)
    {
        List<Structure> structureList = new ArrayList<Structure>();
        Node structuresNode = structuresList.item(0);

        if (structuresNode.getNodeType() == Node.ELEMENT_NODE)
        {
            Element structuresElement = (Element)structuresNode;
            NodeList structures = structuresElement.getElementsByTagName("structure");
            int length = structures.getLength();

            for (int i = 0; i < length; ++i)
            {
                Node structureNode = structures.item(i);
                if (structureNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element structureElement = (Element)structureNode;

                    String name = getElementStringFromTag(structureElement, "name");
                    String type = getElementStringFromTag(structureElement, "type");
                    StructureType structureType;

                    if (Utils.isInEnum(type, StructureType.class))
                    {
                        structureType = StructureType.valueOf(type);
                    }
                    else
                    {
                        // TODO: should be error or something...
                        continue;
                    }

                    NodeList structureProperties = structureElement.getElementsByTagName("properties");
                    List<PropertyModel> properties = readProperties(structureProperties);

                    Structure structure = new Structure(name, structureType, properties);
                    structureList.add(structure);
                }
            }
        }

        return structureList;
    }

    private String getElementStringFromTag(Element parent, String tag)
    {
        NodeList nodeList = parent.getElementsByTagName(tag);
        Element element = (Element)nodeList.item(0);
        NodeList childNodes = element.getChildNodes();
        return childNodes.item(0).getNodeValue().trim();
    }

    public List<PropertyModel> readProperties(NodeList propertiesList)
    {
        List<PropertyModel> propertyList = new ArrayList<PropertyModel>();
        Node propertiesNode = propertiesList.item(0);

        if (propertiesNode.getNodeType() == Node.ELEMENT_NODE)
        {
            Element propertiesElement = (Element)propertiesNode;
            NodeList properties = propertiesElement.getElementsByTagName("property");
            int length = properties.getLength();

            for (int i = 0; i < length; ++i)
            {
                Node propertyNode = properties.item(i);
                if (propertyNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element propertyElement = (Element)propertyNode;

                    String name = getElementStringFromTag(propertyElement, "name");

                    NodeList nodeList = propertyElement.getElementsByTagName("value");
                    Element element = (Element)nodeList.item(0);
                    String value = element.getChildNodes().item(0).getNodeValue().trim();

                    String type = nodeList.item(0).getAttributes().getNamedItem("type").getNodeValue();
                    PropertyModel property;

                    if (type.equals("BOOLEAN"))
                    {
                        property = new PropertyModel<Boolean>(name, Boolean.valueOf(value));
                    }
                    else if (type.equals("DOUBLE"))
                    {
                        property = new PropertyModel<Double>(name, Double.valueOf(value));
                    }
                    else
                    {
                        // TODO: should be error...
                        continue;
                    }

                    propertyList.add(property);
                }
            }
        }

        return propertyList;
    }

    public List<Device> readDevices(NodeList devicesList)
    {
        List<Device> deviceList = new ArrayList<Device>();
        Node devicesNode = devicesList.item(0);

        if (devicesNode.getNodeType() == Node.ELEMENT_NODE)
        {
            Element devicesElement = (Element)devicesNode;
            NodeList devices = devicesElement.getElementsByTagName("device");
            int length = devices.getLength();

            for (int i = 0; i < length; ++i)
            {
                Node deviceNode = devices.item(i);
                if (deviceNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element deviceElement = (Element)deviceNode;

                    String name = getElementStringFromTag(deviceElement, "name");
                    String type = getElementStringFromTag(deviceElement, "type");
                    DeviceType deviceType;

                    if (Utils.isInEnum(type, DeviceType.class))
                    {
                        deviceType = DeviceType.valueOf(type);
                    }
                    else
                    {
                        // TODO: should be error or something...
                        continue;
                    }

                    NodeList propertiesList = deviceElement.getElementsByTagName("properties");
                    List<PropertyModel> properties = readProperties(propertiesList);

                    Device device = new Device(name, deviceType, properties);
                    deviceList.add(device);
                }
            }
        }

        return deviceList;
    }

    /*
    public Collection<Prefab> readPrefabFile(File file)
    {
        Collection<Prefab> prefabCollection = null;

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);

            NodeList prefabList = doc.getElementsByTagName("Prefab");
            prefabCollection = readPrefabs(prefabList);

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return prefabCollection;
    }

    private Collection<Prefab> readPrefabs(NodeList prefabList)
    {
        List<Prefab> prefabs = new ArrayList<Prefab>();

        int length = prefabList.getLength();
        for (int node = 0; node < length; ++node)
        {
            Node prefabNode = prefabList.item(node);

            if (prefabNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element prefabElement = (Element)prefabNode;

                NodeList nameList = prefabElement.getElementsByTagName("Name");
                Element nameElement = (Element)nameList.item(0);

                NodeList nameText = nameElement.getChildNodes();
                String prefabName = nameText.item(0).getNodeValue().trim();

                NodeList suffixList = prefabElement.getElementsByTagName("MemberSuffix");
                Element suffixElement = (Element)suffixList.item(0);

                NodeList suffixText = suffixElement.getChildNodes();
                String prefabMemberSuffix = suffixText.item(0).getNodeValue().trim();

                NodeList idList = prefabElement.getElementsByTagName("ID");
                Element idElement = (Element)idList.item(0);

                NodeList idText = idElement.getChildNodes();
                Integer prefabId = Integer.parseInt(idText.item(0).getNodeValue().trim());

                NodeList memberList = prefabElement.getElementsByTagName("PrefabMembers");
                Element memberElement = (Element)memberList.item(0);

                NodeList individuals = memberElement.getElementsByTagName("Individual");
                List<IndividualModel> prefabMembers = readPrefabMembers(individuals);

                Prefab newPrefab = new Prefab(prefabId, prefabName, prefabMemberSuffix, prefabMembers);
                prefabs.add(newPrefab);
            }
        }

        return prefabs;
    }


    private List<IndividualModel> readPrefabMembers(NodeList members)
    {
        List<IndividualModel> memberIndividuals = new ArrayList<IndividualModel>();

        int length = members.getLength();
        for (int node = 0; node < length; ++node)
        {
            Node memberNode = members.item(node);

            if (memberNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element memberElement = (Element)memberNode;

                NodeList idList = memberElement.getElementsByTagName("ID");
                Element idElement = (Element)idList.item(0);

                NodeList idText = idElement.getChildNodes();
                Integer memberId = Integer.parseInt(idText.item(0).getNodeValue().trim());

                NodeList nameList = memberElement.getElementsByTagName("Name");
                Element nameElement = (Element)nameList.item(0);

                NodeList nameText = nameElement.getChildNodes();
                String memberName = nameText.item(0).getNodeValue().trim();

                NodeList classList = memberElement.getElementsByTagName("ClassName");
                Element classElement = (Element)classList.item(0);

                NodeList classText = classElement.getChildNodes();
                String memberClassName = classText.item(0).getNodeValue().trim();


                NodeList descriptionList = memberElement.getElementsByTagName("Description");
                Element descriptionElement = (Element)descriptionList.item(0);

                NodeList descriptionText = descriptionElement.getChildNodes();
                String memberDescription = "";

                if (descriptionText.getLength() > 0)
                    memberDescription = descriptionText.item(0).getNodeValue();


                NodeList propertiesList = memberElement.getElementsByTagName("Properties");
                Element propertiesElement = (Element)propertiesList.item(0);

                NodeList propertyList = propertiesElement.getElementsByTagName("Property");
                List<PropertyModel> memberProperties = readMemberProperties(propertyList);

                IndividualModel member = new IndividualModel(memberId, memberName, memberClassName, memberDescription, memberProperties);
                memberIndividuals.add(member);
            }
        }

        return memberIndividuals;
    }

    private List<PropertyModel> readMemberProperties(NodeList properties)
    {
        List<PropertyModel> memberProperties = new ArrayList<PropertyModel>();

        int length = properties.getLength();
        for (int property = 0; property < length; ++property)
        {
            Node propertyNode = properties.item(property);

            if (propertyNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element propertyElement = (Element)propertyNode;

                NodeList nameList = propertyElement.getElementsByTagName("Name");
                Element nameElement = (Element)nameList.item(0);

                NodeList nameText = nameElement.getChildNodes();
                String propertyName = nameText.item(0).getNodeValue();

                NodeList valueList = propertyElement.getElementsByTagName("Value");
                Element valueElement = (Element)valueList.item(0);

                NodeList valueText = valueElement.getChildNodes();
                String value = valueText.item(0).getNodeValue().trim();

                if (value.equals("true") || value.equals("false")) // TODO: remove repetition here and in Individual Model
                {
                    PropertyModel<Boolean> newProperty = new PropertyModel<Boolean>(propertyName, Boolean.getBoolean(value));
                    memberProperties.add(newProperty);
                }
                else
                {
                    PropertyModel<Double> newProperty = new PropertyModel<Double>(propertyName, Double.parseDouble(value));
                    memberProperties.add(newProperty);
                }
            }
        }

        return memberProperties;
    }

    public void savePrefabs(File file, Collection<Prefab> prefabs)
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document doc;

        try
        {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.newDocument();

            Element mainRootElement = doc.createElementNS("www.test.com/", "Prefabs"); // TODO: use proper NS
            doc.appendChild(mainRootElement);

            for (Prefab prefab : prefabs)
            {
                mainRootElement.appendChild(getPrefabNode(doc, prefab));
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult outputFile = new StreamResult(file);

            transformer.transform(source, outputFile);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    private Node getPrefabNode(Document doc, Prefab prefab)
    {
        Element prefabNode = doc.createElement("Prefab");
        Element prefabMembers = doc.createElement("PrefabMembers");

        prefabNode.appendChild(getElement(doc, "Name", prefab.getName()));
        prefabNode.appendChild(getElement(doc, "MemberSuffix", prefab.getMemberSuffix()));
        prefabNode.appendChild(getElement(doc, "ID", String.valueOf(prefab.getId())));
        prefabNode.appendChild(prefabMembers);

        List<IndividualModel> members = prefab.getMembers();
        for (IndividualModel member : members)
        {
            prefabMembers.appendChild(getIndividualNode(doc, member));
        }

        return prefabNode;
    }

    private Node getIndividualNode(Document doc, IndividualModel individual)
    {
        Element individualNode = doc.createElement("Individual");
        Element individualProperties = doc.createElement("Properties");
        //TODO: save the jena individual or get rid of it from IndividualModel?
        individualNode.appendChild(getElement(doc, "Name", individual.getName()));
        String desc = individual.getDescription();
        if (desc == null) desc = ""; // TODO: This check should happen elsewhere
        individualNode.appendChild(getElement(doc, "Description", desc));
        individualNode.appendChild(getElement(doc, "ClassName", individual.getClassName()));
        individualNode.appendChild(getElement(doc, "ID", String.valueOf(individual.getId())));
        individualNode.appendChild(individualProperties);

        List<PropertyModel> properties = individual.getProperties();
        for (PropertyModel propertyModel : properties)
        {
            individualProperties.appendChild(getPropertyNode(doc, propertyModel));
        }

        return individualNode;
    }

    private Node getPropertyNode(Document doc, PropertyModel property)
    {
        Element propertyNode = doc.createElement("Property");

        propertyNode.appendChild(getElement(doc, "Name", property.getName()));
        propertyNode.appendChild(getElement(doc, "Value", String.valueOf(property.getValue()))); // TODO: will I have to store the type of value as an attribute?

        return propertyNode;
    }

    private Node getElement(Document doc, String elementName, String value)
    {
        Element node = doc.createElement(elementName);
        node.appendChild(doc.createTextNode(value));
        return node;
    }*/
}
