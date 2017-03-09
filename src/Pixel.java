import java.util.concurrent.ThreadLocalRandom;

/**
 * The image plane is made of an array of Pixel objects.
 * Class Pixel contains the position inside the array, 
 * and range of X, Y, Z inside which the actual sampling point must lie.
 * We can place the sample in the middle of the pixel, or add noise.
 * 
 * @author Kirti M D
 *
 */
public class Pixel {
	
	int row, column; //position in the grid
	double xMin, yMin, xMax, yMax, z;
	Point position; 
	
	Pixel(double x0, double y0, double x1, double y1, double z0) {
		xMin = (x0 < x1? x0 : x1);
		yMin = (y0 < y1? y0 : y1);
		xMax = (x0 > x1? x0 : x1);
		yMax = (y0 > y1? y0 : y1);
		z = z0;
		
		//default is uniform sampling
		//i.e. place sample in the middle of the x and y range
		position = new Point((xMin + xMax)*0.5d, (yMin + yMax)*0.5d, z);	
	}
	
	//change the X and Y coordinates slightly by adding random noise to both
	public void addJitter() {
		double newX = ThreadLocalRandom.current().nextDouble(xMin, xMax); //returns double within this range
	
		double newY = ThreadLocalRandom.current().nextDouble(yMin, yMax); 
	
		position = new Point(newX, newY, z);
	}
	
	  @Override
	  public boolean equals(Object o) {
		  Pixel p = (Pixel)o;
		  if(xMin == p.xMin && xMax == p.xMax
			 && yMin == p.yMin && yMax == p.yMax) return true;
		  
		  return false;
	  }
}
