/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.multeseo.cualquierCosa;


/**
 *
 * @author Alexander
 */
public class SearchTree {
    
    private int depth;
    private TreeNode root;    

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
    
    public void addNode(TreeNode a){
        if(a.getCostFromRoot()>this.depth) this.depth=a.getCostFromRoot();
    }
    
    public void initTree(){
        this.depth=0;
        this.root.clearChildren();
    }
    
    public SearchTree(GraphNode g){
        TreeNode node = new TreeNode(g);
        node.setParent(null);
        node.costFromRoot();
        this.depth=0;
        this.root=node;
    }            
}
