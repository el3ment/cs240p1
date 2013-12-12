package client.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import client.AppState;
import client.GUI;
import client.controller.BatchController;
import client.framework.ActiveBatch;
import client.framework.AppUtilities;
import client.framework.GlobalEventManager;

// Image window panel has one giant image that can pan and zoom
// as well as highlight cells
@SuppressWarnings("serial")
public class ImageWindowPanel extends JPanel implements ActionListener, MouseWheelListener, MouseMotionListener, MouseListener{
	
	// Invert image filter
	private static final RescaleOp INVERT_FILTER = new RescaleOp(-1F, 255F, null);
	
	// Highlight color
	private static final Color LIGHT_TEAL = new Color(0.0F, 0.4F, 1.0F, 0.3F);
	
	ImageWindowPanel _panel = this;
	Point _dragStartPosition;
	Point _dragStartOrigin;
	ActiveBatch _model;
	JButton _submitButton = new JButton("Submit");
	Image _image = new Image();
	
	public ImageWindowPanel(){
		super();
		
		// Set up layout
		this.setLayout(new BorderLayout());
		
		// Build the bar of buttons
		this.add(_buildMenuBar(), BorderLayout.NORTH);
		
		// Add the main image component
		this.add(_image, BorderLayout.CENTER);
		
		this.setPreferredSize(new Dimension(700, 300));
		
		// Populate the data from the model
		refresh();
		
		// Set up Global Event Listeners
		GlobalEventManager.getInstance().addListener(this);
	}
	
	private JPanel _buildMenuBar(){
		
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		// Zoom In Button
		JButton zoomInButton = new JButton("Zoom In");
		zoomInButton.setActionCommand("zoomIn");
		zoomInButton.addActionListener(this);
		bar.add(zoomInButton);
		
		// Zoom Out Button
		JButton zoomOutButton = new JButton("Zoom Out");
		zoomOutButton.setActionCommand("zoomOut");
		zoomOutButton.addActionListener(this);
		bar.add(zoomOutButton);
		
		// Invert Image Button
		JButton invertImageButton = new JButton("Invert Image");
		invertImageButton.setActionCommand("invertImage");
		invertImageButton.addActionListener(this);
		bar.add(invertImageButton);
		
		// Toggle Highlight Button
		JButton toggleHighlightButton = new JButton("Toggle Highlights");
		toggleHighlightButton.setActionCommand("toggleHighlights");
		toggleHighlightButton.addActionListener(this);
		bar.add(toggleHighlightButton);
		
		// Save Settings and Batch Entry
		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand("saveSettings");
		saveButton.addActionListener(this);
		bar.add(saveButton);
		
		// Submit Finished Batch
		_submitButton.setActionCommand("submitForm");
		_submitButton.addActionListener(this);
		bar.add(_submitButton);
		
		// Set up mouse events
		
		
		return bar;
	}

	public void refresh(){
		_model = (ActiveBatch) AppState.get().get("activeBatch");
		
		if(_model != null){
			_image.setSource(AppUtilities.urlFromFragment(_model.imageUrl));
			_submitButton.setEnabled(true);
		}else{
			_image.clearSource();
			_submitButton.setEnabled(false);
		}
	}
	
	public class Image extends JComponent{
		
		BufferedImage _image;
		double _scale = 0.3;
		double _x = 0;
		double _y = 0;
		boolean _isInverted = false;
		
		Boolean _showHighlights;
		int _highlightX;
		int _highlightY;
		int _highlightWidth;
		int _highlightHeight;
		int _highlightedCellX = 0;
		int _highlightedCellY = 0;
		
		public Image(){
			super();
			this.setBackground(Color.BLACK);
			this.setSize(new Dimension(100, 100));
			
			this.addMouseWheelListener(_panel);
			this.addMouseMotionListener(_panel);
			this.addMouseListener(_panel);
		} 
		
		public boolean hasImage(){
			return _image != null;
		}
	   
	    protected void paintComponent(Graphics g){
	    	if(_image != null){
		        Graphics2D graphics2d = (Graphics2D) g.create();
		        int componentWidth = getWidth();
		        int componentHeight = getHeight();
		        graphics2d.setColor(Color.GRAY);
		        graphics2d.fillRect(0, 0, componentWidth, componentHeight);
		        //if(b != dr.c) {
	                
		        	graphics2d.translate(componentWidth / 2, componentHeight / 2);
		        	graphics2d.scale(_scale, _scale);
		        	graphics2d.translate(-_x, -_y);
		           
		            int imageXOffset = -_image.getWidth() / 2;
		            int imageYOffset = -_image.getHeight() / 2;
		            BufferedImage bufferedimage = _image;
		           
		            if(_isInverted)
		                bufferedimage = INVERT_FILTER.filter(_image, null);
		            
		            graphics2d.drawImage(bufferedimage, imageXOffset, imageYOffset, this);
		            
		            // Highlights
		            if(_showHighlights != null && _showHighlights) {
		                graphics2d.setColor(LIGHT_TEAL);
		                graphics2d.fillRect(_highlightX + imageXOffset, _highlightY + imageYOffset, _highlightWidth, _highlightHeight);
		            }
		            
		           
				//}
	    	}
	    	
	    	
	    	
	    } 
	    
		public void translate(Point startOrigin, Point startLocation, int x, int y){
			
			// Begin an empty transform to inverseTransform the points we need
			// after accounting for scale and centering transformations
			AffineTransform transform = new AffineTransform();
			transform.scale(_scale, _scale);
			transform.translate(-startOrigin.x, -startOrigin.y);
			transform.translate(this.getWidth()/2, this.getHeight()/2);
			
			// We need these points 'corrected' for scale and transformations
			// so we inverseTrasnform them
			Point2D currentPosition_transformed = new Point2D.Double();
			Point2D startLocation_transformed = new Point2D.Double();
			
			try{
				transform.inverseTransform(new Point2D.Double(x, y), currentPosition_transformed);
				transform.inverseTransform(new Point2D.Double(startLocation.x, startLocation.y), startLocation_transformed);
			}catch (NoninvertibleTransformException ex) {
				return;
			}
			
			// Helps us keep the _x and _y setting lines clean
			int currentX = (int) currentPosition_transformed.getX();
			int currentY = (int) currentPosition_transformed.getY();
			int startX = (int) startLocation_transformed.getX();
			int startY = (int) startLocation_transformed.getY();
			
			int w_deltaX = currentX - startX;
			int w_deltaY = currentY - startY;
			
			// Set the new current origin
			_x = startOrigin.x - w_deltaX;
			_y = startOrigin.y - w_deltaY;
			
			
			this.repaint();
			
		}
		
		private Point convert(int x, int y){
	       
			int newX = x - getWidth() / 2;
	        int newY = y - getHeight() / 2;
	       
	        newX = (int) ((double) newX / _scale);
	        newY = (int) ((double) newY / _scale);
	        newX += _x;
	        newY += _y;
	        newX += _image.getWidth() / 2;
	        newY += _image.getHeight() / 2;
	        
	        return new Point(newX, newY);
		}
		
		public void highlightCellAt(int x, int y){
			
			Point p = this.convert(x, y);
			
			int xCorrected = (int) p.x;
			int yCorrected = (int) p.y;
			int row = 0;
			int column = 0;
			
			for(int i = 0; i < _model.getRowCount(); i++){
				if(yCorrected > (i * _model.recordHeight + _model.firstYCoord))
					row = i;
			}
			
			int currentXCoord = 0;
			for(int i = 0; i < _model.getColumnCount(); i++){
				currentXCoord = _model.getXCoord(i);
				if(xCorrected > currentXCoord)
					column = i;
			}
			
			this.highlightCell(row, column);
			
		}
	   
	    public void setScale(double scale)  
	    {  
	        _scale = scale;
	        AppState.get().put("imageScale", _scale);
	        repaint();  
	    }
		
	    public void setInvert(boolean isInverted){
	    	_isInverted = isInverted;
	    	AppState.get().put("imageInverted", isInverted);
	    	repaint();
	    }
	    
	    public void setHighlight(boolean isHighlighted){
	    	_showHighlights = isHighlighted;
	    	AppState.get().put("imageHighlighted", isHighlighted);
	    	this.repaint();
	    }
	    
		public void setSource(String url){
			
			try {
				_image = ImageIO.read(new URL(url));
				this.repaint();
			} catch (IOException e) {
				GUI.globalErrorMessage("There was an error loading the image");
			}
		}
		
		
		public void clearSource(){
			_image = null;
			this.repaint();
		}
		
		public void invert(){
			this.setInvert(!_isInverted);
		}
		
		public void zoomIn(){
			this.setScale(_scale * 1.5);
		}
		
		public void zoomOut(){
			this.setScale(_scale * .75);
		}
		
		public void toggleHighlights(){
			this.setHighlight(!_showHighlights);
		}
		
		public Point getOrigin(){
			return new Point((int) _x, (int) _y);
		}
		
		public void highlightCell(int row, int column){
			
			// These should be UNTRASNFORMED!
			_highlightY = _model.firstYCoord + row * _model.recordHeight;
			_highlightHeight = _model.recordHeight;
			_highlightX = _model.getXCoord(column);
			_highlightWidth = _model.getFieldWidth(column);
			
			_showHighlights = (_showHighlights == null ? true : _showHighlights);
			
			_model.setActiveCell(row, column);
			
			this.repaint();
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
	    switch(action){
		    case "zoomIn":
		    	_image.zoomIn();
		    	break;
		    case "zoomOut":
		    	_image.zoomOut();
		    	break;
		    case "invertImage":
		    	_image.invert();
		    	break;
		    case "toggleHighlights":
		    	_image.toggleHighlights();
		    	break;
		    case "saveSettings":
		    	AppState.get().save();
		    	break;
		    case "submitForm":
		    	(new BatchController()).submitBatch((ActiveBatch) AppState.get().get("activeBatch"));
		    	break;
	    }
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if(notches < 0)
			_image.zoomIn();
		else
			_image.zoomOut();
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {	
		_image.translate(_dragStartOrigin, _dragStartPosition, e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		_dragStartPosition = null;
		_dragStartOrigin = null;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e) && _image.hasImage()){
			_image.highlightCellAt(e.getX(), e.getY());
		}

	}

	
	// Unused mouse event listeners
	@Override
	public void mousePressed(MouseEvent e) {
		if(_dragStartPosition == null){
			_dragStartPosition = e.getPoint();
			_dragStartOrigin = _image.getOrigin();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }
	
	@Override
	public void mouseMoved(MouseEvent e) { }
	
	public void onImageScaleChanged(AppState appState, Double scale){
		_image.setScale(scale);
	}
	
	public void onImageInvertedChanged(AppState appState, Boolean isInverted){
		_image.setInvert(isInverted);
	}
	
	public void onImageHighlightedChanged(AppState appState, Boolean isHighlighted){
		_image.setHighlight(isHighlighted);
	}
	
	public void onActiveBatchChanged(AppState appState, ActiveBatch activeBatch){
		refresh();
	}
	public void onActiveBatchChanged(AppState appState){
		// Active Batch Nullified
		refresh();
	}
	
	// Respond to cell selection
	public void onCellSelected(ActiveBatch activeBatch, ActiveBatch.SelectedCell activeCell){
		if(_image != null && _image.hasImage()){
			_image.highlightCell(activeCell.row, activeCell.column);
		}
	}
	
}
