package student;

import controllers.Spaceship;
import models.Edge;
import models.Node;
import models.NodeStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import controllers.RescueStage;
import controllers.ReturnStage;

/** An instance implements the methods needed to complete the mission. */
public class MySpaceship implements Spaceship {

    /** The spaceship is on the location given by parameter state.
     * Move the spaceship to Planet X and then return, while the spaceship is on
     * Planet X. This completes the first phase of the mission.
     * 
     * If the spaceship continues to move after reaching planet X, rather than
     * returning, it will not count. Returning from this procedure while
     * not on Planet X counts as a failure.
     * <p>
     * There is no limit to how many steps you can take, but your score is
     * directly related to how long it takes you to find Planet X.
     * <p>
     * At every step, you know only the current planet's ID, the IDs of
     * neighboring planets, and the strength of the ping from Planet X at
     * each planet.
     * <p>
     * In this rescueStage, parameter stage has useful methods:<br>
     * (1) In order to get information about the current state, use functions
     * getLocation(), neighbors(), getPing(), and foundSpaceship().
     * <p>
     * (2) You know you are on Planet X when foundSpaceship() is true.
     * <p>
     * (3) Use function moveTo(long id) to move to a neighboring planet
     * by its ID. Doing this will change state to reflect your new position.
     */
    @Override
    public void rescue(RescueStage state) {

    	HashMap<Integer,Boolean> visited = new HashMap<Integer,Boolean>();
    	Integer currentLoc = state.getLocation();
    	visited.put(currentLoc, state.foundSpaceship());
    	planetFitness(state,visited);

    }
    
    /** helper method for depth first search, return when found ship
     * This method prioritizes node to pick based on strength of signal emitted
     * **/
    public void planetFitness(RescueStage state, HashMap<Integer,Boolean> visited) {
    	
    	Integer currentLoc = state.getLocation();
    	visited.put(currentLoc, state.foundSpaceship());
    	if(state.foundSpaceship() == true) {
    		return;
    	}
    	NodeStatus[] neighbors = state.neighbors();
    	Heap<NodeStatus> priorityList = addHeap(neighbors);
    	
    	
    	while(priorityList.size()!=0) {
    		int newLoc = priorityList.poll().getId();

    	if(!visited.containsKey(newLoc)) {
    		visited.put(newLoc, state.foundSpaceship());
    		state.moveTo(newLoc);
    		planetFitness(state, visited);
    		if(visited.containsValue(true)) {
    			return;
    		}
    		state.moveTo(currentLoc);

    		}
    	}
     }

    
    /**add neighbors to heap based on -1*signal, heap is a min-heap**/
     public Heap<NodeStatus> addHeap(NodeStatus[] neighbors){
    	Heap <NodeStatus> priorityHeap = new Heap<NodeStatus>();
    	
    	for(NodeStatus n: neighbors) {
    		priorityHeap.insert(n, n.getPingToTarget()*-1);
    	}
    	return priorityHeap;
    	
    }

    /** Return to Earth while collecting as many gems as possible.
     * The rescued spaceship has information on the entire galaxy, so you
     * now have access to the entire underlying graph. This can be accessed
     * through ReturnStage. getCurrentNode() and getEarth() will return Node
     * objects of interest, and getNodes() gives you a Set of all nodes
     * in the graph. 
     *
     * You must return from this function while on Earth. Returning from the
     * wrong location will be considered a failed run.
     *
     * You must make it back to Earth before running out of fuel.
     * state.getDistanceLeft() will tell you how far you can travel with your  
     * remaining fuel stores.
     * 
     * You can increase your score by collecting more gems on your way back to 
     * Earth. You should look for ways to optimize your return. The information 
     * from the rescued ship includes information on where gems are located. 
     * getNumGems() will give you the number of gems on a node. You will 
     * automatically collect any remaining gems when you move to a planet during 
     * the rescue stage.  */
    @Override
    public void returnToEarth(ReturnStage state) {
    	
    	Node planetW = state.getCurrentNode();
    	Node planetC = state.getCurrentNode();
    	Node mEarth = state.getEarth();
    	HashMap<Node,Boolean> visited = new HashMap<Node, Boolean>();
    	
    	
    	//this is the main body that moves and controls where the spaceship go
    	//loop will continue until arrive at earth 
    	while(planetW != mEarth) {
    		
    		planetW = state.getCurrentNode();
    		Heap<Node> maxGemNodes = gemChanger(planetW);
    		planetC = state.getCurrentNode();
    		Node toVisitNode = maxGemNodes.poll();    //this node will carry the most gem in planetW's vicinity
    		List<Node> minPath = MinPath.minPath(toVisitNode, mEarth);   //minPath from nodetovisit to Earth 
    		minPath.add(0,planetW);
    		List<Node> minPathC = MinPath.minPath(planetW, mEarth);     //current minPath to earth 

       	if(!checkFuel(state,minPathC, minPath)) {      //compare fuel stat wiht Max of minPathC, or minPath 
        	for(int i = 1; i<minPathC.size(); i++) {   //when return false, will move the spaceship back to earth 
    		state.moveTo(minPathC.get(i));
    	}
        	return;
       		
       	}
       	
       	visited.put(toVisitNode, true);                //update node to hashMap
       	state.moveTo(toVisitNode);                  //move to toVisitNode
       	planetW = state.getCurrentNode();          //update the location of the ship 
       	
       	
       	//if have visited node, will try to break the cycle, by going to a new path that contains uncollected gems
       	if(visited.containsKey(planetW)) {           
       		HashMap<Node, Boolean>map2 = new HashMap<Node, Boolean>();
       		LinkedList<Node> outPath = (LinkedList<Node>) outCycle(planetW, map2);  //path that contains more gems
       		List<Node> minPathOut = MinPath.minPath(outPath.getLast(), mEarth);  //min path to earth if taken the new path
       		minPathOut.addAll(0, outPath);  //list that containst the total path to travel from this iteration 
       		
           	if(!checkFuel(state,minPathOut,minPath)) {     //if fuel is less than any of the path, then better return to base
            	for(int i = 2; i<minPath.size(); i++) {	
            		state.moveTo(minPath.get(i));
        	}
            	return;
           		
           	}
       		for(int i = 1; i<outPath.size(); i++) {      //All is well, travel the path that contains more gems
       			state.moveTo(outPath.get(i));
       		}
       		planetW = state.getCurrentNode();
       	}
       	
    	}
    	

    }
    
    
    /**Helper method to find possible paths */
    public List<Node> findGem(Node planetW, Node Earth, HashMap<Node,Boolean> map) {
    	
    	map.put(planetW, true);

    	Heap<Node>priority = gemChanger(planetW);
    	while(priority.size()!=0) {
    		Node w = priority.poll();
    		if(w.equals(Earth)) {
    			LinkedList<Node> gsr = new LinkedList<Node>();
    			gsr.add(w);
    			return gsr;
    		}
    		if (!map.containsKey(w)) {
    			LinkedList<Node> path = (LinkedList<Node>) findGem(w, Earth, map);
        		if(path!=null) {
        			path.add(0,w);
        			return path;
    		}
    	}

    	}
    	return null;	
    }
    
   /** Helper method to count the distance **/
    public Integer findDistance(LinkedList<Node>path) {
    	Integer size = path.size();
    	Integer distance = 0;
    	if (size ==0) return 0;
    	Node current = path.get(0);
   	

    	for (int i=1;i<size; i++) {
    		Edge n = current.getEdge(path.get(i));
    		if(!path.get(i).equals(current)) {
    		distance = n.length + distance;
    		}
    		current = path.get(i);
    	}
    	return distance;
    }
    	

 
    
    /**heap sort to find maximum gem of each edge exit**/
    public Heap<Node> gemChanger(Node planet){
    	Heap<Node> priority = new Heap<Node>();
    	for (Edge n: planet.getExits()) {
    		Node t = n.getOther(planet);
    		priority.insert(t, -t.getNumGems());
    	}
    	return priority;
    }
    
    /**recursion to check for any nodes that have gems in order to get out of cycling **/
    public List<Node> outCycle(Node n, HashMap<Node, Boolean> map2){
    	
    	if(n==null) return null;
    	map2.put(n, true);
    	
    	Heap<Node>priority = gemChanger(n);
    	while(priority.size()!=0) {
    		Node w = priority.poll();
        	if(n.getNumGems()>0) {
        		LinkedList<Node> gsr = new LinkedList<Node>();
        		gsr.add(n);
        		return gsr;
        		
        	}
    		if(!map2.containsKey(w)) {
    	   		LinkedList <Node> neighList = (LinkedList<Node>) outCycle(w, map2);
        		if (neighList!= null) {
        			neighList.add(0,n);
        			return neighList;    			
        		}
    			
    		}
    	}
    	return null;
    }
    	

//    	
   /**method to compare fuel available and shortest distance at current node, and node to be taken **/
    public boolean checkFuel(ReturnStage state,  List<Node> minPath, List<Node> minPath2) {
    	
    	return state.getDistanceLeft() > Math.max(findDistance((LinkedList<Node>) minPath), findDistance((LinkedList<Node>) minPath2));
    	
    }
    
    
    }
    

