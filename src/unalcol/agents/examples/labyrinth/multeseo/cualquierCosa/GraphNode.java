/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.multeseo.cualquierCosa;


import java.util.ArrayList;

/**
 *
 * @author Alexander
 */
public class GraphNode {
    private ArrayList<Edge> neighbors;
    private int x;
    private int y;
    private int choices;
    private int walls;
    private boolean [] exploredNeighboors;
    private boolean alreadyExplored;

    public boolean getExploredNeighboors(int i) {
        return exploredNeighboors[i];
    }

    public void setExploredNeighboors(int i, boolean val) {
        this.exploredNeighboors[i]=val;
    }

    public boolean isAlreadyExplored() {
        return alreadyExplored;
    }

    public void setAlreadyExplored(boolean alreadyExplored) {
        this.alreadyExplored = alreadyExplored;
    }
    
    public int getWalls() {
        return walls;
    }

    public void setWalls(int walls) {
        this.walls = walls;
    }

    public int getChoices() {
        this.calChoices();
        return choices;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }
    
    public void setEdges(ArrayList<Edge> neighbors) {
        this.neighbors = neighbors;
    }
    
    public void calChoices(){
        int val=0;
        if(!this.exploredNeighboors[0]) val++;
        if(!this.exploredNeighboors[1]) val++;
        if(!this.exploredNeighboors[2]) val++;
        if(!this.exploredNeighboors[3]) val++;
        this.choices=val;
    }
    
    public void addNeighbor(GraphNode a, int cost){
        this.addEdge(new Edge(a, cost));
        a.addEdge(new Edge(this, cost));
    }
    
    public void addEdge(Edge e){
        if(!e.getGNode().equals(this)){
            addBestRoute(e);
        }        
    }
    
    public void removeEdge(Edge e){
        this.neighbors.remove(e);
    }
    
    public Edge searchEdge(GraphNode a){
        for(Edge e:this.neighbors){
            if(a.equals(e.getGNode())) return e;
        }return null;
    }
    
    public void removeNeighbor(GraphNode a){
        for(Edge e:neighbors){
            if(e.getGNode().equals(a)){
                neighbors.remove(e);
                break;
            }
        }
    }
    
    public void addBestRoute(Edge edge){   //Revisa si una ruta es unica; borra la ruta mas costosa hacia cierto nodo
        for(Edge e: this.getNeighbors()){
            if(edge.getGNode().equals(e.getGNode())){
                if(edge.getEdgeCost()<e.getEdgeCost()){
                    this.removeEdge(e);
                    this.neighbors.add(edge);
                    return;
                }
            }
        }this.neighbors.add(edge);
    }
    
    public GraphNode(int x, int y){
        this.neighbors = new ArrayList<>();
        this.x=x;
        this.y=y;
        this.walls=0;
        this.alreadyExplored=false;
        this.exploredNeighboors= new boolean[4];
        this.exploredNeighboors[0]=false;
        this.exploredNeighboors[1]=false;
        this.exploredNeighboors[2]=false;
        this.exploredNeighboors[3]=false;
    }
       
    public GraphNode(int x, int y, ArrayList<Edge> children, int choices){
        this.x=x;
        this.y=y;
        this.neighbors=children;
        this.choices=choices;
        this.walls=0;
        this.alreadyExplored=false;
        this.exploredNeighboors = new boolean[4];
        this.exploredNeighboors[0]=false;
        this.exploredNeighboors[1]=false;
        this.exploredNeighboors[2]=false;
        this.exploredNeighboors[3]=false;
    }
}
