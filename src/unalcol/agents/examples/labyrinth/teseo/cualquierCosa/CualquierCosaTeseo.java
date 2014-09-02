package unalcol.agents.examples.labyrinth.teseo.cualquierCosa;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import unalcol.agents.Action;
import unalcol.agents.Percept;
import unalcol.types.collection.vector.Vector;

public class CualquierCosaTeseo extends SimpleTeseoAgentProgram {

    private Graph map;

    private enum Direction {

        NORTH, EAST, SOUTH, WEST;

        public Direction rotateRigth() {
            return Direction.values()[(ordinal() + 1) % 4];
        }

        public int toDir(Direction dir) {
            return (dir.ordinal() + ordinal()) % 4;
        }
    }

    private enum ENV {

        PF, PD, PA, PI, MT, AF, AD, AA, AI;
    }
    private final EnumMap<ENV, Boolean> envMAP;

    private Direction direction;
    private final Stack<String> lastDecision;
    private String currentNode;
    private final Vector<String> visited;

    @Override
    public void init() {
        super.init();
        map.clear();
        direction = Direction.NORTH;
        lastDecision.clear();
        currentNode = "0,0";
        visited.clear();
    }

    public CualquierCosaTeseo() {
        map = new SingleGraph("mapa");
        direction = Direction.NORTH;
        lastDecision = new Stack<>();
        map.addNode("0,0");
        currentNode = "0,0";
        visited = new Vector<>();
        envMAP = new EnumMap<>(ENV.class);
    }

    private String getNextNodeName(int x, int y) {
        if (direction == Direction.SOUTH) {
            x = -x;
            y = -y;
        } else if (direction == Direction.EAST) {
            int t;
            t = x;
            x = y;
            y = t;
        } else if (direction == Direction.WEST) {
            int t;
            t = x;
            x = -y;
            y = -t;
        }
        String[] xy = currentNode.split(",");
        return Integer.toString(Integer.parseInt(xy[0]) + x) + ","
                + Integer.toString(Integer.parseInt(xy[1]) + y);
    }
    private void addNode(String parent, String name) {
        try {
            map.addNode(name);
        } catch (IdAlreadyInUseException e) {
        }
        try {
            map.addNode(parent);
        } catch (IdAlreadyInUseException e) {
        }
        try {
            map.addEdge(parent + name, parent, name).addAttribute("length", 1);
        } catch (IdAlreadyInUseException | ElementNotFoundException | EdgeRejectedException e) {
        }
    }
    private void addToMap() {
        String label;

        if (!envMAP.get(ENV.PF)) {
            addNode(currentNode, getNextNodeName(1, 0));
        }
        if (!envMAP.get(ENV.PA)) {
            addNode(currentNode, getNextNodeName(-1, 0));
        }
        if (!envMAP.get(ENV.PD)) {
            addNode(currentNode, getNextNodeName(0, 1));
        }
        if (!envMAP.get(ENV.PI)) {
            addNode(currentNode, getNextNodeName(0, -1));
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

    private int accion() {
        if (envMAP.get(ENV.MT)) {
            return -1;
        }
        if (!envMAP.get(ENV.PF)) {
            return 0;
        }
        if (!envMAP.get(ENV.PD)) {
            return 1;
        }
        int k = 0;
        return k;
    }

    private void addToDecisionStack() {
        try {
            Iterator it = map.getNode(currentNode).getNeighborNodeIterator();
            int not_visited = 0;
            String to_visit, temp;
            to_visit = "";
            temp = "";
            while (it.hasNext()) {
                // si el nodo vecino no ha sido visitado
                temp = it.next().toString();
                if (!visited.contains(temp)) {
                    not_visited++;
                    to_visit=temp;
                }
            }
            String front = getNextNodeName(1, 0);
            if (not_visited == 1) {
                if(map.getNode(currentNode).hasEdgeBetween(front) && !visited.contains(front)){
                    cmd.add(language.getAction(2));
                } else {
                    goToNeighborNode(currentNode, to_visit, direction);
                }
            
            } else if (not_visited == 0) {
                createReturnSequence();
            } else if (not_visited > 1) {
                lastDecision.add(currentNode);
                if(map.getNode(currentNode).hasEdgeBetween(front) && !visited.contains(front)){
                    cmd.add(language.getAction(2));
                } else {
                    goToNeighborNode(currentNode, to_visit, direction);
                }
            } else {
                cmd.add(language.getAction(0));
            }
        } catch (Exception e) {
        }

    }

    private Direction goToNeighborNode(String source, String destination, Direction d) {
        String[] xySource = source.split(",");
        int xSource = Integer.parseInt(xySource[0]);
        int ySource = Integer.parseInt(xySource[1]);
        String[] xyDest = destination.split(",");
        int xDest = Integer.parseInt(xyDest[0]);
        int yDest = Integer.parseInt(xyDest[1]);

        int x = xDest - xSource;
        int y = yDest - ySource;

        Direction destinationDirection;

        if (x == 0) {
            destinationDirection = (y > 0) ? Direction.EAST : Direction.WEST;
        } else {
            destinationDirection = (x > 0) ? Direction.NORTH : Direction.SOUTH;
        }
        int n = d.toDir(destinationDirection);
        for (int i = 1; i <= n; i++) {
            cmd.add(language.getAction(3)); //rotate
        }
        cmd.add(language.getAction(2));
        return destinationDirection;
    }

    private void createReturnSequence() {
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
        dijkstra.init(map);
        dijkstra.setSource(map.getNode(currentNode));
        dijkstra.compute();
        String last = lastDecision.pop();
        Path path = dijkstra.getPath(map.getNode(last));
        Edge edge;
        Direction d = direction;
        String source, destination;
        List nodes = path.getNodePath();
        source = currentNode;
        for (int i = 1; i < nodes.size(); i++) {
            d = goToNeighborNode(source, nodes.get(i).toString(), d);
            source = nodes.get(i).toString();
        }
    }

    @Override
    public Action compute(Percept p) {
        setEnvMap(p);
        if (cmd.size() == 0) {
            // añade vecinos al mapa
            addToMap();
            // chequea si puede visitar más nodos 
            addToDecisionStack();
            if (!visited.contains(currentNode)) {
                visited.add(currentNode);
            }
        }
        String x = cmd.get(0);
        if (x.equals(language.getAction(3))) { //rotate
            direction = direction.rotateRigth();
        } else if (x.equals(language.getAction(2))) {
            if(envMAP.get(ENV.PF)){
                x = language.getAction(0);
                direction = direction.rotateRigth();
            } else {
                currentNode = getNextNodeName(1, 0);
            }
            
        };
        if(cmd.size() > 0)  cmd.remove(0);
        return new Action(x);
    }
}
