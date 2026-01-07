package edu.uw.bothell.css.dsl.mass.apps.mass_sudoku;

import java.util.*;

import java.io.*;

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

// File Name: MassSudoku.java

// Description: The main file for running the sudoku
// parallel probelm solver. The user passes in the file path to
// the repository that holds all the sudoku puzzles.

public class MassSudoku
{
    // readSudokuFile
    // @Desc: Reads a given file with a 9x9 sudoku matrix using spaces
    //        as delimiters for each cell.
    // @Param: file = the current puzzle that is being read in
    // @Return: the puzzle read from file in a 2D array.
    public static int[][] readSudokuFile(File file) throws IOException
    {
        int SIZE = 9;

        int[][] grid = new int[SIZE][SIZE];

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < SIZE)
            {
                String[] parts = line.trim().split("\\s+");

                if (parts.length != SIZE) {
                    throw new IllegalArgumentException("Invalid line length at row " + row + " in " + file.getName());
                }

                for (int col = 0; col < SIZE; col++)
                {
                    grid[row][col] = Integer.parseInt(parts[col]);
                }
                row++;
            }

            if (row != SIZE) {
                throw new IllegalArgumentException("File does not contain 9 rows: " + file.getName());
            }
        }

        return grid;
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Need a single directory path");
            return;
        }

        // Get file directory from arguements
        File dir = new File(args[0]);
        if (!dir.isDirectory())
        {
            System.out.println("Error: " + args[0] + " is not a directory.");
            return;
        }

        // Get list of txt files from directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0)
        {
            System.out.println("No .txt files found in directory.");
            return;
        }

        // Map file name to content in file
        int[][][] sudParm = new int[files.length][9][9];
        List<String> curFNames = new ArrayList<>();
        int parI = 0;

        System.out.println("Loading files from directory " + args[0]);
        for (File file : files)
        {
            try
            {
                int[][] grid = readSudokuFile(file);
                String fName = file.getName();

                sudParm[parI] = grid;
                curFNames.add(fName);

                parI += 1;
            }
            catch (Exception e)
            {
                System.out.println("Failed to read " + file.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("Loaded " + sudParm.length + " sudoku puzzles.");

        // now start a timer
        System.out.println( "Timer got started." );
        long startTime = System.currentTimeMillis();

        System.out.println( "Starting MASS." );

        //MASS.setLoggingLevel(LogLevel.DEBUG);
        MASS.init();

        System.out.println( "Finished init on MASS." );

        // Generate the sudoku places equivalent to how
        // many puzzles there are to solve
        Places sudPlace = new Places(1, SudokuPlaces.class.getName(),
                null, sudParm.length);

        System.out.println( "Filling each place with a puzzle to solve." );

        // Fill each place with new puzzle
        sudPlace.callAll(1, (Object[])sudParm);

        System.out.println( "Calling Places to solve sudoku." );

        Object[] results;
        // Run solve function on sudoku places in parallel
        results = sudPlace.callAll(2, (Object[])null);

        // stop the timer
        long endTime = System.currentTimeMillis();
        long elapsedTime = (endTime - startTime);
        System.out.println("Elapsed Time: " + elapsedTime);

        String solvedContent = "Solved: ";
        String unsolvedContent = "Unsolved: ";

        // Format puzzles solved/unsolved content
        // that will be output to file
        for(int i = 0; i < results.length; i++)
        {
            int[][] curArg = (int[][])results[i];
            // List only the name of the file if unsolvable
            if(curArg == null)
            {
                unsolvedContent = unsolvedContent.concat(curFNames.get(i));
                unsolvedContent = unsolvedContent.concat(", ");
            }
            // List both the file name and answer
            else
            {
                // Write file name
                solvedContent = solvedContent.concat(curFNames.get(i));
                solvedContent = solvedContent.concat(" ");

                //System.out.println("Solved " + curArg.fileName);

                // Write the first solution found
                for(int j = 0; j < curArg.length; j++)
                {
                    for(int k = 0; k < curArg[j].length; k++)
                    {
                        solvedContent = solvedContent.concat(String.valueOf(curArg[j][k]));
                    }
                }
                // Get ready for next entry
                solvedContent = solvedContent.concat(", ");
            }
        }

        String fileWriteName = "output.txt";
        // Write all content to output.txt for readability
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileWriteName)))
        {
            // Write contents solved & unsolved contents to file
            writer.println(solvedContent);
            writer.println(unsolvedContent);

            System.out.println("String successfully written to " + fileWriteName);
        }
        catch (IOException e)
        {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        MASS.finish();
    }
}