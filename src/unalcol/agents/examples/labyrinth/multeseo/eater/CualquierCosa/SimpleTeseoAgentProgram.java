package unalcol.agents.examples.labyrinth.multeseo.eater.CualquierCosa;

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
  private Boolean AF; //Agent in front of me
  private Boolean AD; //Agent to my right
  private Boolean AA; //Agent behind me
  private Boolean AI; //Agent to my left
  protected boolean AgentFindOtherWay=false;
  protected GraphNode AgentInThatWay=null;
  protected Stack<GraphNode> goBackSolution = new Stack<>(); //Son los nodods que debe visitar para llegar a un nodo de desición anterior
  protected LinkedList<Point> EdgeStates = new LinkedList(); //Son los nodos intermedios que hay entre dos nodos cuando se acorta el grafo
  protected ArrayList<GraphNode> TwoWallsNodes = new ArrayList<>();
  protected ArrayList<boolean[]> badFoods = new ArrayList<>();
  protected boolean[] previousFood= new boolean[4];
  protected int energyLevel=20;
  protected boolean findingFood=false;
  protected int lastFoodDistance=-5;
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
          boolean AF, boolean AD, boolean AA, boolean AI, boolean RS, boolean RColor, 
          boolean RShape, boolean RSize, boolean RWeight, int EL);
    
    public abstract int findOtherWay( boolean PF, boolean PD, boolean PA, boolean PI,
          boolean AF, boolean AD, boolean AA, boolean AI);

    /**
    * execute
    *
    * @param perception Perception
    * @return Action[]
    */
    public Action compute(Percept p) {
        boolean PF = (Boolean) p.getAttribute(language.getPercept(0)); //Pared Frente
        boolean PD = (Boolean) p.getAttribute(language.getPercept(1)); //Pared Derecha
        boolean PA = (Boolean) p.getAttribute(language.getPercept(2)); //Pared Atrás
        boolean PI = (Boolean) p.getAttribute(language.getPercept(3)); //Pared Izquierda
        boolean MT = (Boolean) p.getAttribute(language.getPercept(4)); //Treasure
        AF=false;   AD=false;   AA=false;   AI=false;                  //Other Agents
        boolean RS = (Boolean) p.getAttribute(language.getPercept(5)); //Resource
        boolean RColor = false;                                        //Resource Color
        boolean RShape = false;                                        //Resource shape
        boolean RSize = false;                                         //Resource Size
        boolean RWeight = false;                                       //Resource Weight
        int EL = (int) p.getAttribute(language.getPercept(10));        //Energy Level      
        
        if(RS){
            RColor = (boolean) p.getAttribute(language.getPercept(6));
            RShape = (boolean) p.getAttribute(language.getPercept(7));
            RSize = (boolean) p.getAttribute(language.getPercept(8));
            RWeight = (boolean) p.getAttribute(language.getPercept(9));
        }
        
        if(EL+1 < energyLevel){
            boolean[] food = new boolean[4];
            food[0]=this.previousFood[0];
            food[1]=this.previousFood[1];
            food[2]=this.previousFood[2];
            food[3]=this.previousFood[3];
            this.badFoods.add(food);
        }
        if(EL>this.energyLevel){
            this.previousNode.setGoodFood(true);
            this.lastFoodDistance=0;
        }
        
        this.energyLevel=EL;
        
        if (cmd.size() == 0) {
             /*Borra el doble slash para comentar todo este segmento ó añade un doble slash para comentarlo
            this.printNodes();
            new java.util.Scanner(System.in).nextLine(); //Sirve para ver el proceso paso paso.
            //*/
            
            System.gc();
            
            if(EL<=this.lastFoodDistance+5&&!this.findingFood){
                System.out.println("go back for food");
                this.findingFood=true;
                this.goBackSolution.clear();
                this.EdgeStates.clear();
                this.goBackForFood(actualNode);
            }
            else{
                if(AgentFindOtherWay){
                    int a = findOtherWay(PF, PD, PA, PI, AF, AD, AA, AI);
                    this.shouldIEat(RS, RColor, RShape, RSize, RWeight);
                    AgentAroundFindOtherWay(a);
                }
                else{
                    if(!EdgeStates.isEmpty()){
                        this.shouldIEat(RS, RColor, RShape, RSize, RWeight);
                        Point removed = EdgeStates.peek();
                        goTo(removed);
                    }
                    else{
                        if(!goBackSolution.isEmpty()){
                            this.shouldIEat(RS, RColor, RShape, RSize, RWeight);
                            processGoBackSolution();
                        }
                        else{

                            myGraph.reduceGraph(this.TwoWallsNodes);
                            
                            if(RS){
                                this.shouldIEat(RS, RColor, RShape, RSize, RWeight);
                                if(this.findingFood) this.findingFood=false;
                                this.previousFood[0]=RColor;
                                this.previousFood[1]=RShape;
                                this.previousFood[2]=RSize;
                                this.previousFood[3]=RWeight;                                
                            }
                            
                            int d = accion(PF, PD, PA, PI, MT, AF, AD, AA, AI , RS, RColor, RShape, RSize, RWeight, EL);
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
                this.lastFoodDistance++;
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
    
    private void goBackForFood(GraphNode root){
        ArrayList<Point> list = new ArrayList<>();
        ExpansionTreeSearch searchTree = new ExpansionTreeSearch(root, list);
        TreeNode node = searchTree.IdsFindFood(100);
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
        if(rots==0 && AF) return true;        
        if(rots==1 && AD) return true;
        if(rots==2 && AA) return true;
        if(rots==3 && AI) return true;
        return false;
    }
    
    protected ArrayList<Point> agentsPositions(){ //devuelve la lista de las posiciones donde se perciben otros agentes.
        Compass actualNorth =north;
        ArrayList<Point> points = new ArrayList<>(); 
        if(AF){
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
        }
        if(AD){
            rotate(1);
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
            north=actualNorth;
        }
        if(AA){
            rotate(2);
            GraphNode p = nextMove();
            points.add(new Point(p.getX(),p.getY()));
            north=actualNorth;
        }
        if(AI){
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
    
    protected void shouldIEat(boolean RS,boolean rColor,boolean rShape,boolean rSize,boolean rWeight){
        if(RS){
            if(actualNode.isGoodFood()){
                cmd.add(language.getAction(4));
                //cmd.add(language.getAction(4));
            }
            else{
                for(boolean[] i:this.badFoods){
                    if(i[0]==rColor&&i[1]==rShape&&i[2]==rSize&&i[3]==rWeight){
                        return;
                    }
                }
                cmd.add(language.getAction(4));
            }            
        }
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
            System.out.println("\nis there good food? "+node.isGoodFood());
            System.out.println("Choices: "+node.getChoices());
            System.out.println();
        }
    }
    
}