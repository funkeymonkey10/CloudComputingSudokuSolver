# CloudComputingSudokuSolver

**Masters Project For CSS 534: Parallel Computing in the Grid and Cloud**

**Programming Language:** Java

**Parallel Computing API:** MASS

**Environment:** Linux Operating System, Maven for Packaging Application

**Short Description:**

This project showcases a parallel computing approach to solving a large collection of Sudoku puzzles. The solution is implemented in Java using the MASS API and runs across multiple computers, with a maximum of four computing nodes executing concurrently.

**Detailed Implmentation:**

The strategy used to solve the Sudoku problems follows a divide-and-conquer approach, distributing puzzles across multiple machine nodes managed by MASS. Each Sudoku puzzle is represented as a MASS Place, and puzzles are solved in parallel across the available computing nodes. For example, when solving 10,000 puzzles using four computing nodes, each node is assigned 2,500 puzzles to process. Each Place is then solved concurrently based on the number of computing nodes in use.

Each Sudoku puzzle is solved using a backtracking algorithm enhanced with the Minimum Remaining Values (MRV) heuristic to reduce unnecessary search. The algorithm selects the empty cell with the fewest valid candidate values, assigns a possible number, and recursively continues. If any Sudoku constraints are violated—row, column, or 3×3 subgrid—the algorithm backtracks and tries an alternative value. Once a puzzle is solved, the completed grid along with its associated filename is returned and stored in an object array. If the puzzle cannot be solved, a null value is returned in place of the grid to indicate failure. After all puzzles have been processed, the object array is written to an output file that separates solved puzzles from unsolved ones.

<img width="1400" height="1126" alt="MRVHeuristic" src="https://github.com/user-attachments/assets/b38e4c31-ad88-4918-80d5-bca3f6cc7bb5" />

**Performance:**

When processing 10,000 Sudoku puzzles, the slowest performance occurred when the application was executed using a single computing node, with an elapsed time of 15,651 milliseconds. The fastest performance was achieved using four computing nodes, completing execution in 9,155 milliseconds. This corresponds to a speedup of approximately 1.71× compared to execution with a single node.

**Screenshot of 1 Computing Node:**

<img width="745" height="284" alt="image" src="https://github.com/user-attachments/assets/afe19a87-7260-4319-b7a0-66294018c062" />

**Screenshot of 4 Computing Nodes:**

<img width="656" height="284" alt="image" src="https://github.com/user-attachments/assets/0686a349-a5c9-4577-aecb-db25146093a8" />

**Code for main:** [MassSudoku](https://github.com/funkeymonkey10/CloudComputingSudokuSolver/blob/main/MassSudoku.java)

**Code for Places Class:** [SudokuPlaces](https://github.com/funkeymonkey10/CloudComputingSudokuSolver/blob/main/SudokuPlaces.java)

**Sample Output for 100 puzzles:** [Sample txt](https://github.com/funkeymonkey10/CloudComputingSudokuSolver/blob/main/output.txt)
