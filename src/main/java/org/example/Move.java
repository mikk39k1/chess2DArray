package org.example;

public class Move {
    private int startRow, startCol;
    private int endRow, endCol;

    public Move(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    @Override
    public String toString() {
        return String.format("from (%d, %d) to (%d, %d)", startRow, startCol, endRow, endCol);
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }


}
