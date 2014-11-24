/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.examples.squares;

/**
 *
 * @author Alexander
 */
public class SquareAlex {
    public String COLOR = null;
    public boolean TOP = false;
    public boolean BOTTOM = false;
    public boolean LEFT = false;
    public boolean RIGHT = false;
    
    public SquareAlex(String color, boolean top, boolean bottom, boolean left, boolean right){
        this.BOTTOM=bottom;
        this.COLOR=color;
        this.LEFT=left;
        this.RIGHT=right;
        this.TOP=top;
    }
}
