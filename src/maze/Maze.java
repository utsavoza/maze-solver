package maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Stack;

public final class Maze {

  private final int DEFAULT_SIZE = 10;
  private MazeNode[][] cells;
  private int width;
  private int height;

  public Maze() {
    initialise(DEFAULT_SIZE, DEFAULT_SIZE);
  }

  public Maze(String mazeFilePath) {
    this();
    MazeLoader.loadMaze(mazeFilePath, this);
  }

  public Maze(int width, int height) {
    initialise(width, height);
  }

  void initialise(int width, int height) {
    cells = new MazeNode[width][height];
    this.width = width;
    this.height = height;
  }

  void addNode(int row, int column) {
    cells[row][column] = new MazeNode(row, column);
  }

  void linkEdges() {
    int numRows = cells.length;
    for (int r = 0; r < numRows; r++) {
      int numCols = cells[r].length;
      for (int c = 0; c < numCols; c++) {
        if (cells[r][c] != null) {
          if (r > 0 && cells[r - 1][c] != null) {
            cells[r][c].addNeighbor(cells[r - 1][c]);
          }
          if (c > 0 && cells[r][c - 1] != null) {
            cells[r][c].addNeighbor(cells[r][c - 1]);
          }
          if (r < numRows - 1 && cells[r + 1][c] != null) {
            cells[r][c].addNeighbor(cells[r + 1][c]);
          }
          if (c < numCols - 1 && cells[r][c + 1] != null) {
            cells[r][c].addNeighbor(cells[r][c + 1]);
          }
        }
      }
    }
  }

  /** Pretty prints the maze to the console. */
  public void printMaze() {
    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        if (cells[r][c] == null) {
          System.out.print("*");
        } else {
          System.out.print(cells[r][c].getDisplayChar());
        }
      }
      System.out.println();
    }
  }

  /**
   * Behaves exactly like {@link #printMaze()}, except it returns a string instead of printing
   * the maze on the console.
   */
  public String getMazeString() {
    StringBuilder sb = new StringBuilder();
    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        if (cells[r][c] == null) {
          sb.append("*");
        } else {
          sb.append(cells[r][c].getDisplayChar());
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /** Sets the markers on the maze according to the path. */
  public void setPath(List<MazeNode> path) {
    int index = 0;
    for (MazeNode n : path) {
      if (index == 0) {
        n.setDisplayChar(MazeNode.START);
      } else if (index == path.size() - 1) {
        n.setDisplayChar(MazeNode.GOAL);
      } else {
        n.setDisplayChar(MazeNode.PATH);
      }
      index++;
    }
  }

  /** Clears the existing solution path from the maze. Reverts back to the initial maze problem. */
  public void clearPath() {
    for (int r = 0; r < cells.length; r++) {
      for (int c = 0; c < cells[r].length; c++) {
        MazeNode n = cells[r][c];
        if (n != null) {
          n.setDisplayChar(MazeNode.EMPTY);
        }
      }
    }
  }

  /**
   * Generates a DFS path for the maze problem. The method however doesn't set the markers on the
   * maze. In order to do so, {@link #setPath(List)} needs to be called explicitly.
   *
   * @param startRow the source vertex row index.
   * @param startCol the source vertex column index.
   * @param endRow the goal vertex row index.
   * @param endCol the goal vertex column index.
   * @return List of maze nodes that represents the DFS solution to the problem.
   */
  public List<MazeNode> dfs(int startRow, int startCol, int endRow, int endCol) {
    MazeNode start = cells[startRow][startCol];
    MazeNode goal = cells[endRow][endCol];

    if (start == null || goal == null) {
      System.out.println("Start or end node is null!. No path exists");
      return new LinkedList<>();
    }

    HashMap<MazeNode, MazeNode> parentMap = new HashMap<>();
    boolean found = dfsSearch(start, goal, parentMap);

    if (!found) {
      System.out.println("No path exists");
      return new LinkedList<>();
    }

    return constructPath(start, goal, parentMap);
  }

  private boolean dfsSearch(MazeNode start, MazeNode goal, HashMap<MazeNode, MazeNode> parentMap) {
    HashSet<MazeNode> visited = new HashSet<>();
    Stack<MazeNode> toExplore = new Stack<>();
    toExplore.push(start);
    boolean found = false;

    while (!toExplore.isEmpty()) {
      MazeNode current = toExplore.pop();
      if (current == goal) {
        found = true;
        break;
      }
      List<MazeNode> neighbors = current.getNeighbors();
      ListIterator<MazeNode> it = neighbors.listIterator(neighbors.size());
      while (it.hasPrevious()) {
        MazeNode next = it.previous();
        if (!visited.contains(next)) {
          visited.add(next);
          parentMap.put(next, current);
          toExplore.push(next);
        }
      }
    }
    return found;
  }

  private List<MazeNode> constructPath(MazeNode start, MazeNode goal,
      HashMap<MazeNode, MazeNode> parentMap) {
    LinkedList<MazeNode> path = new LinkedList<>();
    MazeNode current = goal;
    while (current != start) {
      path.addFirst(current);
      current = parentMap.get(current);
    }
    path.addFirst(start);
    return path;
  }

  /**
   * Generates a BFS path for the maze problem. The method however doesn't set the markers on the
   * maze. In order to do so, {@link #setPath(List)} needs to be called explicitly.
   *
   * @param startRow the source vertex row index.
   * @param startCol the source vertex column index.
   * @param endRow the goal vertex row index.
   * @param endCol the goal vertex colum index.
   * @return List of maze nodes that represents the DFS solution to the problem.
   */
  public List<MazeNode> bfs(int startRow, int startCol, int endRow, int endCol) {
    MazeNode start = cells[startRow][startCol];
    MazeNode goal = cells[endRow][endCol];

    if (start == null || goal == null) {
      System.out.println("Start or Goal node is null! No path exists.");
      return new LinkedList<>();
    }

    HashMap<MazeNode, MazeNode> parentMap = new HashMap<>();
    HashSet<MazeNode> visited = new HashSet<>();
    Queue<MazeNode> toExplore = new LinkedList<>();
    toExplore.add(start);
    boolean found = false;

    while (!toExplore.isEmpty()) {
      MazeNode current = toExplore.remove();
      if (current == goal) {
        found = true;
        break;
      }
      List<MazeNode> neighbors = current.getNeighbors();
      ListIterator<MazeNode> it = neighbors.listIterator(neighbors.size());
      while (it.hasPrevious()) {
        MazeNode next = it.previous();
        if (!visited.contains(next)) {
          visited.add(next);
          parentMap.put(next, current);
          toExplore.add(next);
        }
      }
    }

    if (!found) {
      System.out.println("No path found!");
      return new LinkedList<>();
    }

    LinkedList<MazeNode> path = new LinkedList<>();
    MazeNode current = goal;
    while (current != start) {
      path.addFirst(current);
      current = parentMap.get(current);
    }
    path.addFirst(start);
    return path;
  }
}
