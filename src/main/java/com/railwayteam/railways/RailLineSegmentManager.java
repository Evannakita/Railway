package com.railwayteam.railways;

import com.railwayteam.railways.blocks.AbstractLargeTrackBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.ArrayList;
import java.util.HashMap;

public class RailLineSegmentManager {
  private static RailLineSegmentManager singleton;
  private static Graph segmentGraph;

  private RailLineSegmentManager () {
    segmentGraph = new Graph();
  }

  public static RailLineSegmentManager getInstance() {
    if (singleton == null) singleton = new RailLineSegmentManager();
    return singleton;
  }

  protected static int calcHash (BlockPos position) { return position.hashCode(); }

  public static int addTrack (BlockPos position) {
    // this is just to test!
    segmentGraph.addNode(position);
    return 0;
  }

  public static void removeTrack (BlockPos position) {
    if (segmentGraph.containsNode(calcHash(position))) {
      segmentGraph.removeNode(calcHash(position));
    }
  }

  public static boolean containsTrack (BlockPos position) {
    return segmentGraph.containsNode(calcHash(position));
  }

  public static int countLinks (BlockPos position) {
    return segmentGraph.getLinkedNodeIDs(calcHash(position)).size();
  }

  public static void updateTrack (BlockPos pos, ArrayList<BlockPos> adjacent) {
    if (!containsTrack(pos)) {
      segmentGraph.addNode(calcHash(pos));
    }
    adjacent.forEach(adj -> segmentGraph.addLink(calcHash(pos), calcHash(adj)));
  }

  private class GraphNode {
    int id;
    BlockPos position;
    GraphNode (int id) {
      this.id = id;
    }

    GraphNode (BlockPos pos) {
      this.position = pos;
      this.id = calcHash(pos);
    }

    @Override
    public boolean equals (Object other) {
      if (other instanceof GraphNode) {
        return this.id == ((GraphNode)other).id;
      }
      return false;
    }

    @Override
    public int hashCode() { // probably naive...
      return id;
    }
  }

  private class Graph {
    private HashMap<GraphNode, ArrayList<GraphNode>> adjacentNodes;

    Graph () {
      adjacentNodes = new HashMap<>();
    }

    void addNode (int id) { adjacentNodes.putIfAbsent(new GraphNode(id), new ArrayList<>()); }
    void addNode (BlockPos pos) { adjacentNodes.putIfAbsent(new GraphNode(pos), new ArrayList<>()); }

    void removeNode (int id) {
      GraphNode n = new GraphNode(id);
      adjacentNodes.values().stream().forEach(v -> v.remove(n));
      adjacentNodes.remove(n);
    }

    boolean containsNode (int id) {
      return adjacentNodes.containsKey(new GraphNode(id));
    }

    void addLink (int idA, int idB) {
      GraphNode a = new GraphNode(idA);
      GraphNode b = new GraphNode(idB);
      ArrayList<GraphNode> ala = adjacentNodes.get(a);
      ArrayList<GraphNode> alb = adjacentNodes.get(b);
      if (ala == null || alb == null) return; // error, can't link nonsense nodes
      adjacentNodes.get(a).add(b);
      adjacentNodes.get(b).add(a);
    }

    void removeLink (int idA, int idB) {
      GraphNode a = new GraphNode(idA);
      GraphNode b = new GraphNode(idB);
      ArrayList<GraphNode> ala = adjacentNodes.get(a);
      ArrayList<GraphNode> alb = adjacentNodes.get(b);
      if (ala == null || alb == null) return; // error, can't unlink nonsense nodes
      ala.remove(b);
      alb.remove(a);
    }

    ArrayList<Integer> getLinkedNodeIDs (int id) {
      ArrayList<Integer> ret = new ArrayList<>();
      adjacentNodes.get(new GraphNode(id)).stream().forEach(gn -> ret.add(gn.id));
      return ret;
    }
  }
}
