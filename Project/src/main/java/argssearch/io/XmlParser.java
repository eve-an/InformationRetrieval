package argssearch.io;

import argssearch.shared.query.Topic;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlParser {

    /**
     * Parse the Touche xml corpus and extract the query from it.
     *
     * @param path path to xml file
     * @return query
     */
    public static List<Topic> from(final String path) {
        Document doc = toDocument(path);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("topic");
        if (nodeList.getLength() == 0) {
            throw new RuntimeException("Expected at least one topic");
        }

        List<Topic> topicList = new LinkedList<>();
        for (int nodeNum = 0; nodeNum < nodeList.getLength(); nodeNum++) {
            Node node = nodeList.item(nodeNum);   // topic body as node
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                throw new RuntimeException("Expected an Element-Node.");
            }

            String number = getSingleNode(node, "number").getTextContent().trim();

            if (!number.matches("\\d+")) {
                throw new RuntimeException("Could not parse number of Topic!");
            }

            String title = getSingleNode(node, "title").getTextContent().trim();
            String description = getSingleNode(node, "description").getTextContent().trim();
            String narrative = getSingleNode(node, "narrative").getTextContent().trim();

            topicList.add(new Topic(Integer.parseInt(number), title, description, narrative));
        }

        return topicList;
    }

    private static Node getSingleNode(Node node, String tagName) {
        Element element = (Element) node; // topic body as element

        NodeList titleList = element.getElementsByTagName(tagName);
        if (titleList.getLength() != 1) {
            throw new RuntimeException("Expected 1 " + tagName + " but " + titleList.getLength() + " are present.");
        }

        return titleList.item(0);
    }


    private static Document toDocument(final String path) {
        final File input = new File(path);
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(input);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e.getMessage()); // Parsing is essential for this program so exit when fail
        }
    }

    // TODO: Should return something like qid Q0 doc rank score tag
    public void to() {
        // return new Result...
    }
}
