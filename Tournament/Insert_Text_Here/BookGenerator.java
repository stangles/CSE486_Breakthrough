package Insert_Text_Here;

import game.GamePlayer;
import game.GameState;
import game.Util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import breakthrough.BreakthroughMove;
import breakthrough.BreakthroughState;

public class BookGenerator {
	private static int depthLimit = 1;
	private static int moveDepth = 4;
	private static String fileName = "openingbook.dat";
	private static LinkedHashSet<String> boards = new LinkedHashSet<String>();
	private static GamePlayer p = new Insert_Text_Here("Insert_Text_Here",
			depthLimit);
	private static PrintWriter file = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Generating opening book with search depth "
				+ depthLimit + " and move depth " + moveDepth + "...");
		long start = System.currentTimeMillis();
		generateBook();
		System.out.println("Generation took "
				+ (System.currentTimeMillis() - start) + " ms.");
	}

	private static void generateBook() {
		try {
			file = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BreakthroughState init = new BreakthroughState();
		init.who = GameState.Who.HOME;
		generateStates(init, 0);
		for (String s : boards) {
			file.println(s);
		}
		file.close();
	}

	private static void generateStates(BreakthroughState brd, int currMvDepth) {
		if (currMvDepth == moveDepth) {
			return;
		} else {
			GameState.Who currTurn = brd.getWho();
			char me = brd.who == BreakthroughState.Who.HOME ? BreakthroughState.homeSym
					: BreakthroughState.awaySym;
			int dir = brd.who == BreakthroughState.Who.HOME ? +1 : -1;
			// Find valid moves
			ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
			BreakthroughMove mv = new BreakthroughMove();
			for (int r = 0; r < BreakthroughState.N; r++) {
				for (int c = 0; c < BreakthroughState.N; c++) {
					mv.startRow = r;
					mv.startCol = c;
					if (brd.board[r][c] == me) {
						mv.endingRow = r + dir;
						mv.endingCol = c;
						if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove) mv.clone());
						}
						mv.endingRow = r + dir;
						mv.endingCol = c + 1;
						if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove) mv.clone());
						}
						mv.endingRow = r + dir;
						mv.endingCol = c - 1;
						if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove) mv.clone());
						}
					}
				}
			}
			for (BreakthroughMove tempMv : moves) {
				char prevPiece = brd.board[tempMv.endingRow][tempMv.endingCol];
				brd.makeMove(tempMv);
				BreakthroughMove bm = (BreakthroughMove) p.getMove(brd, "");
				char who = brd.who == BreakthroughState.Who.HOME ? BreakthroughState.homeSym
						: BreakthroughState.awaySym;
				boards.add(who + " " + OpeningBook.encode(Util.toString(brd.board))
						+ " " + bm.toString());
				generateStates(brd, currMvDepth + 1);
				// Undo move
				brd.board[tempMv.endingRow][tempMv.endingCol] = prevPiece;
				brd.board[tempMv.startRow][tempMv.startCol] = me;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;
			}
		}
	}
}
