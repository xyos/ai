/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;


import java.util.ArrayList;

/**
 *
 * @author Alexander
 */
public class TreeNode{
    
    private int id;
    private ArrayList<Edge> children;
    private TreeNode parent;
    private int costFromRoot;
    private GraphNode myGraphNode;

    public GraphNode getMyGraphNode() {
        return myGraphNode;
    }

    public void setMyGraphNode(GraphNode myGraphNode) {
        this.myGraphNode = myGraphNode;
    }
    
    public int getCostFromRoot() {
        return costFromRoot;
    }

    public void setCostFromRoot(int costFromRoot) {
        this.costFromRoot = costFromRoot;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public ArrayList<Edge> getChildren() {
        return children;
    }

    public void setEdges(ArrayList<Edge> children) {
        this.children = children;
    }
    
    public void setId(int id){
        this.id=id;
    }    

    public int getId() {
        return id;
    }
    
    public void addChild(TreeNode a, int cost){
        children.add(new Edge(a, cost));
    }
    
    public void removeChild(TreeNode a){
        for(Edge e:children){
            if(e.getTNode().equals(a)){
                children.remove(e);
                break;
            }
        }
    }
    
    public void costFromRoot(){
        int cost=0;
        if(this.parent==null) {
            this.setCostFromRoot(0);
        }else{
            for(Edge e:this.parent.children){
                if(e.getTNode().equals(this)) cost=e.getEdgeCost();
            }
            this.costFromRoot = this.parent.costFromRoot + cost;
        }
    }
    public TreeNode(int id, GraphNode g){
        this.myGraphNode=g;
        this.id=id;
        this.children = new ArrayList();
    }
    
    public TreeNode(int id, TreeNode parent, GraphNode g){
        this.myGraphNode=g;
        this.id=id;
        this.children = new ArrayList();
        this.parent=parent;
    }
    
    public TreeNode(){
        this.children = new ArrayList();
    }
}
