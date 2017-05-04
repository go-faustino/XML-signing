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
 * XML-Enveloped-Signature ¿¹Á¦ º¯Çü
 * 
 * @source <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/xmldsig/GenEnveloped.java"/>
 * @author <a href="mailto:modesty101@daum.net">±èµ¿±Ô</a>
 * @since 2017
 */

/**
 * This is a simple example of generating an Enveloped XML
 * Signature.
 */

public class GenEnveloped {

    //
    // Synopis: java genEnveloped [output]
    //
    //   where "output" is the name of a file that will contain the
    //   generated signature. If not specified, standard output will be used.
    //
	
    public static void main(String[] args) throws Exception {

    	String passFileName = args[0];
    	String certFileName = args[1];
    	String inputFileName = args[2];
    	String outputFileName = args[3];
    	Boolean includePublicKey = Boolean.valueOf(args[4]);
        
    	// Create a DOM XMLSignatureFactory that will be used to generate the
        // enveloped signature
        XMLSignatureFactory signatureFac = XMLSignatureFactory.getInstance("DOM");

        // Create list of the necessary transforms
        ArrayList transformList = new ArrayList();
        Transform envTransform = signatureFac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform exc14nTransform = signatureFac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null);
        transformList.add(envTransform);
        transformList.add(exc14nTransform); 
        
        // Create a Reference to the enveloped document (in this case we are
        // signing the whole document, so a URI of "" signifies that) and
        // also specify the SHA256 digest algorithm and the ENVELOPED Transform.
        Reference xmlDocumentRef = signatureFac.newReference
                ("", signatureFac.newDigestMethod(DigestMethod.SHA256, null),transformList,null, null);
        
        /*
        Reference xmlDocumentRef = signatureFac.newReference("",
        		signatureFac.newDigestMethod(DigestMethod.SHA256, null),
        		Collections.singletonList
        		(signatureFac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null)),
        		null, null);                                
*/
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

        // Get parameter to include public key or not (not included by default)
        KeyInfo keyInfo;

        if(includePublicKey) {
        	// Create the KeyInfo containing the public key and X509Data.        
        	KeyValue publicKeyValue = keyInfoFac.newKeyValue(cert.getPublicKey());
        
        	List x509Content = new ArrayList();       
        	x509Content.add(publicKeyValue);
        	x509Content.add(keyInfoFac.newX509Data(Collections.singletonList(cert.getSubjectX500Principal().getName())));
        	x509Content.add(keyInfoFac.newX509Data(Collections.singletonList(cert)));
        	keyInfo = keyInfoFac.newKeyInfo(x509Content);
        } else {
        	// Create the KeyInfo containing the X509Data.
        	List x509Content = new ArrayList();
        	x509Content.add(cert.getSubjectX500Principal().getName());
        	x509Content.add(cert);
        	X509Data x509CertificateData = keyInfoFac.newX509Data(x509Content);
        	keyInfo = keyInfoFac.newKeyInfo(Collections.singletonList(x509CertificateData));
        }
        
        // Create a DOMSignContext and specify the RSA PrivateKey and
        // location of the resulting XMLSignature's parent element.
        DOMSignContext dsc = new DOMSignContext
            (keyEntry.getPrivateKey(), xmlFileDoc.getDocumentElement());        
        
		// Create the XMLSignature (but don't sign it yet)
        XMLSignature xmlSignature = signatureFac.newXMLSignature(signedInfo, keyInfo);
       
        // Generate the enveloped signature
        xmlSignature.sign(dsc);

        // Transform file to string
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter xmlStringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(xmlStringWriter);
        DOMSource sourceXML = new DOMSource(xmlFileDoc.getDocumentElement());
        trans.transform(sourceXML, streamResult);
        String xmlString = xmlStringWriter.toString();

        // Write the file
        File outputFile = new File(outputFileName);
        if(outputFile.exists() && !outputFile.isDirectory()) {
        	int promptOverwrite = JOptionPane.showConfirmDialog(null, "Do you want to overwrite output file?", "File Exists", JOptionPane.YES_NO_OPTION);
            if (promptOverwrite == JOptionPane.YES_OPTION) {
            	writeOutput(outputFileName, xmlString);
            }
            else {
               JOptionPane.showMessageDialog(null, "File not written");
            }        	
        } else {
        	writeOutput(outputFileName, xmlString);
        }
    }
	
    private static void writeOutput(String outputFileName, String xmlAfterStr) throws IOException {
    	FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
    	fileOutputStream.write(xmlAfterStr.getBytes()); 
    	fileOutputStream.close();		
        JOptionPane.showMessageDialog(null, "File signed successfully");
	}
}
