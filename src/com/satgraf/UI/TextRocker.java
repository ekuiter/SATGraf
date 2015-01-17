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
import javax.swing.SwingUtilities;
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
	private int min, max, prevValue;
	private DocumentListener documentListener;
	
	public TextRocker(int id, String title, int initialValue, int min, int max) {
		this.id = id;
		this.setLayout(new GridLayout(2, 1));
		this.min = min;
		this.max = max;
		this.prevValue = initialValue;
		
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
				
				if (value >= TextRocker.this.min)
					textField.setText(String.valueOf(value));
			}
		});
		
		rightArrow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int value = Integer.parseInt(textField.getText()) + 1;
				
				if (value <= TextRocker.this.max)
					textField.setText(String.valueOf(value));
			}
		});
		
		documentListener = new DocumentListener() {
			
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
		};
		
		textField.getDocument().addDocumentListener(documentListener);
	}
	
	public int getId() {
		return this.id;
	}
	
    public void registerListener(TextRockerListener listener) {
    	synchronized (listeners) {
    		listeners.add(listener);
		}
    }

    public void removeListener(TextRockerListener listener) {
    	synchronized (listeners) {
    		listeners.remove(listener);
		}
    }

    public void update() {
       String text = textField.getText();
       if (text == null || text.isEmpty())
    	   return;
       
       try {
	       int value = Integer.parseInt(text);
	       
	       if (value < this.min || value > this.max) {
	    	   revertValue();
	       } else {
	    	   this.prevValue = value;
		       
	    	   synchronized (listeners) {
	    		   for (TextRockerListener ob : listeners) {
	 		          ob.stateChanged(this.id, value);
	 		       }
	    	   }
	       }
       } catch (NumberFormatException e) {
    	   revertValue();
       }
    }
    
    private void revertValue() {
    	Runnable doRevert = new Runnable() {
			
			@Override
			public void run() {
				textField.setText(String.valueOf(prevValue));
			}
		};
    	
		SwingUtilities.invokeLater(doRevert);
    }
    
    public void setValue(int value) {
       	this.prevValue = value;
       	textField.getDocument().removeDocumentListener(documentListener);
       	textField.setText(String.valueOf(value));
       	textField.getDocument().addDocumentListener(documentListener);
    }
}
