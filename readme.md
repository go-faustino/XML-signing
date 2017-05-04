XML Signing
===============

XML signing utility, in JAVA.

Includes a GUI class (GenEnvelopingMainGUI), an enveloping type signature class (GenEnveloping) and an enveloped type signature class (GenEnveloped).

To execute the GUI class under Windows, run the included sign.bat.

The GUI has the option to include the private key in the signature's key info, and to generate an enveloping or enveloped signature.

You can call the classes directly, passing the necessary parameters.

The following input files are needed:
- Base64 encoded certificate password file;
- Certificate in PKCS12 format;
- XML file for signing.