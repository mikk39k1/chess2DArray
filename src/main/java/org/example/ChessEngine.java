package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ChessEngine {

    private static final char[][] board = new char[8][8]; // Chess board
    private static boolean isWhiteTurn = true; // Track whose turn it is

    private static final int MAX_DEPTH = 5; // Maximum depth for the alphaBeta search
    private static int[] nodeCount;

    // Piece values
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    // Piece-square tables
    private static final int[][] PAWN_TABLE = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            { 5,  5, 10, 25, 25, 10,  5,  5},
            { 0,  0,  0, 20, 20,  0,  0,  0},
            { 5, -5,-10,  0,  0,-10, -5,  5},
            { 5, 10, 10,-20,-20, 10, 10,  5},
            { 0,  0,  0,  0,  0,  0,  0,  0}
    };

    private static final int[][] KNIGHT_TABLE = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    private static final int[][] BISHOP_TABLE = {
            {-20,-10,-10,-10,-10,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5, 10, 10,  5,  0,-10},
            {-10,  5,  5, 10, 10,  5,  5,-10},
            {-10,  0, 10, 10, 10, 10,  0,-10},
            {-10, 10, 10, 10, 10, 10, 10,-10},
            {-10,  5,  0,  0,  0,  0,  5,-10},
            {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    private static final int[][] ROOK_TABLE = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 5, 10, 10, 10, 10, 10, 10,  5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            { 0,  0,  0,  5,  5,  0,  0,  0}
    };

    private static final int[][] QUEEN_TABLE = {
            {-20,-10,-10, -5, -5,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5,  5,  5,  5,  0,-10},
            { -5,  0,  5,  5,  5,  5,  0, -5},
            {  0,  0,  5,  5,  5,  5,  0, -5},
            {-10,  5,  5,  5,  5,  5,  0,-10},
            {-10,  0,  5,  0,  0,  0,  0,-10},
            {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    private static final int[][] KING_TABLE = {
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            { 20, 20,  0,  0,  0,  0, 20, 20},
            { 20, 30, 10,  0,  0, 10, 30, 20}
    };

    public static void main(String[] args) {
        initializeBoard();
        Scanner scanner = new Scanner(System.in);
        // Set the desired maximum depth
        nodeCount = new int[MAX_DEPTH + 1];

        while (true) {
            System.out.println(printBoard());
            printScores();
            if (isWhiteTurn) {
                System.out.println("White's move: ");
                String move = scanner.nextLine();
                if (!processMove(move)) {
                    System.out.println("Invalid move, try again.");
                    continue;
                }
            } else {
                System.out.println("Black's move:");
                int[] bestMove = findBestMove(); // Utilizing the alphaBeta search
                if (bestMove != null) {
                    makeMove(new int[]{bestMove[0], bestMove[1]}, new int[]{bestMove[2], bestMove[3]});
                    System.out.println("All moves: " + generateMoves(false));
                    System.out.println("Black moves from " + posToString(new int[]{bestMove[0], bestMove[1]}) + " to " + posToString(new int[]{bestMove[2], bestMove[3]}) + ".");
                } else {
                    System.out.println("No valid moves available for Black.");
                }
            }
            isWhiteTurn = !isWhiteTurn; // Switch turns
            if (checkGameOver()) {
                System.out.println("Game over.");
                break;
            }

            // Log the node count for each depth level
            System.out.println("Nodes evaluated at each depth:");
            for (int i = 0; i <= MAX_DEPTH; i++) {
                System.out.println("Depth " + i + ": " + nodeCount[i] + " nodes");
            }
            // Reset the node count array for the next move
            Arrays.fill(nodeCount, 0);
        }
        scanner.close();
    }

    // Implementing a simple game-over check
    private static boolean checkGameOver() {
        return isCheckmate();
    }

    // Implement findBestMove method using alphaBeta
    private static int[] findBestMove() {
        int bestVal = Integer.MIN_VALUE;
        int[] bestMove = null;
        List<int[]> moves = generateMoves(false); // Assuming false for Black

        for (int[] move : moves) {
            char capturedPiece = board[move[2]][move[3]];
            makeMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]});
            int moveVal = alphaBeta(MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true); // true because after black's move, it's white's turn
            undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);

            if (moveVal > bestVal) {
                bestVal = moveVal;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private static String posToString(int[] pos) {
        return "" + (char)('a' + pos[1]) + (8 - pos[0]);
    }

    private static void makeMove(int[] start, int[] end) {
        board[end[0]][end[1]] = board[start[0]][start[1]];

        board[start[0]][start[1]] = ' ';
    }

    private static void undoMove(int[] start, int[] end, char capturedPiece) {
        board[start[0]][start[1]] = board[end[0]][end[1]];
        board[end[0]][end[1]] = capturedPiece;
    }

    private static int evaluateBoard() {
        int score = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                score += getPieceValue(board[i][j], i, j);
            }
        }
        return score;
    }

    private static int getPieceValue(char piece, int row, int col) {
        if (Character.isUpperCase(piece)) {
            row = 7 - row; // Adjust for white perspective
        }

        return switch (Character.toLowerCase(piece)) {
            case 'p' -> PAWN_VALUE + PAWN_TABLE[row][col];
            case 'r' -> ROOK_VALUE + ROOK_TABLE[row][col];
            case 'n' -> KNIGHT_VALUE + KNIGHT_TABLE[row][col];
            case 'b' -> BISHOP_VALUE + BISHOP_TABLE[row][col];
            case 'q' -> QUEEN_VALUE + QUEEN_TABLE[row][col];
            case 'k' -> KING_VALUE + KING_TABLE[row][col];
            default -> 0;
        };
    }

    private static List<int[]> generateMoves(boolean isWhite) {
        List<int[]> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                if ((Character.isUpperCase(piece) && isWhite) || (Character.isLowerCase(piece) && !isWhite)) {
                    switch (Character.toLowerCase(piece)) {
                        case 'p' -> generatePawnMoves(row, col, moves, isWhite);
                        case 'r' -> generateRookMoves(row, col, moves);
                        case 'n' -> generateKnightMoves(row, col, moves);
                        case 'b' -> generateBishopMoves(row, col, moves);
                        case 'q' -> generateQueenMoves(row, col, moves);
                        case 'k' -> generateKingMoves(row, col, moves);
                    }
                }
            }
        }
        return moves;
    }

    private static void generateRookMoves(int row, int col, List<int[]> moves) {
        // Horizontal and vertical moves
        int[] directions = {-1, 1};
        for (int direction : directions) {
            for (int i = row + direction; i >= 0 && i < 8; i += direction) { // Vertical moves
                if (!addMoveIfValid(row, col, i, col, moves)) break;
            }
            for (int j = col + direction; j >= 0 && j < 8; j += direction) { // Horizontal moves
                if (!addMoveIfValid(row, col, row, j, moves)) break;
            }
        }
    }

    private static void generateKnightMoves(int row, int col, List<int[]> moves) {
        int[][] knightMoves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            addMoveIfValid(row, col, newRow, newCol, moves);
        }
    }

    private static void generateKingMoves(int row, int col, List<int[]> moves) {
        int[] directions = {-1, 0, 1};
        for (int dRow : directions) {
            for (int dCol : directions) {
                if (dRow != 0 || dCol != 0) {
                    int newRow = row + dRow;
                    int newCol = col + dCol;
                    addMoveIfValid(row, col, newRow, newCol, moves);
                }
            }
        }
    }

    private static void generateQueenMoves(int row, int col, List<int[]> moves) {
        generateRookMoves(row, col, moves);
        generateBishopMoves(row, col, moves);
    }

    private static void generateBishopMoves(int row, int col, List<int[]> moves) {
        int[] directions = {-1, 1};
        for (int dRow : directions) {
            for (int dCol : directions) {
                int i = row + dRow, j = col + dCol;
                while (i >= 0 && i < 8 && j >= 0 && j < 8) {
                    if (!addMoveIfValid(row, col, i, j, moves)) break;
                    i += dRow;
                    j += dCol;
                }
            }
        }
    }

    private static void generatePawnMoves(int row, int col, List<int[]> moves, boolean isWhite) {
        int forward = isWhite ? 1 : -1;
        int startRow = isWhite ? 1 : 6;
        int newRow = row + forward;

        // Straight move
        if (newRow >= 0 && newRow < 8 && board[newRow][col] == ' ') {
            addMoveIfValid(row, col, newRow, col, moves);
            // Double move from start position
            if (row == startRow && board[newRow + forward][col] == ' ') {
                addMoveIfValid(row, col, newRow + forward, col, moves);
            }
        }
        // Captures
        int[] captureCols = {col - 1, col + 1};
        for (int captureCol : captureCols) {
            if (newRow >= 0 && newRow < 8 && captureCol >= 0 && captureCol < 8
                    && board[newRow][captureCol] != ' '
                    && Character.isUpperCase(board[row][col]) != Character.isUpperCase(board[newRow][captureCol])) {
                addMoveIfValid(row, col, newRow, captureCol, moves);
            }
        }
    }

    private static boolean addMoveIfValid(int startRow, int startCol, int endRow, int endCol, List<int[]> moves) {
        if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) {
            if (board[endRow][endCol] == ' ' || Character.isUpperCase(board[startRow][startCol]) != Character.isUpperCase(board[endRow][endCol])) {
                moves.add(new int[]{startRow, startCol, endRow, endCol});
                return board[endRow][endCol] == ' '; // Continue if empty
            }
        }
        return false; // Blocked or out of bounds
    }

    private static int alphaBeta(int depth, int alpha, int beta, boolean maximizingPlayer) {
        nodeCount[depth]++; // Increment node count at the current depth

        if (depth == 0) {
            return evaluateBoard();
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : generateMoves(true)) { // Assuming true for White
                char capturedPiece = board[move[2]][move[3]];
                makeMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]});
                if (!isCheck(isWhiteTurn)) { // Only consider moves that do not put White in check
                    int eval = alphaBeta(depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, maxEval);
                    if (beta <= alpha) {
                        undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);
                        break;
                    }
                }
                undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] move : generateMoves(false)) { // Assuming false for Black
                char capturedPiece = board[move[2]][move[3]];
                makeMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]});
                if (!isCheck(!isWhiteTurn)) { // Only consider moves that do not put Black in check
                    int eval = alphaBeta(depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, minEval);
                    if (beta <= alpha) {
                        undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);
                        break;
                    }
                }
                undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);
            }
            return minEval;
        }
    }


    private static boolean processMove(String move) {
        String[] parts = move.split(",");
        if (parts.length != 2) {
            System.out.println("Invalid format. Use the format 'a2,a3'.");
            return false;
        }

        int[] start = parsePosition(parts[0]);
        int[] end = parsePosition(parts[1]);
        if (start == null || end == null) {
            System.out.println("Invalid positions. Ensure positions are within 'a1' to 'h8'.");
            return false;
        }

        if (!isValidMove(start, end)) {
            System.out.println("Invalid move for the piece. Try again.");
            return false;
        }

        makeMove(start, end);
        return true;
    }

    private static boolean isValidMove(int[] start, int[] end) {
        char piece = board[start[0]][start[1]];
        if (board[end[0]][end[1]] != ' ' && Character.isUpperCase(piece) == Character.isUpperCase(board[end[0]][end[1]])) {
            return false;
        }

        int deltaX = end[1] - start[1];
        int deltaY = end[0] - start[0];

        return switch (Character.toLowerCase(piece)) {
            case 'p' -> isValidPawnMove(start, end, piece); // Pawn
            case 'r' -> isValidRookMove(start, end); // Rook
            case 'n' -> Math.abs(deltaX) * Math.abs(deltaY) == 2 && Math.abs(deltaX) != 0 && Math.abs(deltaY) != 0; // Knight
            case 'b' -> isValidBishopMove(start, end); // Bishop
            case 'q' -> isValidRookMove(start, end) || isValidBishopMove(start, end); // Queen
            case 'k' -> Math.abs(deltaX) <= 1 && Math.abs(deltaY) <= 1; // King
            default -> false;
        };
    }

    private static boolean isValidPawnMove(int[] start, int[] end, char piece) {
        int forward = Character.isUpperCase(piece) ? 1 : -1;
        if (start[1] == end[1]) { // Move forward
            if ((start[0] == 1 && Character.isUpperCase(piece) && end[0] - start[0] == 2) || // Initial double move for white
                    (start[0] == 6 && !Character.isUpperCase(piece) && start[0] - end[0] == 2)) { // Initial double move for black
                return board[end[0]][end[1]] == ' ' && board[start[0] + forward][start[1]] == ' '; // Ensure path is clear
            }
            return end[0] - start[0] == forward && board[end[0]][end[1]] == ' '; // Regular forward move
        } else if (Math.abs(start[1] - end[1]) == 1 && Math.abs(start[0] - end[0]) == 1) { // Diagonal capture
            return board[end[0]][end[1]] != ' ' && Character.isUpperCase(piece) != Character.isUpperCase(board[end[0]][end[1]]);
        }
        return false;
    }

    private static boolean isValidRookMove(int[] start, int[] end) {
        if (start[0] == end[0]) { // Horizontal move
            for (int file = Math.min(start[1], end[1]) + 1; file < Math.max(start[1], end[1]); file++) {
                if (board[start[0]][file] != ' ') {
                    return false; // Obstacle in the path
                }
            }
        } else if (start[1] == end[1]) { // Vertical move
            for (int rank = Math.min(start[0], end[0]) + 1; rank < Math.max(start[0], end[0]); rank++) {
                if (board[rank][start[1]] != ' ') {
                    return false; // Obstacle in the path
                }
            }
        } else {
            return false; // Not a valid rook move
        }
        return true;
    }

    private static boolean isValidBishopMove(int[] start, int[] end) {
        if (Math.abs(start[0] - end[0]) != Math.abs(start[1] - end[1])) {
            return false; // Not moving diagonally
        }
        int stepX = start[1] < end[1] ? 1 : -1;
        int stepY = start[0] < end[0] ? 1 : -1;
        int x = start[1] + stepX, y = start[0] + stepY;
        while (x != end[1]) {
            if (board[y][x] != ' ') {
                return false; // Obstacle in the path
            }
            x += stepX;
            y += stepY;
        }
        return true;
    }

    private static boolean isCheck(boolean isWhiteTurn) {
        int[] kingPos = findKing();
        System.out.println("King position: " + Arrays.toString(kingPos));
        return isSquareAttacked(kingPos[0], kingPos[1], isWhiteTurn);
    }

    private static boolean isCheckmate() {
        if (!isCheck(isWhiteTurn)) {
            return false;
        }
        List<int[]> moves = generateMoves(isWhiteTurn);
        for (int[] move : moves) {
            char capturedPiece = board[move[2]][move[3]];
            makeMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]});
            boolean stillInCheck = isCheck(isWhiteTurn);
            undoMove(new int[]{move[0], move[1]}, new int[]{move[2], move[3]}, capturedPiece);
            if (!stillInCheck) {
                return false;
            }
        }
        return true;
    }

    private static int[] findKing() {
        char king = isWhiteTurn ? 'K' : 'k';
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == king) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalStateException("King not found on the board");
    }

    private static boolean isSquareAttacked(int row, int col, boolean byWhite) {
        List<int[]> moves = generateMoves(!byWhite);
        for (int[] move : moves) {
            if (move[2] == row && move[3] == col) {
                return true;
            }
        }
        return false;
    }

    private static int[] parsePosition(String position) {
        if (position.length() != 2) return null;
        int file = position.charAt(0) - 'a';
        int rank = position.charAt(1) - '1';
        if (file < 0 || file >= 8 || rank < 0 || rank >= 8) return null;
        return new int[]{rank, file};
    }

    private static void initializeBoard() {
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

    public static String printBoard() {
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

    // Calculate and print the score for both players
    private static void printScores() {
        int whiteScore = 0;
        int blackScore = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char piece = board[i][j];
                if (piece != ' ') {
                    int pieceValue = getPieceValue(piece, i, j);
                    if (Character.isUpperCase(piece)) {
                        whiteScore += pieceValue;
                    } else {
                        blackScore += pieceValue;
                    }
                }
            }
        }
        System.out.println("White's total score: " + whiteScore);
        System.out.println("Black's total score: " + blackScore);
    }
}
