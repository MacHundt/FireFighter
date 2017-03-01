package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableColumn;

public class PreView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable detail;
	private JTable table;

	/**
	 * Create the panel.
	 */
	public PreView() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel northSelection = new JPanel();
		add(northSelection, BorderLayout.NORTH);
		northSelection.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblNewLabel = new JLabel("get Top X");
		northSelection.add(lblNewLabel);
		
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
		northSelection.add(spinner);
		
		JButton resultBtn = new JButton(" >>>");
		northSelection.add(resultBtn);
		
		JSplitPane split = new JSplitPane();
		add(split, BorderLayout.CENTER);
		
		JPanel left = new JPanel();
		split.setLeftComponent(left);
		detail = new JTable();
		left.add(detail);
		
		
		JPanel right = new JPanel();
		split.setRightComponent(right);
		
		table = new JTable();
		right.add(table);
		
		
		TableColumn col1 = new TableColumn();
		col1.setHeaderValue("Type");
		
		
		TableColumn col_r1 = new TableColumn();
		col1.setHeaderValue("Type");
		
	}
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Preview");
		frame.setExtendedState(frame.MAXIMIZED_BOTH);
		frame.getContentPane().add(new PreView());
		
		frame.setVisible(true);
	}

}
