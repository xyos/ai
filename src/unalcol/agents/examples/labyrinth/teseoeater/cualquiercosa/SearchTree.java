/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.teseoeater.cualquiercosa;

import java.awt.Point;
import java.util.HashMap;


/**
 *
 * @author Alexander
 */
public class SearchTree {
    
    private int depth;
    private TreeNode root;
    private final HashMap<Point,Object> nodeMap;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }
    
    public boolean isKwownNode(GraphNode node){
        return this.nodeMap.containsKey(new Point(node.getX(), node.getY()));
    }
    
    public void addNode(TreeNode a){
        if(a.getCostFromRoot()>this.depth) this.depth=a.getCostFromRoot();
        this.nodeMap.put(new Point(a.getMyGraphNode().getX(), a.getMyGraphNode().getY()), null);
    }
    
    public void initTree(){
        this.depth=0;
        this.root.clearChildren();
        this.nodeMap.clear();
        nodeMap.put(new Point(root.getMyGraphNode().getX(), root.getMyGraphNode().getY()), null);
    }
    
    public SearchTree(GraphNode g){
        TreeNode node = new TreeNode(g);
        node.setParent(null);
        node.calcCostFromRoot();
        this.depth=0;
        this.root=node;
        this.nodeMap= new HashMap<>(150);
        nodeMap.put(new Point(root.getMyGraphNode().getX(), root.getMyGraphNode().getY()), null);
    }            
}
