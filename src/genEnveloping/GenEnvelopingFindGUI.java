package genEnveloping;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

public class GenEnvelopingFindGUI {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GenEnvelopingFindGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GenEnvelopingFindGUI() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 439);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setBounds(0, 0, 582, 397);
		frame.getContentPane().add(fileChooser);

		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
	        JOptionPane.showMessageDialog(null, "Approve");
		} else if (result == JFileChooser.CANCEL_OPTION) {
	        JOptionPane.showMessageDialog(null, "Cancel");	        
		}
	}
}
