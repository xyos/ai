package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Stack;
import unalcol.agents.AgentProgram;
import unalcol.agents.Percept;
import unalcol.agents.simulate.util.SimpleLanguage;
import unalcol.types.collection.vector.*;
import unalcol.agents.Action;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2014</p>
 *
 * <p>Company: Universidad Nacional de Colombia</p>
 *
 * @author Alexander Gonzalez
 * @version 1.0
 */
public abstract class SimpleTeseoAgentProgram  implements AgentProgram{
  protected SimpleLanguage language;
  protected Vector<String> cmd = new Vector<String>();
  protected Graph myGraph = new Graph();
  protected GraphNode actualNode =myGraph.getRoot();
  public enum Compass {NORTH, EAST, SOUTH, WEST}
  protected Compass north = Compass.NORTH;
  protected Stack<GraphNode> goBackSolution = new Stack<>();
  protected boolean isNewNode=true;
  protected boolean singlePath=false;
  protected ArrayList<GraphNode> TwoWallsNodes = new ArrayList<>();
  public long stop; // Para medir el tiempo (Start esta en el constructor de la clase Graph)
  
  public SimpleTeseoAgentProgram( ) {
  }

  public void setLanguage(  SimpleLanguage _language ){
    language = _language;
  }

  public void init(){
    cmd.clear();
  }

    public abstract int accion( boolean PF, boolean PD, boolean PA, boolean PI, boolean MT,
          boolean AF, boolean AD, boolean AA, boolean AI);

  /**
   * execute
   *
   * @param perception Perception
   * @return Action[]
   */
    public Action compute(Percept p) {
        System.gc();
        boolean PF = (Boolean) p.getAttribute(language.getPercept(0));
        boolean PD = (Boolean) p.getAttribute(language.getPercept(1));
        boolean PA = (Boolean) p.getAttribute(language.getPercept(2));
        boolean PI = (Boolean) p.getAttribute(language.getPercept(3));
        boolean MT = (Boolean) p.getAttribute(language.getPercept(4));
        boolean AF = (Boolean) p.getAttribute(language.getPercept(5));
        boolean AD = (Boolean) p.getAttribute(language.getPercept(6));
        boolean AA = (Boolean) p.getAttribute(language.getPercept(7));
        boolean AI = (Boolean) p.getAttribute(language.getPercept(8));
        if (cmd.size() == 0) {

      //System.out.println("---------------\nPocisión: "+actualNode.getX()+","+actualNode.getY()+"\nBrújula: "+norte);
            //System.out.println("ToNorth:"+actualNode.exploredStates[0]+", ToEast:"+actualNode.exploredStates[1]+", ToSouth:"+actualNode.exploredStates[2]+", ToWest:"+actualNode.exploredStates[3]);
            //System.out.println("Graph: "); printNodes();
            //System.out.println("---------------");
            //new java.util.Scanner(System.in).nextLine(); //Sirve para ver el proceso paso paso.
            int d = accion(PF, PD, PA, PI, MT, AF, AD, AA, AI);
            if (0 <= d && d < 4) {
                for (int i = 1; i <= d; i++) {
                    cmd.add(language.getAction(3)); //rotate
                    rotar(1);
                }
                cmd.add(language.getAction(2)); // advance

                GraphNode newNode = nextMove();
                if (!knownNode(newNode)) {
                    actualNode.addNeighbor(newNode, 1);
                    actualNode = newNode;
                    myGraph.addNode(actualNode);
                    isNewNode = true;
                } else {
                    actualNode = myGraph.SearchNode(actualNode.getX(), actualNode.getY());
                    isNewNode = false;
                }
                myGraph.reduceGraph(this.TwoWallsNodes);
            } else {
                if (d == 5) {
                    myGraph.reduceGraph(this.TwoWallsNodes);
                    goBackDecisionNode(actualNode);
                }
                if (d == -1) {
                    cmd.add(language.getAction(1)); // die
                }
            }
        }
        String x = cmd.get(0);
        if (x.equals(language.getAction(1))) {
            System.out.println("El agente ha muerto\n--------------------");
            this.stop = System.currentTimeMillis();
            System.out.println("| Número de nodos:" + myGraph.getNodes().size() + " | Tiempo: " + (this.stop - this.myGraph.start) / 1000 + " segundos |");
            System.out.println("---------------------------------------------");
            printNodes();
            new java.util.Scanner(System.in).nextLine();
        } else if (x.equals(language.getAction(2))) {
            if (AF) {
                System.out.println("agente al frente");
                x = language.getAction(0);
                cmd.add(x);
            }
        }
        cmd.remove(0);
        return new Action(x);
    }

  /**
   * goalAchieved
   *
   * @param perception Perception
   * @return boolean
   */
  public boolean goalAchieved( Percept p ){
    return (((Boolean)p.getAttribute(language.getPercept(4))).booleanValue());
  }
    
    private void goBackDecisionNode(GraphNode root) {
        ExpansionTreeSearch searchTree = new ExpansionTreeSearch(root);
        //System.out.println("------Expansion Search------");
        TreeNode node = searchTree.Ids(100);
        //System.out.println("----Termino la expansión----");
        if(node==null){
            //System.out.println("La expansión no encontro ningún nodo");
            cmd.add(language.getAction(0)); // die
            return;
        }
        if(node.getParent()==null) goBackSolution.push(node.getMyGraphNode());
        while(node.getParent()!=null){
            goBackSolution.push(node.getMyGraphNode());
            node=node.getParent();
        }
        //System.out.println("-----GoBackSolution-----");
        while(!goBackSolution.isEmpty()){
            GraphNode gNode = goBackSolution.pop();
            //System.out.println("ActualNode: "+actualNode.getX()+","+actualNode.getY());
            //System.out.println("nextNode: "+gNode.getX()+","+gNode.getY());
            Edge edge = actualNode.searchEdge(gNode);
            if (edge.getStates().isEmpty()){
                goTo(gNode.getX(),gNode.getY());
            }else{
                for(Point p:edge.getStates()){
                    goTo(p.x,p.y);
                    actualNode= new GraphNode(p.x,p.y);
                }
                goTo(gNode.getX(), gNode.getY());
            }
            actualNode=gNode;
        }
        isNewNode=false;
        //System.out.println("-----GoBackSolution-----");        
    }
    
    private void goTo(int x, int y){
        int dx= x-actualNode.getX();
        int dy= y-actualNode.getY();
        if (dx==0&&dy==0) return;
        if (dx==1){
            rotateTo(Compass.EAST); cmd.add(language.getAction(2));
        }
        if (dx==-1){
            rotateTo(Compass.WEST); cmd.add(language.getAction(2));
        }
        if (dy==1){
            rotateTo(Compass.NORTH); cmd.add(language.getAction(2));
        }
        if (dy==-1){
            rotateTo(Compass.SOUTH); cmd.add(language.getAction(2));
        }
    }
    
    protected void rotateTo(Compass dir){ //Rota hasta encontra un punto cardinal (Añade acciones a la lista cmd)
        while(north!=dir){
            rotar(1);
            cmd.add(language.getAction(3));
        }
    }
    
    protected void rotar(int times){
        for(int i=0; i<times;i++){
            switch(north){
            case NORTH:
                north=Compass.EAST;
                break;
            case EAST:
                north=Compass.SOUTH;
                break;
            case SOUTH:
                north=Compass.WEST;
                break;
            case WEST:
                north=Compass.NORTH;
                break;
            }
        }
    }

    public Boolean knownNode(GraphNode a){ //Comprueba si un nuevo nodo ya hace parte del grafo
        return myGraph.getNodes().containsKey(new Point(a.getX(),a.getY()));
    }

    public void printNodes(){
        for(GraphNode node:myGraph.getNodes().values()) {
            System.out.print("Node["+node.getX()+","+node.getY()+"]");
            for(Edge e:node.getNeighbors()){
                System.out.print(" Neighbor("+e.getGNode().getX()+","+e.getGNode().getY()+")");
                if(!e.getStates().isEmpty()){
                    System.out.print("{");
                    for(Point p:e.getStates()){
                        System.out.print("("+p.x+","+p.y+"),");
                    }
                    System.out.print("}");
                }
            }
            System.out.println();
        }
    }

    protected GraphNode nextMove(){
        GraphNode a = new GraphNode(actualNode.getX(),actualNode.getY());
        switch(north){
              case NORTH:
                  a.setY(a.getY()+1);
                  break;
              case SOUTH:
                  a.setY(a.getY()-1);
                  break;
              case WEST:
                  a.setX(a.getX()-1);
                  break;
              case EAST:
                  a.setX(a.getX()+1);
                  break;
        }
        return a;      
    }
}
