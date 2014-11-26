package unalcol.agents.examples.labyrinth.teseoeater;
import unalcol.agents.Agent;

import unalcol.agents.examples.labyrinth.*;
import unalcol.agents.examples.labyrinth.teseoeater.cualquiercosa.TeseoCualquierCosa;
import unalcol.agents.simulate.util.*;

public class TeseoEaterMain {
  private static SimpleLanguage getLanguage(){
    return new TeseoEaterLanguage(
      new String[]{"front", "right", "back", "left", "treasure",
                   "resource", "resource-color", "resource-shape", "resource-size", "resource-weight", "resource-type", "energy_level" },
      new String[]{"no_op", "die", "advance", "rotate", "eat"}
      );
  }

  public static void main( String[] argv ){
    Agent agent = new Agent( new InteractiveAgentProgram( getLanguage() ) );
    
    TeseoCualquierCosa cualquierCosaAgent = new TeseoCualquierCosa();
    cualquierCosaAgent.setLanguage(getLanguage());
    Agent cualquierCosa = new Agent (cualquierCosaAgent);
    
    TeseoEaterMainFrame frame = new TeseoEaterMainFrame( cualquierCosa, getLanguage() );
    LabyrinthDrawer.DRAW_AREA_SIZE = 600;
    LabyrinthDrawer.CELL_SIZE = 40;
    Labyrinth.DEFAULT_SIZE = 15;
    frame.setVisible(true);
  }
}
