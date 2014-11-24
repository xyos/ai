/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.squares;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import unalcol.agents.Action;
import unalcol.agents.AgentProgram;
import unalcol.agents.Percept;
import unalcol.types.collection.vector.Vector;

import java.util.Random;

/**
 * @author Jonatan
 */
public class CualquierCosaAgentProgram implements AgentProgram {
    private String color;
    private int boardSize = 0;
    private int[] nodes;
    private Vector<Integer> threeNodes;
    private SquareAlex[][] board;
    private HashMap<String, Integer> moves = new HashMap<>(boardSize*boardSize);

    public CualquierCosaAgentProgram(String color) {
        this.color = color;
    }

    @Override
    public Action compute(Percept p) {
        if (p.getAttribute(Squares.TURN).equals(color)) {
            // initializes the board
            initBoard(p);
            updateEdges(p);
            int node = 0;
            Vector<String> v = new Vector<String>();
            while (threeNodes.size() > 0) {
                int pos = new Random().nextInt(threeNodes.size());
                node = threeNodes.get(pos);
                threeNodes.remove(pos);
                v = checkPlayable(node, p);
                if (v.size() > 0) {
                    break;
                }
            }
            int i = 0, j = 0;
            if (node != 0 && v.size()!= 0) {
                i = node / boardSize;
                j = node % boardSize;
            } else {
                while (v.size() == 0) {
                    i = (int) (boardSize * Math.random());
                    j = (int) (boardSize * Math.random());
                    if (p.getAttribute(i + ":" + j + ":" + Squares.LEFT).equals(Squares.FALSE))
                        v.add(Squares.LEFT);
                    if (p.getAttribute(i + ":" + j + ":" + Squares.TOP).equals(Squares.FALSE))
                        v.add(Squares.TOP);
                    if (p.getAttribute(i + ":" + j + ":" + Squares.BOTTOM).equals(Squares.FALSE))
                        v.add(Squares.BOTTOM);
                    if (p.getAttribute(i + ":" + j + ":" + Squares.RIGHT).equals(Squares.FALSE))
                        v.add(Squares.RIGHT);
                }
                
                board=new SquareAlex[boardSize][boardSize];
                for(int k=0; k<boardSize; k++){
                    for(int h=0; h<boardSize; h++){
                        board[k][h]=new SquareAlex(null, false, false, false, false);
                    }
                }
                replicatePercept(p);
                
                for(int k=0; k<boardSize; k++){
                    for(int h=0; h<boardSize; h++){
                        
                        if(board[k][h].COLOR==null){
                            SquareAlex[][] b = board.clone();                
                            b[k][h].COLOR="Mine";                            
                            if(b[k][h].TOP){
                                b[k][h].TOP=true;
                                updateBoard(b, k-1, h, true);
                                moves.put(k+":"+h+":"+Squares.TOP, alphaBeta(b,5,Integer.MIN_VALUE,Integer.MAX_VALUE,false));
                            }
                            if(b[k][h].BOTTOM){
                                b[k][h].BOTTOM=true;
                                updateBoard(b, k+1, h, true);
                                moves.put(k+":"+h+":"+Squares.BOTTOM, alphaBeta(b,5,Integer.MIN_VALUE,Integer.MAX_VALUE,false));
                            }
                            if(b[k][h].RIGHT){
                                b[k][h].RIGHT=true;
                                updateBoard(b, k, h+1, true);
                                moves.put(k+":"+h+":"+Squares.RIGHT, alphaBeta(b,5,Integer.MIN_VALUE,Integer.MAX_VALUE,false));
                            }
                            if(b[k][h].LEFT){
                                b[k][h].LEFT=true;
                                updateBoard(b, k, h-1, true);
                                moves.put(k+":"+h+":"+Squares.LEFT, alphaBeta(b,5,Integer.MIN_VALUE,Integer.MAX_VALUE,false));
                            }
                        }
                    }
                }
                String maxAction = "";
                int max = Integer.MIN_VALUE;
                Iterator it = moves.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    if((int) pairs.getValue() > max){
                        maxAction = (String) pairs.getKey();
                        max = (int) pairs.getValue();
                    }

                    it.remove(); // avoids a ConcurrentModificationException
                }
                return new Action(maxAction);
                //Obtener el mayor y retornar la acción.
                
            }
            String action = i + ":" + j + ":" + v.get((int) (Math.random() * v.size()));
            System.out.println("WHITE:" + action);
            return new Action(action);
        }
        return new Action(Squares.PASS);
    }

    private void initBoard(Percept p) {
        if (boardSize != Integer.parseInt((String) p.getAttribute(Squares.SIZE))) {
            boardSize = Integer.parseInt((String) p.getAttribute(Squares.SIZE));
            nodes = new int[boardSize * boardSize];
        }
    }

    private Vector<String> checkPlayable(int node, Percept p) {
        int i = node / boardSize;
        int j = node % boardSize;
        Vector<String> v = new Vector();
        //up
        if (threeNodes.contains(node - boardSize) && p.getAttribute(i + ":" + j + ":" + Squares.TOP).equals(Squares.FALSE)) v.add(Squares.TOP);
        //down
        if (threeNodes.contains(node + boardSize) && p.getAttribute(i + ":" + j + ":" + Squares.BOTTOM).equals(Squares.FALSE)) v.add(Squares.BOTTOM);
        //left
        if (node % boardSize != 0 && threeNodes.contains(node - 1) && p.getAttribute(i + ":" + j + ":" + Squares.LEFT).equals(Squares.FALSE)) v.add(Squares.LEFT);
        //right
        if (node % boardSize != boardSize - 1 && threeNodes.contains(node + 1) && p.getAttribute(i + ":" + j + ":" + Squares.RIGHT).equals(Squares.FALSE)) v.add(Squares.RIGHT);
        return v;
    }

    private void countOptions() { //Cuenta los cuadritos que tienen mas de 3 líneas no coloreadas.
        Vector<Integer> v = new Vector<Integer>();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] >= 3) {
                v.add(i);
            }
        }
        this.threeNodes = v;
    }

    private void updateEdges(Percept p) { // Cuenta el numero de lineas no coloreadas para cada cuadrito.
        int n = 0, c;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                c = 0;
                if (p.getAttribute(i + ":" + j + ":" + Squares.LEFT).equals(Squares.FALSE)) c++;
                if (p.getAttribute(i + ":" + j + ":" + Squares.TOP).equals(Squares.FALSE)) c++;
                if (p.getAttribute(i + ":" + j + ":" + Squares.BOTTOM).equals(Squares.FALSE)) c++;
                if (p.getAttribute(i + ":" + j + ":" + Squares.RIGHT).equals(Squares.FALSE)) c++;
                nodes[n] = c;
                n++;
            }
        }
        countOptions();
    }
    
    private int alphaBeta(SquareAlex[][] b,int depth,int alpha,int beta, boolean maximazingPlayer){
        if(depth==0||isTerminalNode(b)){
            return heuristica(b);
        }
        if(maximazingPlayer){
            for(int i=0; i<boardSize; i++){
                for(int j=0; j<boardSize; j++){
                    if(b[i][j].COLOR==null){
                        SquareAlex[][] newBoard = b.clone();
                        updateBoard(newBoard, i, j, true);
                        alpha=Math.max(alpha, alphaBeta(newBoard, depth-1, alpha, beta, false));
                        if(beta<=alpha){
                            i=boardSize;
                            j=boardSize;
                        }
                    }
                }
            }
            return alpha;
        }
        else{
            for(int i=0; i<boardSize; i++){
                for(int j=0; j<boardSize; j++){
                    if(b[i][j].COLOR==null){
                        SquareAlex[][] newBoard = b.clone();
                        updateBoard(newBoard, i, j, false);
                        beta=Math.min(beta, alphaBeta(newBoard, depth-1, alpha, beta, true));
                        if(beta<=alpha){
                            i=boardSize;
                            j=boardSize;
                        }
                    }
                }
            }
            return beta;
        }
    }
    
    private void replicatePercept(Percept p){
        for(int i=0; i<boardSize; i++){
            for(int j=0; j<boardSize; j++){
                if(p.getAttribute(i + ":" + j + ":" + Squares.COLOR).equals(this.color)) board[i][j].COLOR="Mine";
                else{
                    if(!p.getAttribute(i + ":" + j + ":" + Squares.LEFT).equals(Squares.FALSE))
                        if(!p.getAttribute(i + ":" + j + ":" + Squares.RIGHT).equals(Squares.FALSE))
                            if(!p.getAttribute(i + ":" + j + ":" + Squares.TOP).equals(Squares.FALSE))
                                if(!p.getAttribute(i + ":" + j + ":" + Squares.BOTTOM).equals(Squares.FALSE))
                                    board[i][j].COLOR="Adversary";
                }
                if(!p.getAttribute(i + ":" + j + ":" + Squares.LEFT).equals(Squares.FALSE)) board[i][j].LEFT=true;
                if(!p.getAttribute(i + ":" + j + ":" + Squares.RIGHT).equals(Squares.FALSE)) board[i][j].RIGHT=true;
                if(!p.getAttribute(i + ":" + j + ":" + Squares.TOP).equals(Squares.FALSE)) board[i][j].TOP=true;
                if(!p.getAttribute(i + ":" + j + ":" + Squares.BOTTOM).equals(Squares.FALSE)) board[i][j].BOTTOM=true;
            }
        }
    }
    
    private int heuristica(SquareAlex[][] b){
        int count =0;
        for(int i=0; i<boardSize; i++){
            for(int j=0; j<boardSize; j++){
                if(b[i][j].COLOR!=null){
                    if(b[i][j].COLOR.equals("Mine")) count++;
                }
            }
        }
        return count;
    }
    
    private void updateBoard(SquareAlex[][] b, int i, int j,boolean maxPlayer){
        if(i>=0&&i<boardSize&&j>=0&&j<boardSize){
            if(b[i][j].COLOR==null){
                
                if(maxPlayer) b[i][j].COLOR="Mine";
                else b[i][j].COLOR="Adversary";
                
                if(b[i][j].TOP){
                    b[i][j].TOP=true;
                    updateBoard(b, i-1, j, maxPlayer);
                }
                if(b[i][j].BOTTOM){
                    b[i][j].BOTTOM=true;
                    updateBoard(b, i+1, j, maxPlayer);
                }
                if(b[i][j].RIGHT){
                    b[i][j].RIGHT=true;
                    updateBoard(b, i, j+1, maxPlayer);
                }
                if(b[i][j].LEFT){
                    b[i][j].LEFT=true;
                    updateBoard(b, i, j-1, maxPlayer);
                }
            }
        }        
    }
    
    private boolean isTerminalNode(SquareAlex[][] b){
        for(int i=0; i<boardSize; i++){
            for(int j=0; j<boardSize; j++){
                if(b[i][j].COLOR==null) return false;
            }
        }
        return true;
    }
    
    @Override
    public void init() {
    }

}

