/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;

import java.util.Collections;
import java.util.Stack;

/**
 *
 * @author Alexander
 */
public class TeseoSimple extends SimpleTeseoAgentProgram {
    
    /*  Con 0 avanza al frente
        Con 1 a la derecha
        Con 2 hacia atrás
        Con 3 hacia la izquierda
        Con -1 muere   
        Con 5 ejecuta la funcion para encontar el nodo de decision mas cercano
    */
    public int getWalls(boolean PF,boolean PD,boolean PI,boolean PA){
        int walls=0;
        if(PF) walls++;
        if(PD) walls++;
        if(PA) walls++;
        if(PI) walls++;
        return walls;
    }
    
    public int getIndexExploredStates(){
        if(north.equals(Compass.NORTH)) return 0;
        if(north.equals(Compass.SOUTH)) return 2;
        if(north.equals(Compass.EAST)) return 1;
        return 3;
    }
    
    public int getIndexExplorationStates2(int val){
        if(north.equals(Compass.NORTH)) return val;
        if(north.equals(Compass.SOUTH)) return (val+2)%4;
        if(north.equals(Compass.EAST)) return (val+1)%4;
        return (val+3)%4;
    }
    
    public void computeChoices(boolean PF,boolean PD,boolean PI, boolean PA, Stack<Integer> stack){
        Compass realNorth=north;
        Stack<Integer> aux = new Stack<>();
        
        if(!PF){ aux.push(0);}
        else{
            int index = getIndexExploredStates();
            actualNode.exploredStates[index]=true;
        }        
        if(!PD){ aux.push(1);}
        else{
            int index = getIndexExploredStates();
            index=(index+1)%4;
            actualNode.exploredStates[index]=true;
        }        
        if(!PI){ aux.push(3);}
        else{
            int index = getIndexExploredStates();
            index=(index+3)%4;
            actualNode.exploredStates[index]=true;
        }
        
        if(!PA){
            rotar(2);
            if(actualNode.equals(myGraph.getRoot())){
                int index = getIndexExploredStates();
                if(!knownNode(nextMove())&&!actualNode.exploredStates[index]){
                    stack.push(2);
                }
            }else{
                int index = getIndexExploredStates();
                actualNode.exploredStates[index]=true; 
            }
        } else{ actualNode.exploredStates[2]=true; }
        
        while(!aux.isEmpty()){
            north=realNorth;
            int result = aux.pop();
            rotar(result);
            if(knownNode(nextMove())){
                if(isNewNode) {
                    int index = getIndexExploredStates();
                    myGraph.SearchNode(nextMove().getX(), nextMove().getY()).addNeighbor(actualNode, 1);
                    myGraph.SearchNode(nextMove().getX(), nextMove().getY()).exploredStates[(index+2)%4]=true;
                    myGraph.SearchNode(nextMove().getX(), nextMove().getY()).calChoices();
                    actualNode.exploredStates[index]=true;
                    actualNode.calChoices();
                    //System.out.println("Nodo ("+nextMove().getX()+","+nextMove().getY()+").choices="+myGraph.SearchNode(nextMove().getX(), nextMove().getY()).getChoices());
                }
            }else{
                int index = getIndexExploredStates();
                if(!actualNode.exploredStates[index]){
                    stack.push(result);
                }
            }
        }
        north=realNorth;
    }

    public TeseoSimple() {}
    @Override
    public int accion(boolean PF, boolean PD, boolean PA, boolean PI, boolean MT,
            boolean AF, boolean AD, boolean AA, boolean AI) {
        if (MT) return -1;
        actualNode.setWalls(getWalls(PF,PD,PI,PA));
        if(actualNode.getWalls()==2) this.TwoWallsNodes.add(actualNode);
        Stack<Integer> nextMoves = new Stack<>();
        
        computeChoices(PF, PD, PI, PA, nextMoves);
        
        Collections.shuffle(nextMoves);// Decisión Aleatoria, si se comenta: forward, right, left
        Collections.shuffle(nextMoves);
        
        if (nextMoves.isEmpty()){
            return 5; // Go back to a decision node
        }
        
        int pop = nextMoves.pop();
        int index = getIndexExplorationStates2(pop);
        actualNode.exploredStates[index]=true;
        actualNode.calChoices();
        //System.out.println("Nodo ("+actualNode.getX()+","+actualNode.getY()+").choices="+actualNode.getChoices());
        return pop;
    }
    
}