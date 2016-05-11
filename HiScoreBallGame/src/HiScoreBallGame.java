import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.event.*;

/**
   BALL GAME
   @author ERICA KLEMPNER
   I WROTE IT.
*/
public class HiScoreBallGame
{
    private static int highScore = 0;
    private static BoardPanel winningLeaf = null;
    
    public static void main(String[] args)
    {
	BoardFrame theBoardFrame = new BoardFrame();
	theBoardFrame.show();
	BoardPanel bp = theBoardFrame.getTheBoardPanel(); // a straight copy
	findHiScore(bp);
	printHiScorePath(winningLeaf);
    }

    // a DFS method for finding the highest score - bottoms out
    // completely to the end of the game, so:
    // 6x4 boards are fine
    // 8x6 boards take ... (still running...)
    public static void findHiScore(BoardPanel bp)
    {
	for (int i = 0; i < BoardPanel.WIDTH; i++)
	    {
		for (int j = 0; j < BoardPanel.HEIGHT; j++)
		    {
			SquarePanel current = (bp.getSquaresArr())[i][j];
			Color c = current.getBallColor();
			if (!(current.getSeenBefore()) &&
			    BoardPanel.checkNeighbors(bp, i, j, c))
			    {
				BoardPanel childbp =
				    new BoardPanel(bp, bp, i, j);
				boolean gameOver =
				    BoardPanel.deleteGroup(bp, childbp, i, j);
				if (gameOver)
				    {
					int newScore = childbp.getScore();
					if (newScore > highScore)
					    {
						highScore = newScore;
						winningLeaf = childbp;
					    }
				    }
				else { findHiScore(childbp); }
			    }
		    }
	    }
    }

    // because of the recursion will print out in the correct order
    public static void printHiScorePath(BoardPanel bp)
    {
	BoardPanel parent = bp.getBpParent();
	if (parent != null) printHiScorePath(parent);
	    
	int i = bp.getDeletedi();
	int j = bp.getDeletedj();
	if (i != -1 && j != -1)
	    System.out.println("Delete group connected to "
			   + i + ", " + j + ".");
	    
    }
}

/**
   The frame that holds everything.
*/
class BoardFrame extends JFrame
{
    /**
       The width of the frame.
    */
    public static final int BOARDWIDTH = BoardPanel.WIDTH * 50;
    /**
       The height of the frame.
     */
    public static final int BOARDHEIGHT = BoardPanel.HEIGHT * 50 + 20;

    private BoardPanel theBoardPanel;
    
    public BoardFrame()
    {
	setTitle("BALL GAME");
	setSize(BOARDWIDTH, BOARDHEIGHT);

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	theBoardPanel = new BoardPanel();
	theBoardPanel.setBackground(Color.black);
	Container contentPane = getContentPane();
	contentPane.add(theBoardPanel);
    }

    public BoardPanel getTheBoardPanel()
    {
	return (new BoardPanel(theBoardPanel, null, -1, -1));
    }
}

/** The panel that holds everything.
 */

class BoardPanel extends JPanel
{
    /**
       The number of balls across.
    */
    public static final int WIDTH = 8;
    /**
       The number of balls down.
    */
    public static final int HEIGHT = 6;

    private JPanel scorePanel = new JPanel();
    private JTextField scoreDisplay = new JTextField("SCORE: 0", 21);
    
    private JPanel panel;

    // squaresArr holds the smaller panels (one per ball) that make up theBoardPanel.
    private SquarePanel[][] squaresArr = new SquarePanel[WIDTH][HEIGHT];

    private int score = 0;

    // numberDeleted is used to calculate the score at each mouseclick; it is reset
    // to zero each time.
    private int numberDeleted = 0;

    // numberRemaining keeps track of how many balls have been deleted overall; the 
    // showScore method uses this to see if the player gets the 1000-point bonus for
    // deleting all the balls.
    private int numberRemaining = WIDTH * HEIGHT;

    // the parent board configuration in the search tree
    private BoardPanel parent = null;

    // the across position of a ball in the connected group that is
    // deleted in the parent board to produce the current board
    private int iDeletedInParent = -1;

    // the analogous down position
    private int jDeletedInParent = -1;
    
    /** theBoardPanel's constructor.  As the GridLayout creates and distributes the 150
	squarePanels, they are also put into a 2-Dimensional array, squaresArr, which can
	then be used to access them.  squaresArr has WIDTH slots across and HEIGHT down.
    */
    public BoardPanel()
    {
	setLayout(new BorderLayout());
	panel = new JPanel(new GridLayout(HEIGHT, WIDTH));

	for (int j = 0; j < HEIGHT; j++)
	    {
		for (int i = 0; i < WIDTH; i++)
		    {
			SquarePanel squarePanel = new SquarePanel(this, i, j);
			squaresArr[i][j] = squarePanel;
			panel.add(squarePanel);
		    }
	    }
	add(panel, BorderLayout.CENTER);
	scoreDisplay.setEditable(false);
	scoreDisplay.setBackground(Color.orange);
	scorePanel.setBackground(Color.black);
        scorePanel.add(scoreDisplay);
	add(scorePanel, BorderLayout.NORTH);
    }

    // a constructor for copying board positions; also
    // keeps track of the parent board
    public BoardPanel(BoardPanel board, BoardPanel parentBoard,
		      int iDIP, int jDIP)
    {
	for (int j = 0; j < HEIGHT; j++)
	    {
		for (int i = 0; i < WIDTH; i++)
		    {
			Color c = (board.squaresArr[i][j]).getBallColor();
			SquarePanel squarePanel = new SquarePanel(this, i, j, c);
			this.squaresArr[i][j] = squarePanel;
		    }
	    }
	this.score = board.score;
	this.numberRemaining = board.numberRemaining;
	this.parent = parentBoard;
	this.iDeletedInParent = iDIP;
	this.jDeletedInParent = jDIP;
    }

    public SquarePanel[][] getSquaresArr() {return squaresArr; } // NOT a copy
    public int getScore() { return score; }
    public int getDeletedi() { return iDeletedInParent; }
    public int getDeletedj() { return jDeletedInParent; }
    public BoardPanel getBpParent() { return parent; }
    
    public void paintComponent(Graphics g)
    {
	super.paintComponent(g);
    }

    /**
       Checks to see if a particular squarePanel has a neighbor of the same color.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
       @return true if there is a same-color neighbor; false otherwise.
    */
    public static boolean checkNeighbors(BoardPanel bp, int i, int j, Color c)
    {
	if ((c != Color.black) &&
	    ((((i-1) >= 0) && bp.squaresArr[i-1][j].getBallColor() == c) ||
	     (((i+1) < WIDTH) && bp.squaresArr[i+1][j].getBallColor() == c) ||
	     (((j-1) >= 0) && bp.squaresArr[i][j-1].getBallColor() == c) ||
	     (((j+1) < HEIGHT) && bp.squaresArr[i][j+1].getBallColor() == c)))
	    return true;
	else return false;
    }

    /**
       If the mouse has entered a squarePanel that has at least one same-color neighbor,
       the same-color group will be highlighted.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
    */
    public static void highlightGroup(BoardPanel bp, int i, int j)
    {
	Color c = bp.squaresArr[i][j].getBallColor();

	if (checkNeighbors(bp, i, j, c)) highlightRecurse(bp, i, j, c);
    }
    
    /**
       Does the highlighting work.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
       @param mouseAtColor the squarePanel's color.
    */
    public static void highlightRecurse(BoardPanel bp, int i, int j, Color mouseAtColor)
    {
	if (i >= 0 && i <= (WIDTH - 1) && j >= 0 && j <= (HEIGHT - 1))
	    {
		SquarePanel currentSquare = bp.squaresArr[i][j];

		if ((currentSquare.getBallColor().equals(mouseAtColor)) &&
		    (currentSquare.getCurrentColor() != Color.white))
		    {
			currentSquare.setCurrentColor(Color.white);
			currentSquare.repaint();
			highlightRecurse(bp, i, j-1, mouseAtColor);
			highlightRecurse(bp, i, j+1, mouseAtColor);
			highlightRecurse(bp, i-1, j, mouseAtColor);
			highlightRecurse(bp, i+1, j, mouseAtColor);
		    }
	    }
    }

    /**
       Unhighlights when the mouse leaves a squarePanel.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
    */
    public static void unhighlightGroup(BoardPanel bp, int i, int j)
    {
	if (i >= 0 && i <= (WIDTH - 1) && j >= 0 && j <= (HEIGHT - 1))
	    {
		SquarePanel currentSquare = bp.squaresArr[i][j];

		if (currentSquare.getCurrentColor() == Color.white)
		    {
			currentSquare.setCurrentColor(currentSquare.getBallColor());
			currentSquare.repaint();
			unhighlightGroup(bp, i, j-1);
		        unhighlightGroup(bp, i, j+1);
			unhighlightGroup(bp, i-1, j);
			unhighlightGroup(bp, i+1, j);
		    }
	    }
    }

    /**
       Called when the mouse clicks on a squarePanel.  If the squarePanel has at least
       one same-color neighbor, the same-color group will delete, the score is updated,
       and the board compacts.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
    */
    public static boolean deleteGroup(BoardPanel bp, BoardPanel childbp,
					 int i, int j)
    {
	Color c = childbp.squaresArr[i][j].getBallColor();
	boolean gameOver = false;
	
	if (checkNeighbors(childbp, i, j, c))
	    {
		deleteRecurse(bp, childbp, i, j, c);
		ballsFall(childbp);
		shiftLeft(childbp);
		gameOver = isGameOver(childbp);
		showScore(childbp, gameOver);
	    }
	return gameOver;
    }

    /**
       Does the deletion work.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
       @param mouseAtColor the squarePanel's color.
    */
    public static void deleteRecurse(BoardPanel bp, BoardPanel childbp,
				     int i, int j, Color mouseAtColor)
    {
	if (i >= 0 && i <= (WIDTH - 1) && j >= 0 && j <= (HEIGHT - 1))
	    {
		SquarePanel currentSquare = childbp.squaresArr[i][j];
		SquarePanel parentSquare = bp.squaresArr[i][j];

		if ((currentSquare.getBallColor().equals(mouseAtColor)) &&
		     (currentSquare.getBallColor() != Color.black))
		    {
			parentSquare.setSeenBefore(true);
			currentSquare.setBallColor(Color.black);
			currentSquare.setCurrentColor(currentSquare.getBallColor());
			currentSquare.repaint();
			childbp.numberDeleted++;
			childbp.numberRemaining--;
			deleteRecurse(bp, childbp, i, j-1, mouseAtColor);
			deleteRecurse(bp, childbp, i, j+1, mouseAtColor);
			deleteRecurse(bp, childbp, i-1, j, mouseAtColor);
			deleteRecurse(bp, childbp, i+1, j, mouseAtColor);
		    }
	    }
    }
    
    /**
       Checks to see whether the game is over by checking each squarePanel for same-color
       neighbors.
       @return true if there are no squares with at least one same-color neighbor; false
       otherwise.
    */
    public static boolean isGameOver(BoardPanel bp)
    {
	for (int i = 0; i < WIDTH; i++)
	    {
		for (int j = 0; j < HEIGHT; j++)
		    {
			Color c = bp.squaresArr[i][j].getBallColor();
			if (checkNeighbors(bp, i, j, c)) return false;
		    }
	    }
	return true;
    }

    /**
       Updates the text for the scoreDisplay in the scorePanel.
       @param gameOver returned by isGameOver.
    */
    public static void showScore(BoardPanel bp, boolean gameOver)
    {
	String newDisplay = "";
	
	if (bp.numberRemaining == 0)
	    {
		bp.score = bp.score + ((bp.numberDeleted - 2) * (bp.numberDeleted - 2)) + 1000;
		newDisplay = "FINAL SCORE: " + bp.score + " *** GAME OVER ***";
		bp.scoreDisplay.setText(newDisplay);
		return;
	    }
	else 
	    {
		bp.score = bp.score + ((bp.numberDeleted - 2) * (bp.numberDeleted - 2));
	    }
	if (gameOver)
	    {
		newDisplay = "FINAL SCORE: " + bp.score + " *** GAME OVER ***";
		bp.scoreDisplay.setText(newDisplay);
	    }
	else
	    {
		newDisplay = "SCORE: " + bp.score;
		bp.scoreDisplay.setText(newDisplay);
	    }
	bp.numberDeleted = 0;
    }

    /**
       Compacts the board downwards.  In any given row, working from the bottom up,
       if a black (ie blank) square is found, it will call a method to switch it with
       the first square higher up that is not blank.
    */
    public static void ballsFall(BoardPanel bp)
    {
	for (int i = 0; i < WIDTH; i++)
	    {
		for (int j = (HEIGHT - 1); j >= 0; j--)
		    {
			SquarePanel currentSquare = bp.squaresArr[i][j];
			if (currentSquare.getBallColor() == Color.black)
			    {
				switchBalls(bp, i, j);
			    }
		    }
	    }
    }

    /**
       Does the ball-switching for the ballsFall() method.
       @param i the squarePanel's "x" coordinate in squareArr.
       @param j the squarePanel's "y" coordinate in squareArr.
    */
    public static void switchBalls(BoardPanel bp, int i, int j)
    {
	for (int k = (j - 1); k >= 0; k--)
	    {
		SquarePanel currentSquare = bp.squaresArr[i][j];
		SquarePanel switchSquare = bp.squaresArr[i][k];

		if (switchSquare.getBallColor() != Color.black)
		    {
			currentSquare.setBallColor(switchSquare.getBallColor());
			currentSquare.setCurrentColor(currentSquare.getBallColor());
			switchSquare.setBallColor(Color.black);
			switchSquare.setCurrentColor(switchSquare.getBallColor());
			currentSquare.repaint();
			switchSquare.repaint();
			break;
		    }
	    }
    }

    /**
       Called by shiftLeft to check to see if there are any empty columns.
       @return the index in squareArr of the empty column; if there are none empty, will
       return WIDTH (ie out of squareArr's bounds).
    */
    public static int checkEmptyColumns(BoardPanel bp)
    {
	for (int i = 0; i < WIDTH; i++)
	    {
		if (bp.squaresArr[i][HEIGHT - 1].getBallColor() == Color.black)
		    {
			return i;
		    }	       
	    }
	return WIDTH;
    }

    /**
       Compacts the board leftwards when there are empty columns.
    */
    public static void shiftLeft(BoardPanel bp)
    {
	int currentColumn = checkEmptyColumns(bp);
	for (int i = currentColumn + 1; i < WIDTH; i++)
	    {
		if (bp.squaresArr[i][HEIGHT - 1].getBallColor() != Color.black)
		    {			
			for (int j = 0; j < HEIGHT; j++)
			    {
				SquarePanel currentSquare = bp.squaresArr[currentColumn][j];
				SquarePanel switchSquare = bp.squaresArr[i][j];

				currentSquare.setBallColor(switchSquare.getBallColor());
				currentSquare.setCurrentColor(currentSquare.getBallColor());
				switchSquare.setBallColor(Color.black);
				switchSquare.setCurrentColor(switchSquare.getBallColor());
				currentSquare.repaint();
				switchSquare.repaint();		       
			    }
			currentColumn++;
		    }
	    }
    }
}

/**
   The class that makes the smaller panels, one per ball.
*/
class SquarePanel extends JPanel
{
    private int across;
    private int down;
    private BoardPanel bpIAmIn;
    private boolean seenBefore = false;
    
    private Color[] colorArr = {Color.red, Color.green, Color.blue};
    private Color ballColor;
    private Color currentColor;

    /**
       MouseHandling occurs in each small squarePanel (an inner class).
    */
    class MouseHandler extends MouseAdapter
    {
	// does not respond to mouse being on BALL, only in ball's panel - but when you 
	// look at it, it really is close enough (I think so, anyway).
	public void mouseEntered(MouseEvent ev)
	{
	    BoardPanel.highlightGroup(bpIAmIn, across, down);
	}

	public void mouseExited(MouseEvent ev)
	{
	    BoardPanel.unhighlightGroup(bpIAmIn, across, down);
	}
	
	public void mouseClicked(MouseEvent ev)
	{
	    BoardPanel.deleteGroup(bpIAmIn, bpIAmIn, across, down);
	}
    }
    /**
       Constructor.
       @param i the across index in squaresArr, as allocated when theBoardPanel is
       constructed.
       @param j the down index in squaresArr, as allocated when theBoardPanel is
       constructed.
    */
    public SquarePanel(BoardPanel bp, int i, int j)
    {
	across = i;
	down = j;
	bpIAmIn = bp;

	setBackground(Color.black);
	int c = (int)(Math.random() * 3);
	ballColor = colorArr[c];
	currentColor = ballColor;

	addMouseListener(new MouseHandler());
    }

    // an constructor for board-copying only
    public SquarePanel(BoardPanel bp, int i, int j, Color c)
    {
	across = i;
	down = j;
	bpIAmIn = bp;
	ballColor = c;
	currentColor = ballColor;
    }
	
    public boolean getSeenBefore() { return seenBefore; }
    public void setSeenBefore(boolean b) { seenBefore = b; }
    
    /**
       Accessor for ball's ("real") color.
    */
    public Color getBallColor()
    {
	return ballColor;
    }

    /**
       Accessor for ball's apparent color (highlighting turns them white).
    */
    public Color getCurrentColor()
    {
	return currentColor;
    }

    /**
       Mutator for ball's real color (deletion turns them black).
    */
    public void setBallColor(Color c)
    {
	ballColor = c;
    }

    /**
       Mutator for ball's apparent color.
    */
    public void setCurrentColor(Color c)
    {
	currentColor = c;
    }
    /**
       Draws the ball for each panel.
    */
    public void paintComponent(Graphics g)
    {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D)g;
	
	Ellipse2D ball = new Ellipse2D.Double(3, 3, 44, 44);
	g2.setPaint(currentColor);
	g2.fill(ball);
    }	
}