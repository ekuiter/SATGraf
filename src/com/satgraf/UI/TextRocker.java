package com.satgraf.UI;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;

public class TextRocker extends JPanel {
	
	private BasicArrowButton leftArrow = new BasicArrowButton(BasicArrowButton.WEST);
	private BasicArrowButton rightArrow = new BasicArrowButton(BasicArrowButton.EAST);
	private JTextField textField = new JTextField("0");
	private int id;
	private ArrayList<TextRockerListener> listeners = new ArrayList<TextRockerListener>();
	private int lastUpdate;
	
	public TextRocker(int id, String title, int initialValue) {
		this.id = id;
		this.setLayout(new GridLayout(2, 1));
		
		// Title
		JLabel titleLabel = new JLabel(title);
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		this.add(titleLabel);
		
		// Rocker
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		textField.setPreferredSize(new Dimension(60, 30));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setText(String.valueOf(initialValue));
		
		panel.add(leftArrow);
		panel.add(textField);
		panel.add(rightArrow);
		this.add(panel);
		
		leftArrow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int value = Integer.parseInt(textField.getText()) - 1;
				textField.setText(String.valueOf(value));
			}
		});
		
		rightArrow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int value = Integer.parseInt(textField.getText()) + 1;
				textField.setText(String.valueOf(value));
			}
		});
		
		textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				update();
			}
		});
	}
	
	public int getId() {
		return this.id;
	}
	
    public void registerListener(TextRockerListener listener) {
    	listeners.add(listener);
    }

    public void removeListener(TextRockerListener listener) {
    	listeners.remove(listener);
    }

    public void update() {
       String text = textField.getText();
       if (text == null || text.isEmpty())
    	   return;
    	   
       int value = Integer.parseInt(text);
       
       for (TextRockerListener ob : listeners) {
          ob.stateChanged(this.id, value);
       }
    }
}
