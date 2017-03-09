
/**
 * LightSource stores position and color of the 
 * point or area light source.
 * 
 * @author Kirti M D
 *
 */
public class LightSource {

	double[] center;
	double length, breadth;
	Point[][] points; //points on light source, needed for shadow rays
	double[] ambient, diffuse, specular;
	//boolean random;
	
	LightSource(double[] center1, double length1, double breadth1, int gridSize) {
		center = center1;
		length = length1;
		breadth = breadth1;
		
		//white light
		ambient = new double[]{1,1,1};
		diffuse = new double[]{1,1,1};
		specular = new double[]{1,1,1};
		
		
		int noOfX = gridSize/2, noOfZ = gridSize/2;
		double incX = (double)length/noOfX, incZ = (double)breadth/noOfZ;
		points = new Point[noOfX][noOfZ];
		double x = center[0] - breadth/2, z = center[2] - length/2; 
		for(int i = 0; i < noOfX; i++) {
			for(int j = 0; j < noOfZ; j++) {
				points[i][j] = new Point(x, center[1], z);
				z += incZ;
			}
			x += incX;
		}
		
	}
	
}
