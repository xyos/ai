/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.multeseo.cualquierCosa;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Alexander
 */
public class Graph {
    private HashMap<Point,GraphNode> nodes;
    private GraphNode root;
    public long start;
    
    public Graph(){
        this.nodes= new HashMap<>(200);
        GraphNode a = new GraphNode(0,0);
        this.nodes.put(new Point(0,0), a);
        this.root=a;
        this.start=System.currentTimeMillis();
    }

    public HashMap<Point, GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<Point, GraphNode> nodes) {
        this.nodes = nodes;
    }

    public GraphNode getRoot() {
        return root;
    }
    
    public void reduceGraph(ArrayList<GraphNode> twoWallsNodes){        
        if(twoWallsNodes.isEmpty()) return;
        
        ArrayList<GraphNode> nodeList = new ArrayList<>();
        nodeList.addAll(twoWallsNodes);
        for(GraphNode node:nodeList){
            if(node.getNeighbors().size()==2){
                Edge a = node.getNeighbors().get(0);
                Edge b = node.getNeighbors().get(1);
                int edgeCost = a.getStates().size()+b.getStates().size()+2;

                Edge ab = new Edge(b.getGNode(),edgeCost);
                ArrayList<Point> ABStates = new ArrayList<>();
                if(!a.getStates().isEmpty()){
                    ABStates.addAll(a.getStates());
                    Collections.reverse(ABStates);
                }                
                ABStates.add(new Point(node.getX(),node.getY()));
                if(!b.getStates().isEmpty()){
                    ABStates.addAll(b.getStates());
                }
                ab.setStates(ABStates);

                Edge ba = new Edge(a.getGNode(),edgeCost);
                ArrayList<Point> BAStates = new ArrayList<>();
                BAStates.addAll(ABStates);
                Collections.reverse(BAStates);
                ba.setStates(BAStates);

                a.getGNode().addEdge(ab);
                b.getGNode().addEdge(ba);
                a.getGNode().removeNeighbor(node);
                b.getGNode().removeNeighbor(node);
                twoWallsNodes.remove(node);
                eraseNode(node);
            }
        }
    }
    
    public void addNode(GraphNode a){
        this.nodes.put(new Point(a.getX(),a.getY()), a);
    }
    
    public GraphNode SearchNode(int x, int y){
        return nodes.get(new Point(x,y));
    }
    
    public void removeNode(GraphNode a){        
        for(Edge e:a.getNeighbors()){
            for(Edge edge:e.getGNode().getNeighbors()){
                if(edge.getGNode().equals(a)) e.getGNode().removeNeighbor(a);
            }
        }
        this.nodes.remove(new Point(a.getX(),a.getX()));
    }
    
    public void eraseNode(GraphNode a){
        this.nodes.remove(new Point(a.getX(),a.getY()));
    }
    
}
