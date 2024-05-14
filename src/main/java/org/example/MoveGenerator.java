package org.example;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private final Chessboard chessboard;

    public MoveGenerator(Chessboard chessboard) {
        this.chessboard = chessboard;
    }

    public List<Move> generateMoves(boolean isWhite) {
        List<Move> moves = new ArrayList<>();
        char[][] board = chessboard.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];
                if ((Character.isUpperCase(piece) && isWhite) || (Character.isLowerCase(piece) && !isWhite)) {
                    switch (Character.toLowerCase(piece)) {
                        case 'p':
                            generatePawnMoves(row, col, moves, isWhite);
                            break;
                        case 'r':
                            generateRookMoves(row, col, moves);
                            break;
                        case 'n':
                            generateKnightMoves(row, col, moves);
                            break;
                        case 'b':
                            generateBishopMoves(row, col, moves);
                            break;
                        case 'q':
                            generateQueenMoves(row, col, moves);
                            break;
                        case 'k':
                            generateKingMoves(row, col, moves);
                            break;
                    }
                }
            }
        }
        return moves;
    }


    private void generatePawnMoves(int row, int col, List<Move> moves, boolean isWhite) {
        int forward = isWhite ? 1 : -1;
        int startRow = isWhite ? 1 : 6;
        int newRow = row + forward;

        // Straight move
        if (newRow >= 0 && newRow < 8 && chessboard.getBoard()[newRow][col] == ' ') {
            addMoveIfValid(row, col, newRow, col, moves);
            // Double move from start position
            if (row == startRow && chessboard.getBoard()[newRow + forward][col] == ' ') {
                addMoveIfValid(row, col, newRow + forward, col, moves);
            }
        }
        // Captures
        int[] captureCols = {col - 1, col + 1};
        for (int captureCol : captureCols) {
            if (newRow >= 0 && newRow < 8 && captureCol >= 0 && captureCol < 8
                    && chessboard.getBoard()[newRow][captureCol] != ' '
                    && Character.isUpperCase(chessboard.getBoard()[row][col]) != Character.isUpperCase(chessboard.getBoard()[newRow][captureCol])) {
                addMoveIfValid(row, col, newRow, captureCol, moves);
            }
        }
    }

    private void generateBishopMoves(int row, int col, List<Move> moves) {
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

    private void generateRookMoves(int row, int col, List<Move> moves) {
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

    private void generateQueenMoves(int row, int col, List<Move> moves) {
        generateRookMoves(row, col, moves);
        generateBishopMoves(row, col, moves);
    }

    private void generateKingMoves(int row, int col, List<Move> moves) {
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

    private void generateKnightMoves(int row, int col, List<Move> moves) {
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

    private boolean addMoveIfValid(int startRow, int startCol, int endRow, int endCol, List<Move> moves) {
        if (endRow >= 0 && endRow < 8 && endCol >= 0 && endCol < 8) {
            if (chessboard.getBoard()[endRow][endCol] == ' ' || Character.isUpperCase(chessboard.getBoard()[startRow][startCol]) != Character.isUpperCase(chessboard.getBoard()[endRow][endCol])) {
                moves.add(new Move(startRow, startCol, endRow, endCol));
                return chessboard.getBoard()[endRow][endCol] == ' '; // Continue if empty
            }
        }
        return false; // Blocked or out of bounds
    }

    public boolean isKingInCheck(boolean isWhite) {
        // Locate the king
        int kingRow = -1, kingCol = -1;
        char king = isWhite ? 'K' : 'k';
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                if (chessboard.getBoard()[row][col] == king) {

                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break; // Break outer loop if king is found
        }

        // Check for attacks
        List<Move> enemyMoves = generateMoves(!isWhite);
        for (Move move : enemyMoves) {
            if (move.getEndRow() == kingRow && move.getEndCol() == kingCol) {
                return true; // The king is in check
            }
        }

        return false; // No enemy can attack the king's position
    }


    public boolean isMoveValid(Move move) {
        // Simulate the move
        char[][] board = chessboard.getBoard();
        char piece = board[move.getStartRow()][move.getStartCol()];
        char capturedPiece = board[move.getEndRow()][move.getEndCol()];
        // Perform the move temporarily
        board[move.getEndRow()][move.getEndCol()] = piece;
        board[move.getStartRow()][move.getStartCol()] = ' ';

        boolean isValid = !isKingInCheck(Character.isUpperCase(piece)); // Check if this move leaves the king in check

        // Undo the move
        board[move.getStartRow()][move.getStartCol()] = piece;
        board[move.getEndRow()][move.getEndCol()] = capturedPiece;

        return isValid;
    }

    // Implement all move generation methods here, similar to previous example
}
