package ro.bogdani.simplefilebackup.components;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class JMultistateCheckBox extends JCheckBox {
	
	private enum CheckBoxState {
		SELECTED, PARTIAL_SELECTED, NOT_SELECTED
	}
	
	private CheckBoxState state = CheckBoxState.NOT_SELECTED;
	
	private static Icon selected = new ImageIcon(
			JMultistateCheckBox.class.getResource("/selected.png"));
	private static Icon unselected = new ImageIcon(
			JMultistateCheckBox.class.getResource("/unselected.png"));
	private static Icon partialSelected = new ImageIcon(
			JMultistateCheckBox.class.getResource("/partialselected.png"));

	@Override
	public void paint(Graphics g) {
		setBackground(Color.WHITE);
		switch (state) {
		case NOT_SELECTED:			
			setIcon(unselected);
			break;
		case SELECTED:
			setIcon(selected);
			break;	
		case PARTIAL_SELECTED:
			setIcon(partialSelected);
			break;			
		default:
			break;
		}
		super.paint(g);
	}

	public boolean isPartialSelected() {
		return state.equals(CheckBoxState.PARTIAL_SELECTED);
	}

	public void setPartialSelected() {
		state = CheckBoxState.PARTIAL_SELECTED;
		repaint();
	}
	
	@Override
	public void setSelected(boolean isSelected) {		
		if (isSelected) {
			state = CheckBoxState.SELECTED;
		} else {
			state = CheckBoxState.NOT_SELECTED;
		}
		repaint();
	}
}