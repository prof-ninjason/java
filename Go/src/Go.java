import java.util.*;

public class Go
{
    private static ArrayList emptyGroup = new ArrayList();
    private static boolean emptySeen = false;
    
    // OK, get ready for some of the ugliest code I've ever seen -
    
    public static char[][] legalMoves(char[][] board, char toPlay)
    {
	char opponent;
	if (toPlay == 'W') opponent = 'B';
	else opponent = 'W';

	Node[] graphNodes = constructNodeArray(board);
	for (int i = 0; i < graphNodes.length; i++)
	    {
		// the particular node we are dealing with
		Node node = graphNodes[i];

		if (node.getColor() == 'E')
		    {
			// emptyGroup is a static ArrayList that holds all the empty nodes
			// seen in a given dfs pass - make sure it's clear to start
			emptyGroup.clear();
			
			// the first dfs
			legalMovesRecurse(graphNodes, node);

			// if any empty square has an empty neighbor, it is Legal -
			// so label all connected empty squares Legal (L)
			if (emptyGroup.size() > 1)
			    {
				for (int j = 0; j < emptyGroup.size(); j++)
				    {
					Node n = (Node)emptyGroup.get(j);
					n.setColor('L');
				    }
			    }

			// must do other checks for empty squares that don't have any
			// empty neighbors			
			else
			    {
				// reuse emptyGroup (although now it's badly named) for the 
				// next dfs pass
				emptyGroup.clear();

				// suicideCheck checks to see if placing a stone on the 
				// empty square in question would yield a toPlay-colored
				// connected group of stones that is surrounded by the 
				// opponent.  if the static variableemptySeen is ever 
				// set to true, the connected group is not surrounded.
				emptySeen = false;

				// the second dfs
				suicideCheck(graphNodes, node, i, toPlay, opponent);

				// cleanup in case we must search again:
				// reset searched nodes' "seenBefore" marker before each 
				// successive dfs pass.
				for (int j = 0; j < emptyGroup.size(); j++)
				    {
					Node n = (Node)emptyGroup.get(j);
					n.setSeenBefore(false);
				    }

				// if the connected group is not surrounded, the empty
				// square is legal.
				if (emptySeen) node.setColor('L');
				
				// otherwise (yet more) checks must be done.
				else
				    {
					LinkedList edges = node.getEdges();
					ListIterator iterEdges = edges.listIterator();
					boolean opponentNeighborSeen = false;
					while (iterEdges.hasNext())
					    {
						Node neighbor =
						    graphNodes[((Integer)iterEdges.next()).
							      intValue()];
						if (neighbor.getColor() != toPlay)
						    {
							opponentNeighborSeen = true;
							break;
						    }
					    }

					// if ALL of the empty square's IMMEDIATE neighbors 
					// are the same color as toPlay, it is an illegal
					// move (I)			
					if (!opponentNeighborSeen) node.setColor('I');
					
					// otherwise run suicideCheck again WITH THE COLORS
					// SWITCHED
					else
					    {
						emptyGroup.clear();
						emptySeen = false;

						// the third dfs
						suicideCheck(graphNodes, node, i, opponent,
							     toPlay);
						
						for (int j = 0; j < emptyGroup.size(); j++)
						    {
							Node n = (Node)emptyGroup.get(j);
							n.setSeenBefore(false);
						    }
						
						// if the connected group of stones found by 
						// suicideCheck this time round is surrounded, 
						// the move is legal (because this means 
						// toPlay can capture his opponent's stones).
						// otherwise toPlay is genuinely surrounded 
						// by opponent, and the move is illegal.
						if (!emptySeen) node.setColor('L');
						else node.setColor('I');
					    }
				    }
			    }
		    }
	    }
	char[][] newBoard = constructNewBoard(graphNodes, board.length);
	return newBoard;
    }

    public static void legalMovesRecurse(Node[] graphNodes, Node node)
    {
	node.setSeenBefore(true);
	emptyGroup.add(node);
	LinkedList edges = node.getEdges();
	ListIterator iterEdges = edges.listIterator();
	while (iterEdges.hasNext())
	    {
		Node nextNode = graphNodes[((Integer)iterEdges.next()).intValue()];
		if ((nextNode.getColor() == 'E') && (!(nextNode.getSeenBefore())))
		    legalMovesRecurse(graphNodes, nextNode);
	    }
    }

    public static void suicideCheck(Node[] graphNodes, Node node, int startNodeIndex,
				    char toPlay, char opponent)
    {
	node.setSeenBefore(true);
	emptyGroup.add(node);
	LinkedList edges = node.getEdges();
	ListIterator iterEdges = edges.listIterator();
	while (iterEdges.hasNext())
	    {
		Node nextNode = graphNodes[((Integer)iterEdges.next()).intValue()];
		if ((nextNode.getColor() == toPlay) && (!(nextNode.getSeenBefore())))
		    suicideCheck(graphNodes, nextNode, startNodeIndex, toPlay, opponent);
		else if (nextNode.getName() != startNodeIndex &&
			 nextNode.getColor() != toPlay &&
			 nextNode.getColor() != opponent)
		    emptySeen = true;
	    }
    }

    public static Node[] constructNodeArray(char[][] board)
    {
	int length = board.length;

	// Constructs the appropriate node array
	// (I'd written everything for one-d node arrays, and prefer them anyway...)
	Node[] graphNodes = new Node[length*length];
	for (int i = 0; i < length; i++)
	    {
		for (int j = 0; j < length; j++)
		    {
			char color = board[i][j];
			int offset = length*i + j;
			if (i == 0 && j == 0)
			    graphNodes[offset] = new Node(0, new int[] {1, length}, color);
			else if (i == 0 && j == length-1)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-1, offset+length}, color);
			else if (i == length-1 && j == 0)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-length, offset+1}, color);
			else if (i == length-1 && j == length-1)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-length, offset-1}, color);
			else if (i == 0)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-1, offset+1, offset+length},
							  color);
			else if (i == length-1)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-length, offset-1, offset+1},
							  color);
			else if (j == 0)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-length, offset+1,
						       offset+length}, color);
			else if (j == length-1)
			    graphNodes[offset] = new Node(offset,
					    new int[] {offset-length, offset-1,
						       offset+length}, color);
			else graphNodes[offset] = new Node(offset,
					     new int[] {offset-length, offset-1,
							offset+1, offset+length},
					     color);
		    }
	    }
	return graphNodes;
    }

    public static char[][] constructNewBoard(Node[] graphNodes, int length)
    {
	char[][] board = new char[length][length];
	int offset;
	for (int i = 0; i < length; i++)
	    {
		for (int j = 0; j < length; j++)
		    {
			offset = length*i + j;
			board[i][j] = graphNodes[offset].getColor();
		    }
	    }
	return board;
    }
}

class Node
{
    private int name; // this will be the same as the node's position in the graph array
    private LinkedList edges = new LinkedList();
    private boolean seenBefore = false;
    private char color = 'E'; // E = empty, B = black, W = white

    public Node(int n, int[] e)
    {
	name = n;
	for (int i = 0; i < e.length; i++)
	    {
		Integer nodeNum = new Integer(e[i]);
		edges.add(nodeNum);
	    }
    }

    public Node(int n, int[] e, char c)
    {
	name = n;
	color = c;
	for (int i = 0; i < e.length; i++)
	    {
		Integer nodeNum = new Integer(e[i]);
		edges.add(nodeNum);
	    }
    }

    // only used internally
    public Node(int n, LinkedList edges, char c)
    {
	name = n;
	color = c;
	this.edges = edges; // LinkedList edges is ALREADY a copy
    }

    // accessors
    public int getName() { return name; }
    public LinkedList getEdges() { return (LinkedList)edges.clone(); }
    public boolean getSeenBefore() { return seenBefore; }
    public char getColor() { return color; }

    // mutators
    public void setSeenBefore(boolean b) { seenBefore = b; }
    public void setColor(char c) { color = c; }
}