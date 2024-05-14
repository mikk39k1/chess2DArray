package org.example;

import java.util.List;

public class Chessboard {
    private static final int MAX_DEPTH = 5;
    private static int[] nodeCount;
    private final char[][] board;
    private final Evaluation evaluation;
    private final MoveGenerator moveGenerator;

    public Chessboard() {
        this.board = new char[8][8];
        initializeBoard();
        nodeCount = new int[MAX_DEPTH + 1];
        this.evaluation = new Evaluation(this);  // Assuming Evaluation is properly defined
        this.moveGenerator = new MoveGenerator(this);  // Assuming MoveGenerator is properly defined
    }

    public char[][] getBoard() {
        return board;
    }

    public String printBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("     A    B    C    D    E    F    G    H\n");
        sb.append("  +----+----+----+----+----+----+----+----+\n");

        for (int rank = 7; rank >= 0; rank--) {
            sb.append(rank + 1).append(" |");
            for (int file = 0; file < 8; file++) {
                char piece = board[rank][file];
                sb.append("  ").append(piece).append(" |");
            }
            sb.append("  ").append(rank + 1).append("th rank\n");
            sb.append("  +----+----+----+----+----+----+----+----+\n");
        }

        sb.append("     A    B    C    D    E    F    G    H - file(s)\n");
        return sb.toString();
    }

    public void printScores() {
        // Evaluate the board from the perspective of white
        int whiteScore = evaluation.evaluateBoard(true);
        // Evaluate the board from the perspective of black
        int blackScore = evaluation.evaluateBoard(false);

        System.out.println("White's total score: " + whiteScore);
        System.out.println("Black's total score: " + blackScore);
    }

    public boolean processMove(String moveInput, boolean isWhiteTurn) {
        String[] parts = moveInput.trim().split("[-\\s]+");  // assuming input like "e2 e4" or "e2-e4"
        if (parts.length != 2) {
            System.out.println("Invalid format. Use the format 'e2 e4' or 'e2-e4'.");
            return false;
        }

        int[] start = parsePosition(parts[0]);
        int[] end = parsePosition(parts[1]);
        if (start == null || end == null) {
            System.out.println("Invalid positions. Ensure positions are within 'a1' to 'h8'.");
            return false;
        }

        Move proposedMove = new Move(start[0], start[1], end[0], end[1]);
        if (!moveGenerator.isMoveValid(proposedMove)) {
            System.out.println("Invalid move for the piece. Try again.");
            return false;
        }

        makeMove(proposedMove);
        return true;
    }


    public Move findBestMove(boolean isWhite) {
        int bestVal = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Move bestMove = null;
        List<Move> moves = moveGenerator.generateMoves(isWhite);

        for (Move move : moves) {
            char capturedPiece = board[move.getEndRow()][move.getEndCol()];
            makeMove(move);
            int moveVal = alphaBeta(MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite);
            undoMove(move, capturedPiece);

            if (isWhite ? moveVal > bestVal : moveVal < bestVal) {  // Maximizing for white, minimizing for black
                bestVal = moveVal;
                bestMove = move;
            }
        }
        return bestMove;
    }


    public void makeMove(Move move) {
        board[move.getEndRow()][move.getEndCol()] = board[move.getStartRow()][move.getStartCol()];
        board[move.getStartRow()][move.getStartCol()] = ' ';
    }

    private void undoMove(Move move, char capturedPiece) {
        board[move.getStartRow()][move.getStartCol()] = board[move.getEndRow()][move.getEndCol()];
        board[move.getEndRow()][move.getEndCol()] = capturedPiece;
    }

    private void initializeBoard() {
        // Setup board with pieces in initial positions
        board[0] = new char[]{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}; // White major pieces
        board[1] = new char[]{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'}; // White pawns
        for (int i = 2; i < 6; i++) { // Empty squares
            for (int j = 0; j < 8; j++) {
                board[i][j] = ' ';
            }
        }
        board[6] = new char[]{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'}; // Black pawns
        board[7] = new char[]{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}; // Black major pieces
    }

    public int[] parsePosition(String position) {
        if (position.length() != 2) return null;
        int file = position.charAt(0) - 'a';
        int rank = position.charAt(1) - '1';
        if (file < 0 || file >= 8 || rank < 0 || rank >= 8) return null;
        return new int[]{rank, file};
    }

    private boolean isValidMove(int[] start, int[] end) {
        // This should use the MoveGenerator to validate moves
        return moveGenerator.isMoveValid(new Move(start[0], start[1], end[0], end[1]));
    }

    public boolean checkGameOver() {
        // Implement game over conditions
        return false;
    }

    private int alphaBeta(int depth, int alpha, int beta, boolean maximizingPlayer) {
        nodeCount[depth]++; // Increment node count at the current depth

        if (depth == 0 || checkGameOver()) {  // Also check for game over condition
            return evaluation.evaluateBoard(maximizingPlayer);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moveGenerator.generateMoves(true)) { // Assuming true for White
                char capturedPiece = board[move.getEndRow()][move.getEndCol()];
                makeMove(move);  // Now using Move object
                if (!moveGenerator.isKingInCheck(true)) { // Only consider moves that do not put White in check
                    int eval = alphaBeta(depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, maxEval);
                    if (beta <= alpha) {
                        undoMove(move, capturedPiece); // Now using Move object
                        break;
                    }
                }
                undoMove(move, capturedPiece); // Now using Move object
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moveGenerator.generateMoves(false)) { // Assuming false for Black
                char capturedPiece = board[move.getEndRow()][move.getEndCol()];
                makeMove(move);  // Now using Move object
                if (!moveGenerator.isKingInCheck(false)) { // Only consider moves that do not put Black in check
                    int eval = alphaBeta(depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, minEval);
                    if (beta <= alpha) {
                        undoMove(move, capturedPiece); // Now using Move object
                        break;
                    }
                }
                undoMove(move, capturedPiece); // Now using Move object
            }
            return minEval;
        }
    }

}
