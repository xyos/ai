/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.squares;

import unalcol.agents.Action;
import unalcol.agents.AgentProgram;
import unalcol.agents.Percept;
import unalcol.types.collection.vector.Vector;

/**
 *
 * @author Jonatan
 */
public class DummySquaresAgentProgram implements AgentProgram {
    protected String color;
    public DummySquaresAgentProgram( String color ){
        this.color = color;        
    }
    
    @Override
    public Action compute(Percept p) {
        if( p.getAttribute(Squares.TURN).equals(color) ){
            int size = Integer.parseInt((String)p.getAttribute(Squares.SIZE));
            int i = 0;
            int j = 0;
            Vector<String> v = new Vector();
            while(v.size()==0){
              i = (int)(size*Math.random());
              j = (int)(size*Math.random());
              if(p.getAttribute(i+":"+j+":"+Squares.LEFT).equals(Squares.FALSE))
                v.add(Squares.LEFT);
              if(p.getAttribute(i+":"+j+":"+Squares.TOP).equals(Squares.FALSE))
                v.add(Squares.TOP);
              if(p.getAttribute(i+":"+j+":"+Squares.BOTTOM).equals(Squares.FALSE))
                v.add(Squares.BOTTOM);
              if(p.getAttribute(i+":"+j+":"+Squares.RIGHT).equals(Squares.FALSE))
                v.add(Squares.RIGHT);
            }
            String action = i+":"+j+":"+v.get((int)(Math.random()*v.size()));
            System.out.println("BLACK:" + action);
            return new Action( action );
        }
        return new Action(Squares.PASS);
    }

    @Override
    public void init() {
    }
    
}
