package edu.uw.bothell.css.dsl.mass.apps.mass_sudoku;

import java.util.*;

import edu.uw.bothell.css.dsl.MASS.*;
import edu.uw.bothell.css.dsl.MASS.Agents;
import edu.uw.bothell.css.dsl.MASS.GraphPlaces;
import edu.uw.bothell.css.dsl.MASS.Place;
import edu.uw.bothell.css.dsl.MASS.Places;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.MASSBase;
import edu.uw.bothell.css.dsl.MASS.graph.CytoscapeListener;
import edu.uw.bothell.css.dsl.MASS.graph.Graph;
import edu.uw.bothell.css.dsl.MASS.graph.MASSListener;
import edu.uw.bothell.css.dsl.MASS.graph.transport.VertexModel;
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;

// Author: Nathan Miller

// File Name: SudokuPlaces.java

// Description: Extends from MASS's Place class. Each Place
// is represented by a single puzzle and solved concurrently
// when the Place's callMethod is used.

public class SudokuPlaces extends Place
{
    private int[][] board;       // Local puzzle copy

    // Constructor called by MASS
    public SudokuPlaces(Object arg)
    {
    }

    // setPuzzle()
    // @Desc: sets the 9x9 puzzle grid on this place using the
    //        callALL function.
    // @Param: arg = the 2d array of the 9x9 puzzle
    // @Return: nothing, no solving is being done
    public Object setPuzzle(Object arg)
    {
        // Get current sudoku board
        int[][] src = (int[][]) arg;
        // Create new sudoku board that will always be 9x9
        this.board = new int[9][9];
        // Go through each row and populate with given numbers
        for (int i = 0; i < 9; i++)
        {
            board[i] = Arrays.copyOf(src[i], 9);
        }

        return null;
    }

    // isValid()
    // @Desc: looks if current empty cell (0) with a given number
    //        follows proper sudoku format
    // @Param: r = row on the board
    //         c = column on the board
    //         n = number being compared to the row and column
    // @Return: true if current cell follows proper Sudoku rules
    //          false if we do not follow the rules (no duplicates
    //          of the same number in rows, columns, and 3x3 box)
    private boolean isValid(int r, int c, int n)
    {
        // Go through whole grid and check both row and
        // column to see if any sudoku rules are violated
        for (int i = 0; i < 9; i++)
        {
            // Check at same time both row and column
            if (board[r][i] == n || board[i][c] == n)
            {
                return false;
            }
        }

        // Get the current 3x3 box
        int br = (r / 3) * 3;
        int bc = (c / 3) * 3;

        // Check the current 3x3 box for duplicates
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                if (board[br + i][bc + j] == n)
                {
                    return false;
                }
            }
        }

        // Current number follows all sudoku rules
        return true;
    }

    // isValidPreCheck()
    // @Desc: looks if current cell (not 0) with a given number
    //        follows proper sudoku format. Skips checking its
    //        own cell since a number is already in its spot.
    // @Param: r = row on the board
    //         c = column on the board
    //         n = number being compared to the row and column
    // @Return: true if current cell follows proper Sudoku rules
    //          false if we do not follow the rules (no duplicates
    //          of the same number in rows, columns, and 3x3 box)
    private boolean isValidPreCheck(int r, int c, int n)
    {
        // Go through whole grid and check both row and
        // column to see if any sudoku rules are violated
        for (int i = 0; i < 9; i++)
        {
            // Check at same time both row and column
            if ( (board[r][i] == n && i != c) ||
                    (board[i][c] == n && i != r) )
            {
                return false;
            }
        }

        // Get the current 3x3 box
        int br = (r / 3) * 3;
        int bc = (c / 3) * 3;

        // Check the current 3x3 box for duplicates
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                int rr = br + i;
                int cc = bc + j;

                if (rr == r && cc == c) continue; // skip self

                if (board[rr][cc] == n)
                {
                    return false;
                }
            }
        }

        // Current number follows all sudoku rules
        return true;
    }

    // countCandidates()
    // @Desc: Counts all possible valid options for the
    //        current empty cell and then returns total
    // @Param: r = current row of the empty cell
    //         c = current column of the empty cell
    // @Return: Sum of how many valid possiblities there are
    private int countCandidates(int r, int c)
    {
        int count = 0;
        for (int n = 1; n <= 9; n++)
        {
            if (isValid(r, c, n)) count++;
        }
        return count;
    }

    // chooseCellMRV()
    // @Desc: looks for an empty cell with the least
    //        amount of conflicts. When found the cells
    //        column and row location is returned
    // @Return: Either the next lowest conflict cell found
    //          or no spot (all spots have been filled).
    private int[] chooseCellMRV()
    {
        int bestR = -1, bestC = -1;
        int bestCount = 10;

        for (int r = 0; r < 9; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                if (board[r][c] == 0)
                {
                    int count = countCandidates(r, c);

                    if (count < bestCount)
                    {
                        bestCount = count;
                        bestR = r;
                        bestC = c;

                        // The least amount of conflicts found exit out
                        if (count == 1) return new int[]{bestR, bestC};
                    }
                }
            }
        }
        return bestR == -1 ? null : new int[]{bestR, bestC};
    }

    // solve()
    // @Desc: Recursive backtracking search that takes
    //        a spot that has the least conflicts to
    //        minimize backtracking when running trying
    //        to solve the puzzle
    // @Return: If the puzzle is solvable
    private boolean solve()
    {
        // Check to make sure no rules are broken before trying
        // to solve the puzzle
        for(int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                // Look only at non empty spots
                if(board[i][j] != 0)
                {
                    // Check to make sure current filled number
                    // does not break any rules
                    if(isValidPreCheck(i, j, board[i][j]) == false)
                    {
                        return false;
                    }
                }
            }
        }

        int[] cell = chooseCellMRV();
        if (cell == null) return true;  // solved

        int r = cell[0];
        int c = cell[1];

        for (int n = 1; n <= 9; n++)
        {
            if (isValid(r, c, n))
            {
                board[r][c] = n;

                if (solve())
                {
                    return true;
                }

                board[r][c] = 0;
            }
        }
        return false;
    }

    // callMethod()
    // @Desc: Entry point for places, calls each individual
    //        place and runs the approiate function depending
    //        on the passed in function id.
    // @Param: funcID = the function that will be called by the
    //                  current place (intialization or solve)
    //         arg = a 2d int array that sets the puzzle for the place.
    // @Return: either a solved 2d int grid or null (not solved)
    public Object callMethod(int funcID, Object arg)
    {
        switch(funcID)
        {
            // Set each individual puzzle
            case 1: return setPuzzle(arg);
            // Solve each individual puzzle
            case 2: return solve() ? (Object) board : null;
        }

        return null;
    }
}