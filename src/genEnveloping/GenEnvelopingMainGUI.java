package genEnveloping;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;

public class GenEnvelopingMainGUI {

	private JFrame frmXmlSigningFacility;
	private JTextField passFileTextField;
	private JTextField certFileTextField;
	private JTextField inputFileTextField;
	private JTextField outputFileTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GenEnvelopingMainGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GenEnvelopingMainGUI() {
		frmXmlSigningFacility = new JFrame();
		frmXmlSigningFacility.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	quitPrompt();
		    }
		});		
		CreateGUI();
	}

	public void quitPrompt() {
    	int promptQuit = JOptionPane.showConfirmDialog(null, "Are you sure?", "Quit application", JOptionPane.YES_NO_OPTION);		 
        if (promptQuit == JOptionPane.YES_OPTION)
			System.exit(0);
	}
	
	public void CreateGUI() {
		frmXmlSigningFacility.setTitle("XML signing");
		frmXmlSigningFacility.setBounds(100, 100, 640, 339);
		frmXmlSigningFacility.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmXmlSigningFacility.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("GenEnveloping");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	quitPrompt();
			}
		});
		mnNewMenu.add(mntmQuit);
		frmXmlSigningFacility.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Password file (base64)");
		lblNewLabel.setBounds(10, 11, 310, 14);
		frmXmlSigningFacility.getContentPane().add(lblNewLabel);
		
		final String appPath = System.getProperty("user.dir");
				
		passFileTextField = new JTextField();
		passFileTextField.setBounds(10, 36, 505, 20);
		frmXmlSigningFacility.getContentPane().add(passFileTextField);
		passFileTextField.setColumns(1000);
		passFileTextField.setText(appPath + "\\password_base64.txt");
		
		JLabel lblCertificatepkcs = new JLabel("Certificate file (PKCS12)");
		lblCertificatepkcs.setBounds(10, 67, 310, 14);
		frmXmlSigningFacility.getContentPane().add(lblCertificatepkcs);
		
		certFileTextField = new JTextField();
		certFileTextField.setColumns(1000);
		certFileTextField.setBounds(10, 92, 505, 20);
		certFileTextField.setText(appPath + "\\certificate.p12");
		frmXmlSigningFacility.getContentPane().add(certFileTextField);
		
		JLabel lblInputFile = new JLabel("Unsigned input file (XML)");
		lblInputFile.setBounds(10, 123, 310, 14);
		frmXmlSigningFacility.getContentPane().add(lblInputFile);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setColumns(1000);
		inputFileTextField.setBounds(10, 148, 505, 20);
		inputFileTextField.setText(appPath + "\\unsigned.xml");
		frmXmlSigningFacility.getContentPane().add(inputFileTextField);
		
		JLabel lblSignedOutputFile = new JLabel("Signed output file destination (XML)");
		lblSignedOutputFile.setBounds(10, 179, 310, 14);
		frmXmlSigningFacility.getContentPane().add(lblSignedOutputFile);
		
		outputFileTextField = new JTextField();
		outputFileTextField.setColumns(1000);
		outputFileTextField.setBounds(10, 204, 505, 20);
		outputFileTextField.setText(appPath + "\\signed.xml");
		frmXmlSigningFacility.getContentPane().add(outputFileTextField);

		JButton getPassFileButton = new JButton("Find...");
		getPassFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(passFileTextField.getText());
				fileChooser.setBounds(0, 0, 582, 397);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					passFileTextField.setText(fileChooser.getSelectedFile().toString());
				}
			}
		});
		getPassFileButton.setBounds(525, 35, 89, 23);
		frmXmlSigningFacility.getContentPane().add(getPassFileButton);
		
		JButton getCertFileButton = new JButton("Find...");
		getCertFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(certFileTextField.getText());
				fileChooser.setBounds(0, 0, 582, 397);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					certFileTextField.setText(fileChooser.getSelectedFile().toString());
				}
			}
		});
		getCertFileButton.setBounds(525, 91, 89, 23);
		frmXmlSigningFacility.getContentPane().add(getCertFileButton);
		
		JButton getInputFileButton = new JButton("Find...");
		getInputFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(inputFileTextField.getText());
				fileChooser.setBounds(0, 0, 582, 397);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					inputFileTextField.setText(fileChooser.getSelectedFile().toString());
				}
			}
		});
		getInputFileButton.setBounds(525, 147, 89, 23);
		frmXmlSigningFacility.getContentPane().add(getInputFileButton);
		
		JButton getOutputFileButton = new JButton("Find...");
		getOutputFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(outputFileTextField.getText());
				fileChooser.setBounds(0, 0, 582, 397);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					outputFileTextField.setText(fileChooser.getSelectedFile().toString());
				}
			}
		});
		getOutputFileButton.setBounds(525, 203, 89, 23);
		frmXmlSigningFacility.getContentPane().add(getOutputFileButton);
		
		JButton btnSign = new JButton("Sign file");
		btnSign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] genEnvelopingArgs = new String[4];
				genEnvelopingArgs[0] = passFileTextField.getText();
				genEnvelopingArgs[1] = certFileTextField.getText();
				genEnvelopingArgs[2] = inputFileTextField.getText();
				genEnvelopingArgs[3] = outputFileTextField.getText();

				try {
					genEnveloping.main(genEnvelopingArgs);
				} catch (Exception e2) {
					e2.printStackTrace();
			        JOptionPane.showMessageDialog(null, "Error: " + e2);
				}
				
			}
		});
		btnSign.setBounds(525, 246, 89, 23);
		frmXmlSigningFacility.getContentPane().add(btnSign);
		frmXmlSigningFacility.setVisible(true);
	}
}
