/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package unalcol.agents.examples.labyrinth.multeseo.eater.CualquierCosa;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author Alexander
 */
public class ExpansionTreeSearch {
    private SearchTree tree;
    private final GraphNode root;
    private GraphNode goal;
    private TreeNode actualNode;
    private int limitDepth; //DFS & IDS
    private boolean idsNoPosibleSolution; //IDS
    private final ArrayList<Point> otherAgents;
    
    public ExpansionTreeSearch(GraphNode root, ArrayList<Point> otherAgents){
        this.root=root;
        this.tree = new SearchTree(root);
        this.otherAgents=otherAgents;
    }
    
    public final void initTree(){
        this.tree.initTree();
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
    
    public boolean isRoot(){
        return actualNode.equals(tree.getRoot());
    }
    
    public void sucesor(ArrayList<TreeNode> list){
        for(Edge e :actualNode.getMyGraphNode().getNeighbors()){
            if( isRoot() || (!tree.isKwownNode(e.getGNode()) && e.getGNode().getWalls()!=3 ) ){
                TreeNode node = new TreeNode(e.getGNode());
                node.setParent(actualNode);
                actualNode.addChild(node, e.getEdgeCost());
                node.setCostFromRoot(actualNode.getCostFromRoot()+e.getEdgeCost());
                if(node.getCostFromRoot()>this.limitDepth){
                    actualNode.removeChild(node);
                    this.idsNoPosibleSolution=false;
                }else{
                    tree.addNode(node);
                    list.add(node);
                }
            }
        }
    }
    
    public TreeNode Bfs(){ //No usar, hay que revisarlo
        initTree();
        this.limitDepth = Integer.MAX_VALUE;
        LinkedList<TreeNode> queue = new LinkedList<>();
        ArrayList<Point> agentsLocations= new ArrayList<>();
        agentsLocations.addAll(this.otherAgents);
        ArrayList<TreeNode> list = new ArrayList<>();
        queue.add(tree.getRoot());
        while(!queue.isEmpty()){
            actualNode = queue.remove();
            if(actualNode.getMyGraphNode().getChoices()>=1 && !actualNode.getMyGraphNode().equals(this.root)){
                return actualNode;
            }
            list.clear();
            sucesor(list);
            Collections.shuffle(list);
            for(TreeNode n:list){
                boolean avoidAgent=false;
                for(Point p:agentsLocations){
                    if(p.x==n.getMyGraphNode().getX()&&p.y==n.getMyGraphNode().getY()){
                        avoidAgent=true;
                        break;
                    }
                }
                if(!avoidAgent){
                    queue.add(n);
                }
            }
            agentsLocations.clear();
        }
        return null;
    }
    
    public TreeNode Ids(int lim){
        ArrayList<Point> agentsLocations= new ArrayList<>();
        this.idsNoPosibleSolution=false;
        ArrayList<TreeNode> list = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        for(int i=1;i<=lim;i++){
            System.gc();
            stack.clear();
            this.idsNoPosibleSolution=true;
            initTree();
            agentsLocations.addAll(this.otherAgents);
            this.limitDepth=i;
            stack.push(tree.getRoot());
            while(!stack.isEmpty()){
                actualNode = stack.pop();
                if(actualNode.getMyGraphNode().getChoices()>=1 && !actualNode.getMyGraphNode().equals(this.root)){
                    return actualNode;
                }
                list.clear();
                sucesor(list);
                Collections.shuffle(list);
                for(TreeNode n:list){
                    boolean avoidAgent=false;
                    for(Point p:agentsLocations){
                        if(p.x==n.getMyGraphNode().getX()&&p.y==n.getMyGraphNode().getY()){
                            avoidAgent=true;
                            break;
                        }
                    }
                    if(!avoidAgent){
                        stack.push(n);
                    }
                }
                agentsLocations.clear();
            }
            if(this.idsNoPosibleSolution){
                return null;
            }
        }
        return null;
    }
    
}
