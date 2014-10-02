package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;

import java.awt.Point;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Stack;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.Viewer;
import unalcol.agents.Action;
import unalcol.agents.AgentProgram;
import unalcol.agents.Percept;
import unalcol.agents.simulate.util.SimpleLanguage;
import unalcol.types.collection.vector.Vector;

public class CualquierCosaTeseo implements AgentProgram {

    protected SimpleLanguage language;
    private final Graph map;
    private final LinkedList<String> cmd;
    private Direction direction;
    private final Stack<Point> lastDecision;
    private final Point currentNode;
    private final Vector<Point> explored;
    private final EnumMap<ENV, Boolean> envMAP;

    public void setLanguage(SimpleLanguage _language) {
        language = _language;
    }

    private void goForward() {
        if (direction == Direction.NORTH) {
            currentNode.translate(1, 0);
        } else if (direction == Direction.EAST) {
            currentNode.translate(0, 1);
        } else if (direction == Direction.WEST) {
            currentNode.translate(0, -1);
        } else {
            currentNode.translate(-1, 0);
        }
    }

    private enum Direction {

        NORTH, EAST, SOUTH, WEST;

        public Direction rotateRigth() {
            return Direction.values()[(ordinal() + 1) % 4];
        }

        public int rotationsToDir(Direction dir) {
            return (dir.ordinal() + ordinal()) % 4;
        }

        public Direction wall(ENV wall) {
            if (wall == ENV.PF) {
                return Direction.values()[ordinal()];
            } else if (wall == ENV.PD) {
                return Direction.values()[(ordinal() + 1) % 4];
            } else if (wall == ENV.PA) {
                return Direction.values()[(ordinal() + 2) % 4];
            } else {
                return Direction.values()[(ordinal() + 3) % 4];
            }
        }

        public Direction inverse() {
            return Direction.values()[(ordinal() + 2) % 4];
        }
    }

    private enum ENV {

        PF, PD, PA, PI, MT, AF, AD, AA, AI;
    }

    private ENV[] walls = {ENV.PF, ENV.PD, ENV.PA, ENV.PI};

    @Override
    public void init() {
        cmd.clear();
        map.clear();
        direction = Direction.NORTH;
        lastDecision.clear();
        currentNode.move(0, 0);
        map.addNode(currentNode.toString());
        explored.clear();
    }

    public CualquierCosaTeseo() {
        cmd = new LinkedList<>();
        map = new SingleGraph("mapa");
        direction = Direction.NORTH;
        lastDecision = new Stack<>();
        currentNode = new Point(0, 0);
        explored = new Vector<>();
        envMAP = new EnumMap<>(ENV.class);
    }

    private int nNeighbors() {
        int n = 4;
        for (ENV wall : walls) {
            if (envMAP.get(wall)) {
                n--;
            }
        }
        return n;
    }

    private Node addToGraph(Point p) {
        try {
            Node node = map.addNode(currentNode.toString());
            node.setAttribute("xyz", currentNode.x, currentNode.y, 0);
        } catch (IdAlreadyInUseException e) {// Already in graph
        }
        Node init = map.getNode(currentNode.toString());
        try {
            for (ENV wall : walls) {
                if (!envMAP.get(wall)) {
                    Direction wallDirection = direction.wall(wall);
                    Node dest = map.addNode(currentNode.toString()
                            + wallDirection.toString());
                    Edge edge = map.addEdge(currentNode.toString()
                            + wallDirection.toString(), init, dest, true);
                    edge.addAttribute("walkingIn", false);
                }
            }
            removeArrivingEdge();

        } catch (IdAlreadyInUseException | EdgeRejectedException e) {
        }
        return init;
    }

    private void checkDecision(int neighbors) {
        if (neighbors == 1) {// End node
            if (map.getNode(currentNode.toString()) == null) { // new node
                addToGraph(currentNode);
                rotate(2);
            } else { // returning to last decision node
                walk();
            }

        } else if (!lastDecision.contains(currentNode) && !explored.contains(currentNode)) { //Decision node
            if (map.getNode(currentNode.toString()) == null) { // new node
                addToGraph(currentNode);
                Edge edge = map.getEdge(currentNode.toString() + direction.toString());
                if (edge != null) {
                    walk();
                    edge.changeAttribute("walkingIn", true);
                } else {
                    rotateToNext();
                }
                Point p = new Point(
                        currentNode.x,
                        currentNode.y
                );
                lastDecision.push(p);
            } else {
            }
        } else {
            // we just rotated after adding a node or just arrived from an end node
            if (lastDecision.peek().toString().equals(currentNode.toString())) {
                rotateToNext();
                walk();
            } else { //we arrived from other path
                removeArrivingEdge();
                rotateToNext();
            }
        }
    }

    private void removeArrivingEdge() {

        // deleting unexplored edges
        Node init = map.getNode(currentNode.toString());
        if (lastDecision.size() > 0) {
            Node lastNode = map.getNode(lastDecision.peek().toString());
            Edge edge = null;
            Node dest = null;
            for (Edge e : lastNode.getEachEdge()) {
                if (e.getAttribute("walkingIn") != null ? (boolean) e.getAttribute("walkingIn") : false) {
                    edge = e;
                    dest = e.getTargetNode();
                    break;
                }
            }
            if (edge != null) {
                map.removeEdge(edge);
                map.removeNode(dest);
                Direction invdir = direction.inverse();
                map.removeEdge(currentNode.toString() + invdir.toString());
                map.removeNode(currentNode.toString() + invdir.toString());
                // TODO: add path cost to each one
                map.addEdge(lastNode.getId() + init.toString(), lastNode, init, true);
                map.addEdge(init.toString() + lastNode.getId(), init, lastNode, true);
                map.getEdge(init.toString() + lastNode.getId()).addAttribute("direction", invdir.toString());
            }
        }
    }

    private void rotateToNext() {
        Edge edge = map.getEdge(currentNode.toString() + direction.toString());
        if (edge != null) {
            edge.changeAttribute("walkingIn", true);
        } else {
            Direction dir = direction;
            for (int i = 1; i < 3; i++) {
                dir = dir.rotateRigth();
                edge = map.getEdge(currentNode.toString() + dir.toString());
                if (edge != null && edge.hasAttribute("walkingIn")) {
                    rotate(i);
                    edge.changeAttribute("walkingIn", true);
                    break;
                }
            }
        }
        // everything is explored
        if(edge == null){
            explored.add(currentNode);
            lastDecision.remove(currentNode);
            goToLastDecision();
            
        }

    }
    private void goToLastDecision() {
        Viewer viewer = map.display();
        // Let the layout work ...
        //viewer.disableAutoLayout();
        SpriteManager sman = new SpriteManager(map);
        for(Node node:map.getNodeSet()){
            Sprite s = sman.addSprite(node.getId());
            s.attachToNode(node.getId());
        }
        viewer.enableAutoLayout();
    }

    ;
    private void rotate(int n) {
        for (int i = 1; i <= n; i++) {
            cmd.add(language.getAction(3)); //rotate
        }
    }

    private void walk() {
        // number of rotations to go forward
        int n = (!envMAP.get(ENV.PF)) ? 0
                : (!envMAP.get(ENV.PD)) ? 1 : 3;
        rotate(n);
        cmd.add(language.getAction(2)); // advance      
    }

    private void checkNode() {
        int neighbors = nNeighbors();
        if (neighbors >= 3) {// Decision Node
            checkDecision(neighbors);
        } else if (neighbors == 2) {// Walk node
            if (currentNode.equals(new Point(0, 0))) {//add initial node to lastDecision Stack
                checkDecision(neighbors);
            } else {
                walk();
            }
        } else if (neighbors == 1) {// End node
            checkDecision(neighbors);
        } else {// Locked Node
        }
    }

    private void setEnvMap(Percept p) {
        envMAP.put(ENV.PF, (Boolean) p.getAttribute(language.getPercept(0)));
        envMAP.put(ENV.PD, (Boolean) p.getAttribute(language.getPercept(1)));
        envMAP.put(ENV.PA, (Boolean) p.getAttribute(language.getPercept(2)));
        envMAP.put(ENV.PI, (Boolean) p.getAttribute(language.getPercept(3)));
        envMAP.put(ENV.MT, (Boolean) p.getAttribute(language.getPercept(4)));
        envMAP.put(ENV.AF, (Boolean) p.getAttribute(language.getPercept(5)));
        envMAP.put(ENV.AD, (Boolean) p.getAttribute(language.getPercept(6)));
        envMAP.put(ENV.AA, (Boolean) p.getAttribute(language.getPercept(7)));
        envMAP.put(ENV.AI, (Boolean) p.getAttribute(language.getPercept(8)));
    }

    @Override
    public Action compute(Percept p) {
        setEnvMap(p); // UPDATE-STATE
        if (cmd.size() == 0) {
            checkNode();
        }
        String x = cmd.remove();
        if (x.equals(language.getAction(3))) {
            direction = direction.rotateRigth(); // rotate
        } else if (x.equals(language.getAction(2))) { // advance
            goForward();
            System.out.println(currentNode.toString());
        }
        return new Action(x);
    }

    public boolean goalAchieved(Percept p) {
        return (((Boolean) p.getAttribute(language.getPercept(4))));
    }
}
