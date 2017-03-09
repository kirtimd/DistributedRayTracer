
/**
 * Class Bounds contains the upper and lower limits for X, Y and Z
 * coordinates corresponding to each node of the kd-tree.
 * All edges of Bounds object are axis aligned.
 * 
 * @author Kirti M D
 *
 */
public class Bounds {
	double xMin, xMax, yMin, yMax, zMin, zMax;
	
	Bounds(double x0, double y0, double z0, //min
			double x1, double y1, double z1) { //max
		xMin = (x0 < x1 ? x0 : x1);
		yMin = (y0 < y1 ? y0 : y1);
		zMin = (z0 < z1 ? z0 : z1);
		
		xMax = (x0 > x1 ? x0 : x1);
		yMax = (y0 > y1 ? y0 : y1);
		zMax = (z0 > z1 ? z0 : z1);
		
	}
	
	
	//This method checks if given triangle is within these bounds
	public boolean withinBounds(Triangle tri) {
		if(tri.p0.x <= xMin && tri.p1.x <= xMin && tri.p2.x <= xMin &&
		   tri.p0.x >= xMax && tri.p1.x >= xMax && tri.p2.x >= xMax &&
		   tri.p0.y <= yMin && tri.p1.y <= yMin && tri.p2.y <= yMin &&
		   tri.p0.y >= yMax && tri.p1.y >= yMax && tri.p2.y >= yMax &&
		   tri.p0.z <= zMin && tri.p1.z <= zMin && tri.p2.z <= zMin &&
		   tri.p0.z >= zMax && tri.p1.z >= zMax && tri.p2.z >= zMax
		) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String out = "Bounds:\n";
		out += "\tX : " + xMin + " to " + xMax + " : "+ (xMax-xMin)+"\n";
		out += "\tY : " + yMin + " to " + yMax + " : "+ (yMax-yMin)+"\n";
		out += "\tZ : " + zMin + " to " + zMax + " : "+ (zMax-zMin);
		return out;
	}
}
