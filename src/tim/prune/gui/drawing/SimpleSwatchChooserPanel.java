package tim.prune.gui.drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimpleSwatchChooserPanel extends AbstractColorChooserPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private final List<Color> baseColors = new ArrayList<Color>();
	private final Map<JButton,Color> binding = new HashMap<JButton,Color>();
	private JButton moreButton;
	private JSlider alphaSlider;

	public SimpleSwatchChooserPanel() {
		baseColors.add(Color.PINK);
		baseColors.add(Color.RED);
		baseColors.add(Color.ORANGE);
		baseColors.add(Color.YELLOW);

		baseColors.add(Color.GREEN);
		baseColors.add(Color.BLUE);
		baseColors.add(Color.CYAN);
		baseColors.add(Color.MAGENTA);

		baseColors.add(Color.BLACK);
		baseColors.add(Color.WHITE);
	}

	@Override
	public void updateChooser() {
		Color color = getColorFromModel();
		alphaSlider.setValue((int)((color.getAlpha()/255f) * 100f));
	}

	@Override
	protected void buildChooser() {
		setLayout(new BorderLayout(5, 5));
		JPanel basePanel = new JPanel(new GridLayout(2, 5, 5, 5));
		for( Color c : baseColors ) {
			JButton button = new JButton(buildButtonIcon(c, 50, 40));
			button.setPreferredSize(new Dimension(50, 40));
			button.setBorder(BorderFactory.createEmptyBorder());
			button.setBackground(c);
			button.addActionListener(this);
			binding.put(button, c);
			basePanel.add(button);
		}

		moreButton = new JButton("More...");
		moreButton.addActionListener(this);
		
		alphaSlider = new JSlider(0, 100, 100);
		alphaSlider.setLabelTable(alphaSlider.createStandardLabels(100));
		alphaSlider.setPaintLabels(true);
		JPanel alphaPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
		alphaPanel.add(new JLabel("Opacity"));
		alphaPanel.add(alphaSlider);
		alphaSlider.addChangeListener(this);
		
		add(alphaPanel, BorderLayout.NORTH);
		add(basePanel, BorderLayout.CENTER);
		add(moreButton, BorderLayout.SOUTH);
	}

	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public Icon getSmallDisplayIcon() {
		return null;
	}

	@Override
	public Icon getLargeDisplayIcon() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == moreButton ) {
			JColorChooser chooser = new JColorChooser();
			JDialog moreDialog = JColorChooser.createDialog(this, "Select Color", true, chooser, null, null);
			chooser.getSelectionModel().addChangeListener(this);
			moreDialog.setVisible(true);
		} else {
			JButton button = (JButton) e.getSource();
			ColorSelectionModel model = getColorSelectionModel();
			Color c = binding.get(button);
			
			// if we pick a color, up the alpha a little so as not to confuse the user
			if( alphaSlider.getValue() <= 0 ) {
				alphaSlider.setValueIsAdjusting(true);
				alphaSlider.setValue(50);
				alphaSlider.setValueIsAdjusting(false);
			}
			
			model.setSelectedColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), getAlphaSliderValue()));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if( e.getSource() == alphaSlider ) {
			if( !alphaSlider.getModel().getValueIsAdjusting() ) {
				ColorSelectionModel model = getColorSelectionModel();
				int alpha = getAlphaSliderValue();
				Color c = model.getSelectedColor();
				if( c.getAlpha() != alpha ) {
					model.setSelectedColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
				}
			}
		} else {
			ColorSelectionModel model = (ColorSelectionModel) e.getSource();
			Color c = model.getSelectedColor();
			getColorSelectionModel().setSelectedColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), getAlphaSliderValue()));
		}
	}
	
	private int getAlphaSliderValue() {
		return (int)(255f * ( alphaSlider.getValue() / 100f )); 
	}
	
	private static ImageIcon buildButtonIcon( Color color, int width, int height ) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics graphics = image.getGraphics();
		graphics.setColor(color);
		graphics.fillRect(0 , 0, width, height);
		return new ImageIcon(image);
	}
}
