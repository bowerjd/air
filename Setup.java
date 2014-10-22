import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Setup {

    public static void main(String[] args) throws Exception {
        File file = new File(System.getProperty("user.home") + "/.m2/settings.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        Node settings = document.getElementsByTagName("settings").item(0);
        servers(document, settings);
        profiles(document, settings);
        activeProfiles(document, settings);

        StringWriter output = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(output));

        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(output.toString().getBytes("UTF-8"));
        fos.close();
    }

    public static void servers(Document document, Node settings) {
        Node servers = hasChild(settings, "servers");
        if (servers == null) {
            servers = settings.appendChild(document.createElement("servers"));
        }

        Node server = find(servers, "id", "ossrh");
        if (server == null) {
            server = servers.appendChild(document.createElement("server"));
        }
        removeChildren(server);
        Node id = server.appendChild(document.createElement("id"));
        Node username = server.appendChild(document.createElement("username"));
        Node password = server.appendChild(document.createElement("password"));
        id.setTextContent("ossrh");
        username.setTextContent(System.getenv("OSSRH_USER"));
        password.setTextContent(System.getenv("OSSRH_PASSWORD"));
    }

    public static void profiles(Document document, Node settings) {
        Node profiles = hasChild(settings, "profiles");
        if (profiles == null) {
            profiles = settings.appendChild(document.createElement("profiles"));
        }

        Node profile = find(profiles, "id", "gpg");
        if (profile == null) {
            profile = profiles.appendChild(document.createElement("profile"));
        }
        removeChildren(profile);
        Node id = profile.appendChild(document.createElement("id"));
        id.setTextContent("gpg");
        Node properties = profile.appendChild(document.createElement("properties"));
        Node passphrase = properties.appendChild(document.createElement("gpg.passphrase"));
        Node publicKeyring = properties.appendChild(document.createElement("gpg.publicKeyring"));
        Node secretKeyring = properties.appendChild(document.createElement("gpg.secretKeyring"));
        passphrase.setTextContent(System.getenv("OSSRH_PASSWORD"));
        publicKeyring.setTextContent(System.getenv("TRAVIS_BUILD_DIR") + "/public.gpg");
        secretKeyring.setTextContent(System.getenv("TRAVIS_BUILD_DIR") + "/private.gpg");
    }

    public static void activeProfiles(Document document, Node settings) {
        Node activeProfiles = hasChild(settings, "activeProfiles");
        if (activeProfiles == null) {
            activeProfiles = settings.appendChild(document.createElement("activeProfiles"));
        }

        if (hasChildValue(activeProfiles, "gpg") == null) {
            Node activeProfile = activeProfiles.appendChild(document.createElement("activeProfile"));
            activeProfile.setTextContent("gpg");
        }
    }

    public static Node find(Node node, String name, String value) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            NodeList values = child.getChildNodes();
            for (int x = 0; x < values.getLength(); x++) {
                if (child.getNodeName().equals(name) && child.getNodeValue().equals(values)) {
                    return child;
                }
            }
        }

        return null;
    }

    public static Node hasChild(Node node, String name) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(name)) {
                return child;
            }
        }

        return null;
    }

    public static Node hasChildValue(Node node, String value) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getTextContent().equals(value)) {
                return child;
            }
        }

        return null;
    }

    public static void removeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

}
