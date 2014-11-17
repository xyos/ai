/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.squares;

import unalcol.agents.Agent;
import unalcol.reflect.service.ServiceProvider;
import unalcol.reflect.util.ReflectUtil;

/**
 *
 * @author Jonatan
 */
public class SquaresMain  {
  public static void main( String[] argv ){
    // Reflection
    Agent w_agent = new Agent( new CualquierCosaAgentProgram(Squares.WHITE));
    Agent b_agent =  new Agent( new DummySquaresAgentProgram( Squares.BLACK ));
    SquaresMainFrame frame = new SquaresMainFrame( w_agent, b_agent );
    frame.setVisible(true);
  }
    
}

