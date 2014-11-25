/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.labyrinth.multeseo.eater.CualquierCosa;

import java.util.Collections;
import java.util.Stack;

/**
 *
 * @author Alexander
 * Grupo 8
 */
public class TeseoCualquierCosa extends SimpleTeseoAgentProgram {
    
    /*  Con 0 avanza al frente
        Con 1 a la derecha
        Con 2 hacia atrás
        Con 3 hacia la izquierda
        Con -1 muere   
        Con 5 ejecuta la funcion para encontar el nodo de decision mas cercano
        con 6 come
    */
    private int calcRotations(Compass dir){
        int rotations=0;
        while(north!=dir){
            rotations++;
            rotate(1);
        }
        return rotations;
    }
    
    public Stack<Integer> getChoicesLeft(){
        Stack<Integer> stack = new Stack<>();
        Compass realNorth=north;
        for(int i=0; i<=3; i++){
            if(!actualNode.getExploredNeighboors(i)){
                int rots;
                switch (i){
                    case 0:
                        rots=calcRotations(Compass.NORTH);
                        break;
                    case 1:
                        rots=calcRotations(Compass.EAST);
                        break;
                    case 2:
                        rots=calcRotations(Compass.SOUTH);
                        break;
                    default:
                        rots=calcRotations(Compass.WEST);
                        break;
                }
                stack.push(rots);
                north=realNorth;
            }
        }
        return stack;
    }
    
    public Stack<Integer> analyzePerception(boolean PF,boolean PD,boolean PA, boolean PI, boolean RS){
        Stack<Integer> aux = new Stack<>();
        actualNode.setWalls(0);
        
        if(!PF){ aux.push(0); }
        else{
            int index = getIndexExploredStates(0);
            actualNode.setExploredNeighboors(index, true);
            actualNode.setWalls(actualNode.getWalls()+1);
        }        
        if(!PD){ aux.push(1); }
        else{
            int index = getIndexExploredStates(1);
            actualNode.setExploredNeighboors(index, true);            
            actualNode.setWalls(actualNode.getWalls()+1);
        }        
        if(!PI){ aux.push(3); }
        else{
            int index = getIndexExploredStates(3);
            actualNode.setExploredNeighboors(index, true);
            actualNode.setWalls(actualNode.getWalls()+1);
        }
        
        if(!PA){
            int index = getIndexExploredStates(2);
            if(actualNode.equals(myGraph.getRoot())){
                aux.push(2);
            }else{
                actualNode.setExploredNeighboors(index, true); //En este caso, quiere decir que avanzó obviamente desde un nodo conocido 
            }
        }else{
            int index = getIndexExploredStates(2);
            actualNode.setExploredNeighboors(index, true);
            actualNode.setWalls(actualNode.getWalls()+1);
        }
        if(actualNode.getWalls()==2 && !RS) this.TwoWallsNodes.add(actualNode);
        actualNode.setAlreadyExplored(true);
        return aux;
    }
    
    public Stack<Integer> computeChoices(Stack<Integer> choices, boolean AF, boolean AD, boolean AA, boolean AI){        
        Stack<Integer> moves = new Stack<>();
        Compass realNorth=north;
        while(!choices.isEmpty()){
            north=realNorth;
            int result = choices.pop();
            rotate(result);
            int index = getIndexExploredStates(0);
            if(result==2){
                if(!AA) moves.push(2);
                continue;
            }
            if(knownNode(nextMove())){
                myGraph.SearchNode(nextMove().getX(), nextMove().getY()).addNeighbor(actualNode, 1);
                myGraph.SearchNode(nextMove().getX(), nextMove().getY()).setExploredNeighboors((index+2)%4, true);
                actualNode.setExploredNeighboors(index, true);
            }else{
                boolean AgentP=false;
                if(result==0) AgentP=AF;
                if(result==1) AgentP=AD;
                if(result==3) AgentP=AI;
                if(!AgentP) moves.push(result);
            }
        }
        north=realNorth;
        return moves;
    }

    public TeseoCualquierCosa() {}
    
    @Override
    public int accion(boolean PF, boolean PD, boolean PA, boolean PI, boolean MT,
            boolean AF, boolean AD, boolean AA, boolean AI, boolean RS, boolean RColor,
            boolean RShape, boolean RSize, boolean RWeight, int EL) {
        
        if (MT) return -1;
        
        Stack<Integer> posibleMoves;
        
        if(!actualNode.isAlreadyExplored()){
            posibleMoves=analyzePerception(PF, PD, PA, PI, RS);
        }else{
            posibleMoves=getChoicesLeft();
        }
        
        Stack<Integer> nextMoves = computeChoices(posibleMoves, AF, AD, AA, AI);        
        Collections.shuffle(nextMoves);// Decisión Aleatoria, si se comenta: forward, right, left
        
        if (nextMoves.isEmpty()){
            return 5; // Go back to a decision node
        }
        
        return nextMoves.pop();
    }

    @Override
    public int findOtherWay(boolean PF, boolean PD, boolean PA, boolean PI, boolean AF, boolean AD, boolean AA, boolean AI) {
        if(!PF && !AF) return 0;
        if(!PD && !AD) return 1;        
        if(!PI && !AI) return 3;        
        if(!PA && !AA) return 2;
        return 5;
    }    
}