package genEnveloping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;
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

import com.sun.org.apache.xml.internal.security.utils.Base64;

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
    //   generated signature. If not specified, standard output will be used.
    //
	
    public static void main(String[] args) throws Exception {

    	String passFileName = args[0];
    	String certFileName = args[1];
    	String inputFileName = args[2];
    	String outputFileName = args[3];
        
        // First, create the DOM XMLSignatureFactory that will be used to
        // generate the XMLSignature
        XMLSignatureFactory signatureFac = XMLSignatureFactory.getInstance("DOM");

        // Next, prepare the referenced Object (XML read from file)
        DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
        docBuilderFac.setNamespaceAware(true);
        Document xmlFileDoc = docBuilderFac.newDocumentBuilder().parse (new File(inputFileName));
        xmlFileDoc.getDocumentElement().normalize();
        
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
        FileInputStream passFileInputStream = new FileInputStream(passFileName);
        File passFile = new File(passFileName);
        byte[] passwordBytes = new byte[(int) passFile.length()];
        passFileInputStream.read(passwordBytes); 
        passFileInputStream.close();
                
        byte[] decodedPasswordBytes = Base64.decode(passwordBytes);
        String passwordStr = new String(decodedPasswordBytes, "UTF-8");        
        char[] passwordChar = passwordStr.toCharArray();

        // Create key / key info factories
        KeyInfoFactory keyInfoFac = KeyInfoFactory.getInstance("DOM");

        // Read key store from file
        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
        FileInputStream certificateFileInputStream = new FileInputStream(certFileName);
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

        // Save root element content in a string. The signing process changes the name spaces,
        // but creates the digest with the original name spaces, which causes an error validating 
        // the signature: error=12:invalid data:data and digest do not match
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter swBefore = new StringWriter();
        StreamResult resultBefore = new StreamResult(swBefore);
        DOMSource sourceXMLBefore = new DOMSource(xmlFileDoc.getDocumentElement());
        trans.transform(sourceXMLBefore, resultBefore);
        String xmlBeforeStr = swBefore.toString();
        
        int startRootBeforeIndex = xmlBeforeStr.indexOf("<" + fullRootXMLElementNameStr);
        String xmlBeforeAuxStr = xmlBeforeStr.substring(startRootBeforeIndex);       
        int endRootBeforeIndex = xmlBeforeAuxStr.indexOf(">");
        String rootXMLElementContentBeforeStr = xmlBeforeAuxStr.substring(0, endRootBeforeIndex);       
        
        // Generate the enveloping signature
        xmlSignature.sign(dsc);

        // Before writing the file, the root element name spaces are changed back to their original value.
        StringWriter swAfter = new StringWriter();
        StreamResult resultAfter = new StreamResult(swAfter);
        DOMSource sourceXMLAfter = new DOMSource(xmlFileDoc.getDocumentElement());
        trans.transform(sourceXMLAfter, resultAfter);
        String xmlAfterStr = swAfter.toString();
                
        int startRootAfterIndex = xmlAfterStr.indexOf("<" + fullRootXMLElementNameStr);
        String xmlAfterAuxStr = xmlAfterStr.substring(startRootAfterIndex);       
        int endRootAfterIndex = xmlAfterAuxStr.indexOf(">");
        String rootXMLElementContentAfterStr = xmlAfterAuxStr.substring(0, endRootAfterIndex);       
        
        xmlAfterStr = xmlAfterStr.replace(rootXMLElementContentAfterStr, rootXMLElementContentBeforeStr);

        // Write the file
        File outputFile = new File(outputFileName);
        if(outputFile.exists() && !outputFile.isDirectory()) {
        	int promptOverwrite = JOptionPane.showConfirmDialog(null, "Do you want to overwrite output file?", "File Exists", JOptionPane.YES_NO_OPTION);
            if (promptOverwrite == JOptionPane.YES_OPTION) {
            	writeOutput(outputFileName, xmlAfterStr);
            }
            else {
               JOptionPane.showMessageDialog(null, "File not written");
            }        	
        } else {
        	writeOutput(outputFileName, xmlAfterStr);
        }
    }
	
    private static void writeOutput(String outputFileName, String xmlAfterStr) throws IOException {
    	FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
    	fileOutputStream.write(xmlAfterStr.getBytes()); 
    	fileOutputStream.close();		
        JOptionPane.showMessageDialog(null, "File signed successfully");
	}
}