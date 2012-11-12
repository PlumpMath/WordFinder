import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class WordFinder extends Applet implements Runnable, KeyListener{
	HashMap<String, String> wordList = new HashMap<String, String>(); //HashMap of words
	public static final int width = 440, height = 260; //Self explanatory
	private BufferedImage buffer; //Image to store graphics data, then write to screen, to avoid flickering (Double buffer)
	private Graphics2D bufferGraphics; //Double buffering graphics, created from buffer
	public HashSet<String> possible; //The possible words. Used HashSet for its efficiency and lack of duplicates
	public String finalWord, initialWord; //finalWord is where the final "best word" is stored, initial is the original input
	public boolean restart = true;
	private static int percent, time;
	private static double lastTime, currentTime, calcPerSec, calcRequired;
	
	public WordFinder()
	{
		addKeyListener(this);
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); //Make buffer image
		bufferGraphics = buffer.createGraphics(); //Create graphics element out of buffer image
		try {
		    // Set System L&F
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File f = new File("words.txt");//The word list file
		Scanner s = null;
		try {
			s = new Scanner(f); //Used to read in the words
		} catch (FileNotFoundException e) {}
		while(s.hasNext())
		{
			String word = s.nextLine().toLowerCase(); //Saves the current word
			ArrayList<Character> chars = new ArrayList<Character>();
			for(int i = 0; i < word.length(); i++)
			{
				chars.add(word.charAt(i)); //Converts to character ArrayList
			}
			Collections.sort(chars); //Sorts them (Much like a word unscrambler)
			String wordKey = ""; //The key to put in the HashMap
			for(int i = 0; i < chars.size(); i++)
			{
				wordKey += chars.get(i); //Converts ArrayList of characters to String
			}
			wordList.put(wordKey, word); //Puts the key and value in
		}
		logic(); //Runs the test logic
	}
	public static void main(String args[])
	{
		//Initializes the frame and program itself
		JFrame frame = new JFrame("Word Finder");
		WordFinder game = new WordFinder();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.black);
		frame.setLayout(new BorderLayout());
		frame.setSize(width, height);
		frame.setResizable(false);
		frame.add(game, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - height/2);
	}
	public void start()
	{
		new Thread(this).start();
	}
	public void run()
	{
		repaint();
	}
	public void logic()
	{
		finalWord = null;
		initialWord = JOptionPane.showInputDialog("Enter avaliable letters"); //Gets letters
		if(initialWord == null)
		{
			System.exit(0);
		}
		initialWord = initialWord.toLowerCase();
		possible = new HashSet<String>();
		String wordValue = findWord(initialWord); //Checks the wordList for the given word/letters
		if(wordValue != null)
		{
			possible.add(wordValue);
			finalWord = wordValue;
		}
		JFrame frame = new JFrame("Loading");
		Loading loading = new Loading();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.black);
		frame.setSize(450+5, 65+32);
		frame.add(loading, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - 505/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - 97/2);
		long num = 1; //Number that is incremented and changed to a binary string in order to try all combinations of removing characters
		calcRequired = Math.pow(2, initialWord.length());
		lastTime = 0;
		percent = 0;
		currentTime = 0;
		time = 0;
		calcPerSec = 0;
		while(num < calcRequired-1) //While num is less than the max number of 1's possible	   Ex: "abc" is 3 characters in length so 2^3-1 in binary is 111, which would remove all
		{
			if(num == 1)
			{
				lastTime = System.currentTimeMillis();
			}
			String tempWord = initialWord;
			String binary = String.format("%" + (initialWord.length()-1) + "s", ""+Long.toBinaryString(num)); //Convert to binary, pad with spaces
			int[] indexes = findOnes(binary); //Finds ones in the binary string
			for(int i = indexes.length-1; i>= 0; i--)
			{
				tempWord = removeIndex(tempWord, indexes[i]);//Removes the letters at the index of the 1's in the binary
			}
			String temp = findWord(tempWord);//Check if the word made was valid
			if(temp != null && temp.length() != 1) //If it is...
			{
				possible.add(temp);//Add it
			}
			num++; //Increment binary number
			int scale = 500;
			percent = (int)((num/calcRequired) * scale);
			int percentDebug = (int)((num/(calcRequired)) * 50);
			String astrisk = "";
			for(int i = 0; i <= percentDebug; i++)
			{
				astrisk = astrisk + "*";
			}
			astrisk = "[" + String.format("%-" + 50 + "s", astrisk) + "]";
			System.out.println(astrisk);
			if(calcRequired >= 1000)
			{
				if(num == 1000)
				{
					currentTime = System.currentTimeMillis();
					calcPerSec = currentTime - lastTime;
				}
				if(num >= 1000)
				{
					int processesLeft = (int)(calcRequired - num);
					time = (int)((processesLeft / 1000 * calcPerSec) / 10000);
				}
			}
		}
		loading.kill();
		loading = null;
		frame.dispose();
		if(finalWord == null)
		{
			Iterator<String> i = possible.iterator();
			String longest = null;
			if(i.hasNext()) //Sets longest to the first found
			{
				longest =  i.next();
			}
			while(i.hasNext()) //Finds longest
			{
				String temp = i.next();
				if(temp.length()>longest.length())
				{
					longest = temp;
				}
			}
			finalWord = longest; //Sets the word to the longest
		}
		if(finalWord == null)
		{
			finalWord = "No word possible";
		}
	}
	public static class Loading extends Applet implements Runnable
	{
		private boolean running;
		private BufferedImage buffer;
		private Graphics2D bufferGraphics;
		public Loading()
		{
			buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bufferGraphics = buffer.createGraphics();
			start();
		}
		public void start()
		{
			running = true;
			new Thread(this).start();
		}

		public void paint(Graphics g)
		{
			bufferGraphics.clearRect(0, 0, 500, 65);
			bufferGraphics.setColor(Color.green);
			bufferGraphics.fillRect(0, 0, percent, 40);
			if(calcRequired >= 1000)
				if(time >= 60)
				{
					bufferGraphics.drawString("Approx. " + (time/60) + "minutes " + (time%60) + " seconds", 180, 55);
				}
				else
				{
					bufferGraphics.drawString("Approx. " + time + " seconds", 180, 55);
				}
			g.drawImage(buffer, 0, 0, this);
		}
		public void update(Graphics g)
		{
			paint(g);
		}

		@Override
		public void run() {
			while(running)
			{
				repaint();
			}
		}
		public void kill()
		{
			running = false;
		}
	}
	//Takes in a string, sorts it and returns the word found (Can be null)
	public String findWord(String initialWord)
	{
		ArrayList<Character> chars = new ArrayList<Character>();
		for(int i = 0; i < initialWord.length(); i++)
		{
			chars.add(initialWord.charAt(i));
		}
		Collections.sort(chars);
		String wordKey = "";
		for(int i = 0; i < chars.size(); i++)
		{
			wordKey += chars.get(i);
		}
		return wordList.get(wordKey);
	}
	//Removes the character at a given index
	public String removeIndex(String s, int i)
	{
		if(i == s.length()-1)
		{
			return s.substring(0, s.length()-1);
		}
		else if(i == 0)
		{
			return s.substring(1, s.length());
		}
		else
		{
			return s.substring(0, i) + s.substring(i+1, s.length());
		}
	}
	//Returns an array of all the indexes of the a '1' in a given string
	public int[] findOnes(String s)
	{
		int length = 0;
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) == '1')
			{
				length++;
			}
		}
		int[] ones = new int[length];
		int index = 0;
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) == '1')
			{
				ones[index] = i;
				index++;
			}
		}
		return ones;
	}
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_ENTER) //Restarts
		{
			initialWord = null;
			possible = null;
			logic();
			repaint();
		}
		if(keyCode == KeyEvent.VK_P) //Shows all possible
		{
			Iterator<String> possibleIterator = possible.iterator();
			String temp = "";
			int num = 0;
			while(possibleIterator.hasNext())
			{
				temp += possibleIterator.next();
				num++;
				if(num % 12 == 0)
				{
					temp += "\n";
				}
				else
				{
					temp += " ";
				}
			}
			int i = JOptionPane.showConfirmDialog(this, "Possible words:\n" + temp + "\nTry another?");
			if(i==0) //Restarts
			{
				initialWord = null;
				possible = null;
				logic();
				repaint();
			}
			else if(i == 1)
			{
				System.exit(0);
			}
		}
		if(keyCode == KeyEvent.VK_C)
		{
			String input = "" + JOptionPane.showInputDialog("Enter required letter:");
			Iterator<String> possibleIterator = possible.iterator();
			HashSet<String> tempSet = new HashSet<String>();
			while(possibleIterator.hasNext())
			{
				String temp = possibleIterator.next();
				if(temp.contains(input))
				{
					tempSet.add(temp);
				}
			}
			if(tempSet.size() != 0)
			{
				possible = tempSet;
				possibleIterator = possible.iterator();
				String longest = "";
				if(possibleIterator.hasNext()) //Sets longest to the first found
				{
					longest =  possibleIterator.next();
				}
				while(possibleIterator.hasNext()) //Finds longest
				{
					String temp = possibleIterator.next();
					if(temp.length()>longest.length())
					{
						longest = temp;
					}
				}
				finalWord = longest;
				repaint();
			}
			else
			{
				int i = JOptionPane.showConfirmDialog(this, "Either no possible words or invalid letter.\nTry another set?");
				if(i==0) //Restarts
				{
					initialWord = null;
					possible = null;
					logic();
					repaint();
				}
				else if(i == 1)
				{
					System.exit(0);
				}
			}
		}
	}
	public void keyReleased(KeyEvent e){} //Not used
	public void keyTyped(KeyEvent e){} //Not used
	public void paint(Graphics g)
	{
		bufferGraphics.clearRect(0, 0, width, height);
		bufferGraphics.setColor(Color.white);
		bufferGraphics.drawString("Letters entered:  " + initialWord, width/2-98, 40);
		bufferGraphics.drawString("Best word:", width/2-35, 65);
		bufferGraphics.setColor(Color.green);
		bufferGraphics.drawString(finalWord, width/2-35, 95);
		bufferGraphics.setColor(Color.white);
		bufferGraphics.drawString("(Press P to view all possible words)", width/2-100, 125);
		bufferGraphics.drawString("Press C to require a certain letter", width/2-96, 165);
		bufferGraphics.setColor(Color.red);
		bufferGraphics.drawString("Press Enter to try another.", width/2-80, 200);
		g.drawImage(buffer, 0, 0, this); //Draw buffer to screen
		Toolkit.getDefaultToolkit().sync(); //Sync, I'm not sure how well this works
	}
	public void update(Graphics g)
	{
		paint(g);
	}
}