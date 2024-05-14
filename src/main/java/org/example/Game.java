package org.example;

import java.util.Scanner;

public class Game {
    private Chessboard board;
    private boolean isWhiteTurn;
    private Scanner scanner;

    public Game() {
        this.board = new Chessboard(); // Ensure Chessboard is set up to handle initialization properly
        this.isWhiteTurn = true;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            System.out.println(board.printBoard());
            board.printScores();
            if (isWhiteTurn) {
                System.out.println("White's move: ");
                String moveInput = scanner.nextLine();
                if (!board.processMove(moveInput, isWhiteTurn)) {
                    System.out.println("Invalid move, try again.");
                    continue; // If invalid, don't switch turns.
                }
            } else {
                System.out.println("Black's move:");
                Move bestMove = board.findBestMove(isWhiteTurn); // Use isWhiteTurn directly
                if (bestMove != null) {
                    board.makeMove(bestMove);
                    System.out.println("Black moves from " + bestMove + ".");
                } else {
                    System.out.println("No valid moves available for Black.");
                }
            }
            isWhiteTurn = !isWhiteTurn; // Switch turns
            System.out.println((isWhiteTurn ? "White" : "Black") + " to move next.");
            if (board.checkGameOver()) {
                System.out.println("Game over.");
                break;
            }
        }
        scanner.close();
    }
}
