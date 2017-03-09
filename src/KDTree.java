import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class KDTree is used to build and traverse a kd-tree
 * 
 * @author Kirti M D
 *
 */
public class KDTree {
	
	private KDNode root;
	private int treeDepth;
	
	/**
	 * Calls divide() which will create the kd-tree from given list
	 * 
	 * @param list			list of Triangles created after parsing the obj file
	 * @param rootBounds	x, y, z bounds of the model as a whole
	 */
	public void createTree(ArrayList<Triangle> list, Bounds rootBounds) {
		if(list.size() == 0) return;
		treeDepth = 0;
		root = divide1(list, rootBounds, 0); //divide1 : median, divide2 : SAH
		System.out.print("Root ");
		System.out.println(root.bounds);
		System.out.println("Tree depth: "+treeDepth);
	}
	
	/**
	 * Calls traverse() which will search the tree for an intersection for given ray
	 * 
	 * @param ray			Ray object for which need the nearest intersection
	 * @param light			LightSource object 
	 * @param transform 	Transformation for the Shape object
	 * @return				intersection point(if any)
	 */
	public Point traverseTree(Ray ray,  LightSource light, Transform transform) {
		Ray transRay = transform.transformRay(ray);//inverse transform the ray
		Point p = traverse(root, transRay, light);
		
		p = transform.transformPoint(p); 
		return p;
	}
	
	/**
	 * It recursively searches the tree to find the intersection
	 * @param node 		current kd-tree node to be searched
	 * @param ray		Ray object
	 * @param light		LightSource object(needed during shading calculations
	 * 					when intersection is found)
	 * 
	 * @return			Point object if nearest intersection found. 
	 * 					Null if not.
	 */
	private Point traverse(KDNode node, Ray ray, LightSource light) {			
		if(node.leaf != null) { //if node is a leaf
			ArrayList<Point> pts = new ArrayList<Point>();
			for(Triangle t : node.leaf) {
				Point p = ray.intersectsTriangle(t, light);//, transform);
				if(p != null) pts.add(p);
				if(!t.traversed) {
					t.traversed=true;
					
				}
			}
			double[] rayOrigin = ray.getOrigin();
			Point origin = new Point(rayOrigin[0], rayOrigin[1], rayOrigin[2]);
			Point p = origin.closestIntersection(pts);
			return p;
		}
		
		//continue if current node is not leaf node 	
		
		Point[] pBox = ray.intersectsBoundingBox(node, light);
		
		Point p = null;
		if(pBox != null) {
			int i = node.level%3;
			//store points as double arrays
			double[] enter = {pBox[0].x, pBox[0].y, pBox[0].z},  
					 exit  = {pBox[2].x, pBox[2].y, pBox[2].z};
			
			if(pBox[1] == null) { //if ray does not intersect the splitPlane
				double splitPlaneLoc = 0;
				if(i == 0) splitPlaneLoc = node.splitPlane.xMin; 
				else if(i == 1) splitPlaneLoc = node.splitPlane.yMin; 
				else splitPlaneLoc = node.splitPlane.zMin; 
	
				if(enter[i] < splitPlaneLoc) {
					p = traverse(node.A, ray, light);
				} else {
					p = traverse(node.B, ray, light);
				}
			} else {
			double[] split = {pBox[1].x, pBox[1].y, pBox[1].z}; 
			
			if(enter[i] <= split[i]) {
				if(exit[i] < split[i]) {
					p = traverse(node.A, ray, light); //A
				} else {
					if(exit[i] == split[i]) {
						p = traverse(node.A, ray, light); //A or B
					} else { // if exit.x > split.x
						Point p1 = traverse(node.A, ray, light);
						Point p2 = traverse(node.B, ray, light);
						if(p1 == null || p2 == null) {
							p = (p1 == null) ? p2 : p1;
						}
						
						else if(p1 != null && p2 != null) {
							double[] origin = ray.getOrigin();
							Point rayOrigin = new Point(origin[0], origin[1], origin[2]);	
							double dist1 = p1.distanceFrom(rayOrigin), dist2 = p2.distanceFrom(rayOrigin);	
							p = (dist1 < dist2) ? p1 : p2;
						}
					}
				}
			} 
			else { //enter.x > split.x
				if(exit[i] > split[i]) { //B
					p = traverse(node.B, ray, light);
				} else {
					Point p1 = traverse(node.B, ray, light);
					Point p2 = traverse(node.A, ray, light);
					if(p1 == null || p2 == null) {
						p = (p1 == null) ? p2 : p1;
					}
					
					else if(p1 != null && p2 != null) {
						double[] origin = ray.getOrigin();
						Point rayOrigin = new Point(origin[0], origin[1], origin[2]);	
						double dist1 = p1.distanceFrom(rayOrigin), dist2 = p2.distanceFrom(rayOrigin);	
						p = (dist1 < dist2) ? p1 : p2;
					}
				}
			}			
		}
		
		}
		return p;	
	}	
	
	/**
	 * It creates the kd-tree by recursively dividing the list along the median.
	 * 
	 * @param list		ArrayList of remaining Triangle objects
	 * @param bounds	x,y,z bounds of the list above
	 * @param level		node level(determines the axis along which the list is split)
	 * 
	 * @return			the parent node for list
	 * 					(ultimately returns the root node after the tree is complete)
	 */
	private KDNode divide1(ArrayList<Triangle> list, Bounds bounds, int level) {
		if(treeDepth < level) treeDepth = level;
		if(list.size() == 1) {//we've reached the triangle
			KDNode leafNode = new KDNode();
			leafNode.level = level; 
			leafNode.leaf = new ArrayList<Triangle>(list); 
			leafNode.bounds = bounds;
		
			return leafNode;
		} 
		
		int i = level % 3; //0 : x, 1 : y, 2 : z
		
		KDNode newNode = new KDNode();
		newNode.level = level ;
		
		double median = 0; 
		newNode.bounds = bounds; 
		newNode.splitPlane = new SplitPlane();
		ArrayList<Triangle> leftOfMedian = new ArrayList<Triangle>(), rightOfMedian = new ArrayList<Triangle>();
		Bounds boundsA = null, boundsB = null;//new Bounds();
		switch(i) {
		case 0 : // X
			//sort list by x
			Collections.sort(list, new Comparator<Triangle>() {
				@Override
				public int compare(Triangle t1, Triangle t2) {
					return Double.compare(t1.centroid.x, t2.centroid.x);
				}				
			});
			
			//find median
			if(list.size() % 2 != 0) { //for odd number of triangles
				int mid = (list.size() - 1)/2;
				median = list.get(mid).centroid.x;
				
			} else {  //if even, take average of the two middle values
				int mid = (list.size())/2;
				median = (double)(list.get(mid - 1).centroid.x + list.get(mid).centroid.x)/2;  
				
			}
			
			leftOfMedian = new ArrayList<Triangle>();
			rightOfMedian = new ArrayList<Triangle>();
			for(Triangle t : list) {
				//if any vertex is on the left, add to list
				if(t.p0.x <= median || t.p1.x <= median || t.p2.x <= median) {
					leftOfMedian.add(t);
				}
				if(t.p0.x > median || t.p1.x > median || t.p2.x > median) {
					rightOfMedian.add(t);
				}
			}
			newNode.splitPlane.setXBounds(median, median);
			newNode.splitPlane.setYBounds(bounds.yMin, bounds.yMax);
			newNode.splitPlane.setZBounds(bounds.zMin, bounds.zMax);
			newNode.splitPlane.normal = new double[]{1, 0, 0};
			//divide bounds vertically at median
			boundsA = new Bounds(bounds.xMin, bounds.yMin, bounds.zMin, median, bounds.yMax, bounds.zMax);
			boundsB = new Bounds(median, bounds.yMin, bounds.zMin, bounds.xMax, bounds.yMax, bounds.zMax);
			
			break;
		
		case 1 : // Y
			//sort list by y
			Collections.sort(list, new Comparator<Triangle>() {
				@Override
				public int compare(Triangle t1, Triangle t2) {
					return Double.compare(t1.centroid.y, t2.centroid.y);
				}				
			});
			
			//find median
			if(list.size() % 2 != 0) { //for odd number of triangles
				int mid = (list.size() - 1)/2;
				median = list.get(mid).centroid.y;
				
			} else {  //if even, take average of the two middle values
				int mid = (list.size())/2;
				median = (double)(list.get(mid - 1).centroid.y + list.get(mid).centroid.y)/2;  
				
			}
			leftOfMedian = new ArrayList<Triangle>();
			rightOfMedian = new ArrayList<Triangle>();
			for(Triangle t : list) {
				//if any vertex is on the left, add to list
				if(t.p0.y <= median || t.p1.y <= median || t.p2.y <= median) {
					leftOfMedian.add(t);
				}
				if(t.p0.y > median || t.p1.y > median || t.p2.y > median) {
					rightOfMedian.add(t);
				}
			}
			newNode.splitPlane.setXBounds(newNode.bounds.xMin, newNode.bounds.xMax);
			newNode.splitPlane.setYBounds(median, median);
			newNode.splitPlane.setZBounds(newNode.bounds.zMin, newNode.bounds.zMax);
			newNode.splitPlane.normal = new double[]{0, 1, 0};
			
			boundsA = new Bounds(bounds.xMin, bounds.yMin, bounds.zMin, bounds.xMax, median, bounds.zMax);
			boundsB = new Bounds(bounds.xMin, median, bounds.zMin, bounds.xMax, bounds.yMax, bounds.zMax);
			
			break;
			
		case 2 : // Z
			//sort list by z
			Collections.sort(list, new Comparator<Triangle>() {
				@Override
				public int compare(Triangle t1, Triangle t2) {
					return Double.compare(t1.centroid.z, t2.centroid.z);
				}				
			});
			
			//find median
			if(list.size() % 2 != 0) { //for odd number of triangles
				int mid = (list.size() - 1)/2;
				median = list.get(mid).centroid.z;
				
			} else {  //if even, take average of the two middle values
				int mid = (list.size())/2;
				median = (double)(list.get(mid - 1).centroid.z + list.get(mid).centroid.z)/2;  
				
			}
			leftOfMedian = new ArrayList<Triangle>();
			rightOfMedian = new ArrayList<Triangle>();
			for(Triangle t : list) {
				//if any vertex is on the left, add to list
				if(t.p0.z <= median || t.p1.z <= median || t.p2.z <= median) {
					leftOfMedian.add(t);
				}
				if(t.p0.z > median || t.p1.z > median || t.p2.z > median) {
					rightOfMedian.add(t);
				}
			}
			newNode.splitPlane.setXBounds(bounds.xMin, bounds.xMax);
			newNode.splitPlane.setYBounds(bounds.yMin, bounds.yMax);
			newNode.splitPlane.setZBounds(median, median);
			newNode.splitPlane.normal = new double[]{0, 0, 1};
			
			boundsA = new Bounds(bounds.xMin, bounds.yMin, bounds.zMin, bounds.xMax, bounds.yMax, median);
			boundsB = new Bounds(bounds.xMin, bounds.yMin, median, bounds.xMax, bounds.yMax, bounds.zMax);
			
			break;
		}
		
		
		if(leftOfMedian.size() == list.size() || rightOfMedian.size() == list.size()) {
			newNode.leaf = new ArrayList<Triangle>();
			for(Triangle t : list) {
				newNode.leaf.add(t);
			}
			return newNode;
		} 
			
		newNode.A = divide1(leftOfMedian, boundsA, level + 1);
		newNode.B = divide1(rightOfMedian, boundsB, level + 1);
	
		return newNode;
		
	}
	

}
