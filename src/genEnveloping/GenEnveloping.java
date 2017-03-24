package genEnveloping;

import javax.swing.JOptionPane;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.*;
import java.util.Collections;

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
    //   generated signature. If not specified, standard ouput will be used.
    //
	
    public static void main(String[] args) throws Exception {

        // First, create the DOM XMLSignatureFactory that will be used to
        // generate the XMLSignature
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Next, prepare the referenced Object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse (new File("object.xml"));
        doc.getDocumentElement().normalize();
        String elementName = doc.getDocumentElement().getNodeName();
        XMLStructure content = new DOMStructure(doc.getDocumentElement());
        
        // Next, create a Reference to a same-document URI that is an Object
        // element and specify the SHA256 digest algorithm
        Reference ref = fac.newReference("#" + elementName,
        		fac.newDigestMethod(DigestMethod.SHA256, null),
        		Collections.singletonList
        		(fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null)),
        		null, null);         
        
        // Next, create the referenced Object
        XMLObject obj = fac.newXMLObject
            (Collections.singletonList(content), elementName, null, null);

        // Create the SignedInfo
        SignedInfo si = fac.newSignedInfo(
            fac.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#",
               (C14NMethodParameterSpec) null),
            fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),            
            Collections.singletonList(ref));

        // Get password from base64 encoded file
        InputStream isPass = new FileInputStream("password_base64.txt"); 
        BufferedReader buf = new BufferedReader(new InputStreamReader(isPass)); 
        String passBase64 = buf.readLine(); 
        isPass.close();
        byte[] decoded = Base64.decode(passBase64);
        String pass = new String(decoded, "UTF-8");
        JOptionPane.showMessageDialog(null, "pass:" + pass);
        
        // Create a RSA KeyPair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Create a KeyValue containing the RSA PublicKey that was generated
        KeyInfoFactory kif2 = fac.getKeyInfoFactory();
        KeyValue kv = kif2.newKeyValue(kp.getPublic());

        // Create a KeyInfo and add the KeyValue to it
        KeyInfo ki = kif2.newKeyInfo(Collections.singletonList(kv));

        // Create the XMLSignature (but don't sign it yet)
        XMLSignature signature = fac.newXMLSignature(si, ki,
            Collections.singletonList(obj), null, null);

        // Create a DOMSignContext and specify the RSA PrivateKey for signing
        // and the document location of the XMLSignature
        DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), doc);

        // Lastly, generate the enveloping signature using the PrivateKey
        signature.sign(dsc);

        // output the resulting document
        OutputStream os;
        if (args.length > 0) {
           os = new FileOutputStream(args[0]);
        } else {
           os = System.out;
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(doc), new StreamResult(os));
    }
}