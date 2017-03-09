import java.util.ArrayList;

import Jama.Matrix;

/**
 * Class Point represents a 3d point on a surface.
 * Along with the x, y and z coordinates, it stores the surface normal
 * and color.
 * 
 * @author Kirti M D
 *
 */
public class Point {
	double x, y, z;
	double[] normal;
	double[] color;
	int id;//object id on which the point lies. Used in shadow calculations
		   //set to -1, if it is not on object OR object id not assigned yet
	
	Texture tex; //only set for intersection points
	
	Point() {
		color = new double[3];
		id = -1;
	}
	Point(double x1, double y1, double z1) {
		x = x1;
		y = y1;
		z = z1;
		color = new double[3];
		id = -1;
		//tex = null;
	}
		
	//copy constructor
	Point(Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
		if(p.normal != null) normal = new double[]{p.normal[0], p.normal[1], p.normal[2]};
		if(p.color != null) color = new double[]{p.color[0], p.color[1], p.color[2]};
	}
	
	public void setNormal(double nx, double ny, double nz) {
		normal = new double[]{nx, ny, nz};
		normal = normalize(normal);
	}
    
    public double distanceFrom(Point p) {
    	return Math.sqrt((x - p.x)*(x - p.x) + (y - p.y)*(y - p.y) + (z - p.z)*(z - p.z));
    }
    
    @Override
    public boolean equals(Object o) {
    	if(o instanceof Point) {
	    	Point p = (Point)o;
	    	if(x == p.x && y == p.y && z == p.z) {
	    		return true;
	    	}
    	}
    	return false;
    }
    
    @Override
    public String toString() {
    	 return "Point: "+x+" "+y+" "+z+"\n"+
    			 "\t N: "+normal[0]+" "+normal[1]+" "+normal[2];
    	 
    }
    
    // from given list, find the closest one to this point
    public Point closestIntersection(ArrayList<Point> pts) {
		if(pts.size() == 0) return null;
		
		if(pts.size() == 1) {
			return pts.get(0);
		}
		
		Point closest = null; 	
		double dist = Double.MAX_VALUE;	
		
		for(Point p : pts) {
			if(p != null)
			if(p.distanceFrom(this) < dist) {
				dist = p.distanceFrom(this);
				closest = p;
			}
		}
		return closest;
	}   
    
    private double[] normalize(double[] A) {
    	double l = Math.sqrt(A[0]*A[0] + A[1]*A[1] + A[2]*A[2]);
    	A[0] = (double)A[0]/l;
    	A[1] = (double)A[1]/l;
    	A[2] = (double)A[2]/l;
    	return A;
    }
}
