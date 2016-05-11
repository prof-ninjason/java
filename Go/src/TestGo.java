public class TestGo
{
    public static void main(String[] args)
    {
	char[][] board =
	{
	    {'E', 'B', 'B', 'E', 'E'},
	    {'B', 'W', 'W', 'B', 'E'},
	    {'B', 'W', 'E', 'W', 'B'},
	    {'B', 'W', 'W', 'W', 'B'},
	    {'E', 'B', 'B', 'B', 'E'}
	};
	char[][] newBoard = Go.legalMoves(board, 'B');
	printBoard(newBoard);
    }

    public static void printBoard(char[][] board)
    {
	int length = board.length;
	for (int i = 0; i < length; i++)
	    {
		for (int j = 0; j< length; j++)
		    {
			System.out.print(board[i][j] + "  ");
		    }
		System.out.println("");
	    }
    }
}

/*  OUTPUT OF ABOVE:

L  B  B  L  L  
B  W  W  B  L  
B  W  L  W  B  
B  W  W  W  B  
L  B  B  B  L  

*/






