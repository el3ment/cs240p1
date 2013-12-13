package client.framework;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import client.AppState;

// Handy functions for the app
public class AppUtilities {
	
	// Returns a JLabel with a photo in it - used by getSampleImage
	public static JLabel createImageLabel(String fragment, int width, int height){
		
		String url = urlFromFragment(fragment);
		
		try{
			BufferedImage image = ImageIO.read(new URL(url));
			Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
			
			return picLabel;
		}catch(Exception e){ 
			// noop
		}
		
		return null;
	}
	
	// Appends the current server hostname and port to the partial urls
	public static String urlFromFragment(String fragment){
		if(fragment.contains("http://") || fragment.contains("https://"))
			return fragment;
		else
			return "http://" + AppState.get().get("hostname") + ":" + AppState.get().get("port") + "/" + fragment;
	}
	
}
