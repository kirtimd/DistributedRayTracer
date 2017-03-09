import java.util.ArrayList;

/**
 * The class Shape is the parent class for all scene objects
 * It stores the vertices, triangles(faces) and the kd-tree.
 * 
 * @author Kirti M D
 *
 */
public class Shape {
	
	private ArrayList<Triangle> faces;
	private ArrayList<Point> vertices; //keeping this list is useful while transforming	
	private int noOfSmoothingGroups;	
	private KDTree kdtree;
	
	Shape() {
		faces = new ArrayList<Triangle>();
		vertices = new ArrayList<Point>();
		kdtree = null;
		noOfSmoothingGroups = 0;
	}
	
	Shape(ArrayList<Point> vertices1, ArrayList<Triangle> faces1, int noOfSmoothingGroups1) {
		vertices = vertices1;
		faces = faces1;
		noOfSmoothingGroups = noOfSmoothingGroups1;
		kdtree = null;
	}

	/**
	 * Calls createTree() which will build the kd-tree from the list of triangles
	 */
	public void createKDTree() {
		kdtree = new KDTree();
		Bounds rootBounds = getXYZBounds();
		kdtree.createTree(faces, rootBounds);
	}
	
	/**
	 * Calls traverseTree() which will find ray-shape intersection by traversing the kd-tree
	 * @param ray
	 * @param light
	 * @param transform
	 * @return
	 */
	public Point intersectedByRay(Ray ray, LightSource light, Transform transform) {
		return kdtree.traverseTree(ray, light, transform);
	}
	
	
	/**
	 * Applies given transform to all vertices
	 * 
	 * @param t 	Transform object 
	 */
	public void transformVertices(Transform t) {	
		for(Point p : vertices)
			t.transformPoint(p);
	}
	
	/**
	 * Interpolate surface normals of all triangles
	 * Helps the surface appear more smooth 
	 */
	public void interpolateNormals() {
		//System.out.println("noOfSmoothingGroups: "+ noOfSmoothingGroups);
		
		for(int s = 0; s < noOfSmoothingGroups; s++) {
			//find all triangles connected to a vertex, add them and normalise
			ArrayList<Point> points = new ArrayList<Point>();
			ArrayList<Integer> count = new ArrayList<Integer>(); //number of triangles having a vertex v
			ArrayList<double[]> sumOfNormals = new ArrayList<double[]>();
			for(int i = 0; i < faces.size(); i++) {
				Triangle tri = faces.get(i);
				if(tri.smoothingGroup == s) {
					Point[] vertices = {tri.p0, tri.p1, tri.p2};
					for(Point v : vertices) {
						if(points.contains(v)) {
							int k = points.indexOf(v);
							count.set(k, count.get(k) + 1);
							double[] norm = sumOfNormals.get(k);
							norm[0] += tri.normal[0]; norm[1] += tri.normal[1]; norm[2] += tri.normal[2];
							sumOfNormals.set(k, norm);
						}
						else {
							points.add(v);
							count.add(1);
							sumOfNormals.add(tri.normal);
						}
					}
				}
			}
			ArrayList<double[]> newNormals = new ArrayList<double[]>();
			
			for(int i = 0; i < sumOfNormals.size(); i++) {
				double[] norm = sumOfNormals.get(i);
				int k = count.get(i);
				double[] newNorm = {(double)norm[0]/k, (double)norm[1]/k, (double)norm[2]/k};
				newNormals.add(newNorm);
			}
			
			for(int i = 0; i < faces.size(); i++) {
				if(faces.get(i).smoothingGroup == s) {
					int j = points.indexOf(faces.get(i).p0);
					double[] norm = newNormals.get(j); 
					faces.get(i).p0.setNormal(norm[0], norm[1], norm[2]);
					
					j = points.indexOf(faces.get(i).p1);
					norm = newNormals.get(j); 
					faces.get(i).p1.setNormal(norm[0], norm[1], norm[2]);
					
					j = points.indexOf(faces.get(i).p2);
					norm = newNormals.get(j); 
					faces.get(i).p2.setNormal(norm[0], norm[1], norm[2]);
					
				}
			}
		}
	}
	
	/** 
	 * @return	the minimum and maximum x, y and z values for the shape
	 */
	public Bounds getXYZBounds() {
		  double lowestX = Double.MAX_VALUE, highestX = -Double.MAX_VALUE, 
					  lowestY = Double.MAX_VALUE, highestY = -Double.MAX_VALUE, 
					  lowestZ = Double.MAX_VALUE, highestZ = -Double.MAX_VALUE; 
				 
		  for(Triangle t : faces) { 
			  
			  if(t.p0.x < lowestX) lowestX = t.p0.x;  if(t.p0.x > highestX) highestX = t.p0.x;  
			  if(t.p1.x < lowestX) lowestX = t.p1.x;  if(t.p1.x > highestX) highestX = t.p1.x;  
			  if(t.p2.x < lowestX) lowestX = t.p2.x;  if(t.p2.x > highestX) highestX = t.p2.x;  
			  
			  if(t.p0.y < lowestY) lowestY = t.p0.y;  if(t.p0.y > highestY) highestY = t.p0.y;  
			  if(t.p1.y < lowestY) lowestY = t.p1.y;  if(t.p1.y > highestY) highestY = t.p1.y;  
			  if(t.p2.y < lowestY) lowestY = t.p2.y;  if(t.p2.y > highestY) highestY = t.p2.y;  
			  
			  if(t.p0.z < lowestZ) lowestZ = t.p0.z;  if(t.p0.z > highestZ) highestZ = t.p0.z;  
			  if(t.p1.z < lowestZ) lowestZ = t.p1.z;  if(t.p1.z > highestZ) highestZ = t.p1.z;  
			  if(t.p2.z < lowestZ) lowestZ = t.p2.z;  if(t.p2.z > highestZ) highestZ = t.p2.z;  
			  
		  }
		  
		  //to avoid a flat box, i.e. if the min and max for x, y or z are the same value
		  //add/subtract a tiny value so as to increase the bound area
		  double delta = 0.001d;
		  Bounds b = new Bounds(lowestX - delta, lowestY - delta, lowestZ - delta, 
				  				highestX + delta, highestY + delta, highestZ + delta);
		 
		  return b;
	}
	
	
	public int getNoOfFaces() {
		return faces.size();
	}
	
	/**
	 * Set default material for all triangles in the list.
	 * It is used for shapes without textures.
	 * @param mat
	 */
	public void setMaterial(Material mat) {
		for(Triangle tri : faces) {
			tri.material = mat;
		}
	}
}
