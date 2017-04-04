package genEnveloping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.xml.crypto.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML-Enveloping-Signature 예제 변형
 * 
 * @source <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/xmldsig/GenEnveloping.java"/>
 * @author <a href="mailto:modesty101@daum.net">김동규</a>
 * @since 2017
 */

/**
 * This is a simple example of generating an Enveloping XML
 * Signature.
 */

public class genEnveloping {

    //
    // Synopis: java genEnveloping [output]
    //
    //   where "output" is the name of a file that will contain the
    //   generated signature. If not specified, standard ouput will be used.
    //
	
    public static void main(String[] args) throws Exception {
        
        // First, create the DOM XMLSignatureFactory that will be used to
        // generate the XMLSignature
        XMLSignatureFactory signatureFac = XMLSignatureFactory.getInstance("DOM");

        // Next, prepare the referenced Object (XML read from file)
        DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
        docBuilderFac.setNamespaceAware(true);
        Document doc = docBuilderFac.newDocumentBuilder().parse (new File(args[2]));
        doc.getDocumentElement().normalize();
        Element element = doc.getDocumentElement();        
        Document xmlFileDoc = element.getOwnerDocument();
        
        String fullRootXMLElementNameStr = xmlFileDoc.getDocumentElement().getNodeName();
        int startIndex = fullRootXMLElementNameStr.indexOf(":");
        int endIndex = fullRootXMLElementNameStr.indexOf("_");
        if (endIndex == -1) {
        	endIndex = fullRootXMLElementNameStr.length();
        }        
        String rootXMLElementNameStr = fullRootXMLElementNameStr.substring(startIndex + 1, endIndex);       
        XMLStructure xmlFileContentStructure = new DOMStructure(xmlFileDoc.getDocumentElement());
        XMLObject xmlFileObj = signatureFac.newXMLObject
                (Collections.singletonList(xmlFileContentStructure), rootXMLElementNameStr, null, null);
        
        // Next, create a Reference to a same-document URI that is an Object
        // element and specify the SHA256 digest algorithm
        Reference xmlDocumentRef = signatureFac.newReference("#" + rootXMLElementNameStr,
        		signatureFac.newDigestMethod(DigestMethod.SHA256, null),
        		Collections.singletonList
        		(signatureFac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null)),
        		null, null);                                

        // Create the SignedInfo
        SignedInfo signedInfo = signatureFac.newSignedInfo(
        	signatureFac.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#",
               (C14NMethodParameterSpec) null),
        	signatureFac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),            
            Collections.singletonList(xmlDocumentRef));

        // Get password from base64 encoded file
        byte[] passwordBytes = Files.readAllBytes(new File(args[0]).toPath());
        byte[] decodedPasswordBytes = Base64.getMimeDecoder().decode(passwordBytes);
        String passwordStr = new String(decodedPasswordBytes, "UTF-8");        
        char[] passwordChar = passwordStr.toCharArray();

        // Create key / key info factories
        KeyInfoFactory keyInfoFac = KeyInfoFactory.getInstance("DOM");

        // Read key store from file
        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
        FileInputStream certificateFileInputStream = new FileInputStream(args[1]);
        pkcs12KeyStore.load(certificateFileInputStream, passwordChar); 
        certificateFileInputStream.close();

        // Get X509 certificate from key store
        Enumeration<String> keyStoreAliasesEnum = pkcs12KeyStore.aliases();
        String alias = (String) keyStoreAliasesEnum.nextElement();
        X509Certificate cert = (X509Certificate) pkcs12KeyStore.getCertificate(alias);
        
        // Get private key from key store
        KeyStore.PrivateKeyEntry keyEntry =
                (KeyStore.PrivateKeyEntry) pkcs12KeyStore.getEntry
                    (alias, new KeyStore.PasswordProtection(passwordChar));

        // Create the KeyInfo containing the X509Data.
        List x509Content = new ArrayList();
        x509Content.add(cert.getSubjectX500Principal().getName());
        x509Content.add(cert);
        X509Data x509CertificateData = keyInfoFac.newX509Data(x509Content);
        KeyInfo keyInfo = keyInfoFac.newKeyInfo(Collections.singletonList(x509CertificateData));        

        // Create a DOMSignContext and specify the RSA PrivateKey and
        // location of the resulting XMLSignature's parent element.
        DOMSignContext dsc = new DOMSignContext
            (keyEntry.getPrivateKey(), xmlFileDoc);        
        
        // Create the XMLSignature (but don't sign it yet)
        XMLSignature xmlSignature = signatureFac.newXMLSignature(signedInfo, keyInfo,
        		Collections.singletonList(xmlFileObj), null, null);

        // Lastly, generate the enveloping signature
        xmlSignature.sign(dsc);
                
        // output the resulting document
        OutputStream os;
        if (args.length > 0) {
           os = new FileOutputStream(args[3]);
        } else {
           os = System.out;
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(xmlFileDoc), new StreamResult(os));
    }
    private static String transformXmlNodeToXmlString(Node node)
            throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(buffer));
        String xml = buffer.toString();
        return xml;
    }    
}