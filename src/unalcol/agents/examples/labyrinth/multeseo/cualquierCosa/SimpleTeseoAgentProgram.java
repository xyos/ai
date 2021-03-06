package unalcol.agents.examples.labyrinth.multeseo.cualquierCosa;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
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
  protected Vector<String> cmd = new Vector<>();
  protected Graph myGraph = new Graph();
  protected GraphNode actualNode = myGraph.getRoot(); //La pocisión actual en el grafo
  protected GraphNode previousNode = myGraph.getRoot(); //La pocisión inmediatamente anterior a actualNode
  public enum Compass {NORTH, EAST, SOUTH, WEST}
  protected Compass north = Compass.NORTH;
  private Boolean globalAF;
  private Boolean globalAD;
  private Boolean globalAA;
  private Boolean globalAI;
  protected boolean AgentFindOtherWay=false;
  protected GraphNode AgentInThatWay=null;
  protected Stack<GraphNode> goBackSolution = new Stack<>(); //Son los nodods que debe visitar para llegar a un nodo de desición anterior
  protected LinkedList<Point> EdgeStates = new LinkedList(); //Son los nodos intermedios que hay entre dos nodos cuando se acorta el grafo
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
  
  public Boolean knownNode(GraphNode a){  //Comprueba si un nuevo nodo ya hace parte del grafo
      return myGraph.getNodes().containsKey(new Point(a.getX(),a.getY()));
  }
  
  public int getIndexExploredStates(int val){
      if(north.equals(Compass.NORTH)) return (0+val)%4;
      if(north.equals(Compass.EAST)) return (1+val)%4;
      if(north.equals(Compass.SOUTH)) return (2+val)%4;
      return (3+val)%4;
  }

    public abstract int accion( boolean PF, boolean PD, boolean PA, boolean PI, boolean MT,
          boolean AF, boolean AD, boolean AA, boolean AI);
    
    public abstract int findOtherWay( boolean PF, boolean PD, boolean PA, boolean PI,
          boolean AF, boolean AD, boolean AA, boolean AI);

    /**
    * execute
    *
    * @param perception Perception
    * @return Action[]
    */
    public Action compute(Percept p) {
        boolean PF = (Boolean) p.getAttribute(language.getPercept(0));
        boolean PD = (Boolean) p.getAttribute(language.getPercept(1));
        boolean PA = (Boolean) p.getAttribute(language.getPercept(2));
        boolean PI = (Boolean) p.getAttribute(language.getPercept(3));
        boolean MT = (Boolean) p.getAttribute(language.getPercept(4));
        boolean AF = (Boolean) p.getAttribute(language.getPercept(5));
        boolean AD = (Boolean) p.getAttribute(language.getPercept(6));
        boolean AA = (Boolean) p.getAttribute(language.getPercept(7));
        boolean AI = (Boolean) p.getAttribute(language.getPercept(8));
        globalAF=AF;
        globalAD=AD;
        globalAA=AA;
        globalAI=AI;
        
        if (cmd.size() == 0) {
            /*  Borra el doble slash para comentar todo este segmento ó añade un doble slash para comentarlo
            System.out.println("---------------\nPocisión: "+actualNode.getX()+","+actualNode.getY()+"\nBrújula: "+north);
            System.out.println("AlreadyExplored: "+actualNode.isAlreadyExplored());
            System.out.println("ExploredStates[0,1,2,3]=["+actualNode.getExploredNeighboors(0)+","+
                    actualNode.getExploredNeighboors(1)+","+actualNode.getExploredNeighboors(2)+","+
                    actualNode.getExploredNeighboors(3)+"]");
            System.out.println("PreviousNode("+previousNode.getX()+","+previousNode.getY()+")");
            System.out.println("---------------"); printNodes();
            System.out.println("---------------");
            new java.util.Scanner(System.in).nextLine(); //Sirve para ver el proceso paso paso.
            //*/
            
            System.gc();
            
            if(AgentFindOtherWay){
                int a = findOtherWay(PF, PD, PA, PI, AF, AD, AA, AI);
                AgentAroundFindOtherWay(a);
            }
            else{
                if(!EdgeStates.isEmpty()){
                    Point removed = EdgeStates.peek();
                    goTo(removed);
                }
                else{
                    if(!goBackSolution.isEmpty()){
                        processGoBackSolution();
                    }
                    else{
                        
                        myGraph.reduceGraph(this.TwoWallsNodes);
                        int d = accion(PF, PD, PA, PI, MT, AF, AD, AA, AI);
                        if (0 <= d && d < 4) {
                            for (int i = 1; i <= d; i++) {
                                cmd.add(language.getAction(3)); //rotate
                                rotate(1);
                            }
                            int index = getIndexExploredStates(0);
                            actualNode.setExploredNeighboors(index, true);
                            cmd.add(language.getAction(2)); // advance
                            
                        } else {
                            if (d == 5) {
                                myGraph.reduceGraph(this.TwoWallsNodes);
                                if(AgentInThatWay!=null){
                                    ArrayList<Point> list = agentsPositions();
                                    Point newPoint = findNeighbor(AgentInThatWay);
                                    AgentInThatWay=null;
                                    if(newPoint!=null) list.add(newPoint);
                                    goBackDecisionNode(actualNode, list);
                                }else{
                                    goBackDecisionNode(actualNode, agentsPositions());
                                }
                            }
                            if (d == -1) {
                                cmd.add(language.getAction(1)); // die
                            }
                        }
                    }
                }
            }            
        }
        String x = cmd.get(0);
        if (x.equals(language.getAction(1))) {
            this.stop = System.currentTimeMillis();
            System.out.println("\nEl agente ha muerto\n----------------------------------------------");
            System.out.println("| Número de nodos: " + myGraph.getNodes().size() + " | Tiempo: " + 
                    (this.stop - this.myGraph.start) / 1000 + " segundos |");
            System.out.println("----------------------------------------------");
        }
        if(x.equals(language.getAction(2))){
            if(!AF){
                if(this.AgentFindOtherWay){
                    GraphNode auxNode=nextMove();
                    actualNode=myGraph.SearchNode(auxNode.getX(),auxNode.getY());
                    if(actualNode==null){
                        actualNode = auxNode;
                    }else{
                        AgentFindOtherWay=false;
                        AgentInThatWay= previousNode;
                    }
                }else{
                    if(!EdgeStates.isEmpty()){
                        EdgeStates.poll();
                    }else{
                        if(!goBackSolution.isEmpty()){
                            goBackSolution.pop();
                        }else{
                            GraphNode newNode = nextMove();
                            actualNode.addNeighbor(newNode, 1);
                            actualNode = newNode;
                            myGraph.addNode(actualNode);
                        }
                    }
                }
                previousNode=actualNode;
            }else{
                if(AgentFindOtherWay){
                    
                }else{
                    if(!EdgeStates.isEmpty() || !goBackSolution.isEmpty()){
                        actualNode=previousNode;
                    }else{
                        int index = getIndexExploredStates(0);
                        actualNode.setExploredNeighboors(index, false);
                    }
                }
                cmd.remove(0);
                return new Action(language.getAction(0));
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
  
    private void AgentAroundFindOtherWay(int a){
        if(a==5){
            cmd.add(language.getAction(0)); //no-op
        }else{
            for (int i = 1; i <= a; i++) {
                cmd.add(language.getAction(3)); //rotate
                rotate(1);
            }
            cmd.add(language.getAction(2)); // advance
        }
    }
    
    private void goBackDecisionNode(GraphNode root, ArrayList<Point> list) {
        ExpansionTreeSearch searchTree = new ExpansionTreeSearch(root, list);
        TreeNode node = searchTree.Ids(100);
        if(node==null){
            //System.out.println("\nLa expansión no encontro ningún nodo");
            cmd.add(language.getAction(0)); // no-op
            return;
        }
        while(node.getParent()!=null){
            goBackSolution.push(node.getMyGraphNode());
            node=node.getParent();
        }
        processGoBackSolution();
    }
    
    private void processGoBackSolution(){ //Procesa un item del stack goBackSolution
        GraphNode gNode = goBackSolution.peek();
        Edge edge = actualNode.searchEdge(gNode);
        if (edge.getStates().isEmpty()){
            Point point = new Point(gNode.getX(),gNode.getY());
            goTo(point);
        }else{
            goBackSolution.pop();
            for(Point point:edge.getStates()){
                EdgeStates.add(point);
            }
            Point point = new Point(gNode.getX(),gNode.getY()); //Añade el nodo gNode a la queue, ya que no esta en los States del edge
            EdgeStates.add(point);

            Point removed = EdgeStates.peek();
            goTo(removed);
        }
    }
    
    private void goTo(Point p){
        int dx= p.x-actualNode.getX();
        int dy= p.y-actualNode.getY();
        int rots=0; //Number of 'rotations'
        
        if (dx==0&&dy==0) return;        
        if (dx==1) rots=rotateTo(Compass.EAST);
        if (dx==-1) rots=rotateTo(Compass.WEST);
        if (dy==1) rots=rotateTo(Compass.NORTH);
        if (dy==-1) rots=rotateTo(Compass.SOUTH);
        
        if(watchForAgents(rots)){
            goBackSolution.clear();
            EdgeStates.clear();
            if(!knownNode(actualNode)){
                AgentFindOtherWay=true;
                cmd.add(language.getAction(0));
            }else{
                cmd.add(language.getAction(0));
            }
        }else{
            cmd.add(language.getAction(2));
            actualNode=myGraph.SearchNode(p.x, p.y);
            if(actualNode==null) actualNode = new GraphNode(p.x, p.y);
        }
    }
    
    protected int rotateTo(Compass dir){  //Rota hasta encontrar un punto cardinal (Añade acciones a la lista cmd)
        int rotations=0;
        while(north!=dir){
            rotations++;
            rotate(1);
            cmd.add(language.getAction(3));
        }
        return rotations;
    }
    
    protected void rotate(int times){
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
    
    protected GraphNode nextMove(){ //Crea un nuevo nodo con la pocisión x,y siguiente hacia donde apunta el Compass(Brújula)
        GraphNode a = new GraphNode(actualNode.getX(), actualNode.getY());
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
    
    protected boolean watchForAgents(int rots){
        if(rots==0 && globalAF) return true;        
        if(rots==1 && globalAD) return true;
        if(rots==2 && globalAA) return true;
        if(rots==3 && globalAI) return true;
        return false;
    }
    
    protected ArrayList<Point> agentsPositions(){ //devuelve la lista de las posiciones donde se perciben otros agentes.
        Compass actualNorth =north;
        ArrayList<Point> points = new ArrayList<>(); 
        if(globalAF){
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
        }
        if(globalAD){
            rotate(1);
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
            north=actualNorth;
        }
        if(globalAA){
            rotate(2);
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
            north=actualNorth;
        }
        if(globalAI){
            rotate(3);
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
            north=actualNorth;
        }
        return points;
    }
    
    protected Point findNeighbor(GraphNode state){  //Encuentra el el vecino donde posiblemente hay un agente, que viene del procedimiento AgentFindOtherWay
        for(Edge neighbor:actualNode.getNeighbors()){
            if(!neighbor.getStates().isEmpty()){
                Point auxState = neighbor.getStates().get(0);
                if(auxState.x==state.getX()&&auxState.y==state.getY()){
                    return new Point(neighbor.getGNode().getX(), neighbor.getGNode().getY());
                }
            }
        }
        return null;
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
            System.out.println("\nExploredStates[0,1,2,3]=["+node.getExploredNeighboors(0)+","+node.getExploredNeighboors(1)+
                    ","+node.getExploredNeighboors(2)+","+node.getExploredNeighboors(3)+"]");
            System.out.println("AlreadyExplored: "+node.isAlreadyExplored());
            System.out.println("Choices: "+node.getChoices());
            System.out.println();
        }
    }
    
}