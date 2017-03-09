
/**
 * The plane that splits a bounding box to create two child bounds
 * is represented by class SplitPlane.
 * It stores the x, y, z limits and normal for the splitting plane
 * of each kd-tree node.
 * 
 * @author Kirti M D
 *
 */
public class SplitPlane {
	double xMin, yMin, zMin, xMax, yMax, zMax;
	double[] normal;
	
	double loc; //not set yet
	int level;

	public void setXBounds(double x_min1, double x_max1) {
		xMin = x_min1;
		xMax = x_max1;
	}
	
	public void setYBounds(double y_min1, double y_max1) {
		yMin = y_min1;
		yMax = y_max1;
	}
	
	public void setZBounds(double z_min1, double z_max1) {
		zMin = z_min1;
		zMax = z_max1;
	}
	
	public void setNormal() {
		
	}
	
	@Override
	public String toString() {
		if(xMin == xMax) {
			return "X - split plane at x = " +xMin + "\n" +
					"\ty_min = " + yMin + "  y_max = " + yMax + "\n" + 
					"\tz_min = " + zMin + "  z_max = " + zMax;
			
		}
		else if(yMin == yMax) {
			return "Y - split plane at y = " +yMin + "\n" +
				   "\tx_min = " + xMin + "  x_max = " + xMax + "\n" +
				   "\tz_min = " + zMin + "  z_max = " + zMax;
			
		}
		
		//else if(zMin == zMax) 
		return "Z - split plane at z = " +zMin + "\n" +
			   "\tx_min = " + xMin + "  x_max = " + xMax + "\n" +
			   "\ty_min = " + yMin + "  y_max = " + yMax;
		
		
	}
}
