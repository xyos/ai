/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Alexander
 */
public class ExpansionTreeSearch {
    private SearchTree tree;
    private final GraphNode root;
    private GraphNode goal;
    private TreeNode actualNode = new TreeNode();
    private int limitDepth;
    private boolean idsNoPosibleSolution;
    
    public ExpansionTreeSearch(GraphNode root, GraphNode goal){
        this.root=root;
        newTree();
        this.goal=goal;
    }
    
    public ExpansionTreeSearch(GraphNode root){
        this.root=root;
        newTree();
    }
    
    public final void newTree(){
        this.tree=new SearchTree(root);
    }

    public SearchTree getTree() {
        return tree;
    }

    public void setTree(SearchTree tree) {
        this.tree = tree;
    }

    public GraphNode getGoal() {
        return goal;
    }

    public void setGoal(GraphNode goal) {
        this.goal = goal;
    }

    public TreeNode getActualNode() {
        return actualNode;
    }

    public void setActualNode(TreeNode actualNode) {
        this.actualNode = actualNode;
    }
    
    public boolean isRoot(){
        return actualNode.equals(tree.getRoot());
    }
    
    public boolean isPreviousState(GraphNode a){
        return (a.getX()==actualNode.getParent().getMyGraphNode().getX() && a.getY()==actualNode.getParent().getMyGraphNode().getY());
    }
    
    public TreeNode Bfs(){
        newTree();
        LinkedBlockingQueue queue = new LinkedBlockingQueue();
        queue.add(tree.getRoot());
        while(!queue.isEmpty()){
            actualNode = (TreeNode)queue.remove();
            if(actualNode.getMyGraphNode().equals(goal)) return actualNode;
            ArrayList<TreeNode> list = new ArrayList<>();            
            sucesor(list);
            Collections.shuffle(list);
            for(TreeNode n:list){
                queue.add(n);
            }
        }
        return null;
    }
    
    public void sucesor(ArrayList<TreeNode> list){
        boolean isRoot = isRoot();
        
        for(Edge e :actualNode.getMyGraphNode().getNeighbors()){
            if(isRoot|| !isPreviousState(e.getGNode())){
                TreeNode node = new TreeNode(tree.getNodeNumber(), e.getGNode());
                node.setParent(actualNode);
                actualNode.addChild(node, e.getEdgeCost());
                node.costFromRoot();
                tree.addNode(node);
                list.add(node);
            }else{
                if(!actualNode.getParent().getMyGraphNode().equals(e.getGNode())){
                    TreeNode node = new TreeNode(tree.getNodeNumber(), e.getGNode());
                    node.setParent(actualNode);
                    actualNode.addChild(node, e.getEdgeCost());
                    node.costFromRoot();
                    tree.addNode(node);
                    list.add(node);
                }
            }
        }
    }
    
    public void sucesorDFS(ArrayList<TreeNode> list){
        boolean isRoot = isRoot();
        
        for(Edge e :actualNode.getMyGraphNode().getNeighbors()){
            if( isRoot || (!isPreviousState(e.getGNode())&& e.getGNode().getWalls()!=3 ) ){
                //System.out.println(e.getGNode().getX()+", "+e.getGNode().getY());
                TreeNode node = new TreeNode(tree.getNodeNumber(), e.getGNode());
                node.setParent(actualNode);
                actualNode.addChild(node, e.getEdgeCost());
                node.setCostFromRoot(actualNode.getCostFromRoot()+e.getEdgeCost());
                if(node.getCostFromRoot()>limitDepth){
                    actualNode.removeChild(node);
                    this.idsNoPosibleSolution=false;
                }else{
                    tree.addNode(node);
                    list.add(node);
                }
            }
        }
    }
    
    public TreeNode Dfs(int limite){
        newTree();
        this.limitDepth=limite;
        Stack<TreeNode> stack = new Stack<>();
        stack.push(tree.getRoot());
        while(!stack.isEmpty()){
            actualNode = stack.pop();
            if(actualNode.getMyGraphNode().equals(goal)) return actualNode;
            ArrayList<TreeNode> list = new ArrayList<>();            
            sucesorDFS(list);
            Collections.shuffle(list);
            for(TreeNode n:list){
                stack.push(n);
            }
        }
        return null;
    }
    
    public TreeNode Ids(int lim){
        this.idsNoPosibleSolution=false;
        for(int i=1;i<=lim;i++){
            System.gc();
            this.idsNoPosibleSolution=true;
            newTree();
            this.limitDepth=i;
            Stack<TreeNode> stack = new Stack<>();
            //System.out.println("Limite "+i+":\n----------");
            stack.push(tree.getRoot());
            while(!stack.isEmpty()){
                actualNode = stack.pop();
                //System.out.println("Nodo "+actualNode.getMyGraphNode().getX()+", "+actualNode.getMyGraphNode().getY()+"; choices:"+actualNode.getMyGraphNode().getChoices());
                if(actualNode.getMyGraphNode().getChoices()>=1) return actualNode;
                ArrayList<TreeNode> list = new ArrayList<>();
                sucesorDFS(list);
                Collections.shuffle(list);
                for(TreeNode n:list){
                    stack.push(n);
                }
            }
            if(this.idsNoPosibleSolution) return null;
        }return null;
    }
    
}
