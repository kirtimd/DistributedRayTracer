import Jama.Matrix;

/**
 * Class Transform contains the transformation matrices: scale, rotate and translate.
 * Transformations on vertices, triangles, rays, bounds are performed here.
 * 
 * @author Kirti M D
 *
 */
public class Transform {
	private double sx, sy, sz, //scale
		   		   degreesX, degreesY, degreesZ, //rotate
		   		   tx, ty, tz; //translate
	
	private Matrix S, R, TR, SRT; // S : scale
								  // R : rotate
								  // TR : Translate
								  // SRT : product of all three
	private Matrix invS, invR, invTR, invSRT; //inverse 
	
	Transform(double sx1, double sy1, double sz1, 
			  double degx, double degy, double degz, 
			  double tx1, double ty1, double tz1) {
		sx = sx1;
		sy = sy1;
		sz = sz1;
		degreesX = degx;
		degreesY = degy;
		degreesZ = degz;
		tx = tx1;
		ty = ty1;
		tz = tz1;
		
		double[][] scale = {{sx, 0, 0, 0},{0, sy, 0, 0},{0, 0, sz, 0},{0, 0, 0, 1}};
		double thetaX = Math.toRadians(degreesX), thetaY = Math.toRadians(degreesY), thetaZ = Math.toRadians(degreesZ);
		double[][] rotateX = {{1, 0, 0, 0}, {0, Math.cos(thetaX), -Math.sin(thetaX), 0},
							 {0, Math.sin(thetaX), Math.cos(thetaX), 0},{0, 0, 0, 1}};
		double[][] rotateY = {{Math.cos(thetaY), 0, Math.sin(thetaY), 0}, {0, 1, 0, 0},
							  {-Math.sin(thetaY), 0, Math.cos(thetaY), 0}, {0, 0, 0, 1}};
		double[][] rotateZ = {{Math.cos(thetaZ), -Math.sin(thetaZ), 0, 0}, 
							  {Math.sin(thetaZ), Math.cos(thetaZ), 0, 0},
							  {0, 0, 1, 0}, {0, 0, 0, 1}};
		
		double[][] translate = {{1,0,0,tx},{0,1,0,ty},{0,0,1,tz},{0,0,0,1}};
		S = new Matrix(scale); //scale
		Matrix RX = new Matrix(rotateX); //rotate
		Matrix RY = new Matrix(rotateY); 
		Matrix RZ = new Matrix(rotateZ); 
		R = RX.times(RY).times(RZ); 
		TR = new Matrix(translate); //translate
		
		invS = S.inverse();
		invR = R.inverse();
		invTR = TR.inverse();
		invSRT = invS.times(invR).times(invTR);
		
		SRT = invSRT.inverse();

	}
	
	//default transformation
	Transform() {
		this(1, 1, 1, //scale
			 0, 0, 0, //rotate
			 0, 0, 0); //translate
	}
	
	//apply inverse transformation to ray
	public Ray transformRay(Ray ray) {
		double[] origin = ray.getOrigin();
		double[] dir = ray.getDirection();
		double x0 = origin[0], y0 = origin[1], z0 = origin[2], dx = dir[0], dy = dir[1], dz = dir[2];
		Matrix O = new Matrix(new double[][]{{x0},{y0},{z0},{1}});
		Matrix D = new Matrix(new double[][]{{dx},{dy},{dz},{0}});
		
		Matrix newO = invSRT.times(O);
		Matrix newD = invSRT.times(D);
		
		Ray newRay = new Ray(newO.get(0,0), newO.get(1,0), newO.get(2,0),
						newD.get(0,0), newD.get(1,0), newD.get(2,0));
		
		return newRay;	
	}
	
	public Point transformPoint(Point p) {
		if(p == null) return null;
		
		Matrix P = new Matrix(new double[][]{{p.x},{p.y},{p.z},{1}});
		Matrix P1 = SRT.times(P);
		Point newPt = new Point(P1.get(0,0), P1.get(1,0), P1.get(2,0));
		
		if(p.normal != null) {
		Matrix N = new Matrix(new double[][]{{p.normal[0]},{p.normal[1]},{p.normal[2]},{1}});
		Matrix transposeInvT = invSRT.transpose();
		Matrix N1 = transposeInvT.times(N);
		newPt.setNormal(N1.get(0,0), N1.get(1,0), N1.get(2,0));
		}
		if(p.color != null) newPt.color = p.color;
		return newPt;
	}
	
	public Bounds transformBounds(Bounds b) {
		
		Point min1 = new Point(b.xMin, b.yMin, b.zMin);
		Point max1 = new Point(b.xMax, b.yMax, b.zMax);
		
		Point min2 = transformPoint(min1);
		Point max2 = transformPoint(max1);
		
		b = new Bounds(min2.x, min2.y, min2.z, max2.x, max2.y, max2.z);
		
		return b;
	}
	
}
