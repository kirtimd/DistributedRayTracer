import java.util.ArrayList;
import Jama.*; 

/**
 * Class Ray has the origin and direction for each ray.
 * It contains the intersection tests for the bounding box and splitting plane
 * for each kd-tree node, and triangle as well.
 * 
 * @author Kirti M D
 *
 */
public class Ray {

	private double x0, y0, z0; //origin
	private double dx, dy, dz; //direction
	
	Ray(double x1, double y1, double z1, double dx1, double dy1, double dz1) {
		x0 = x1;
		y0 = y1;
		z0 = z1;

		dx = dx1;
		dy = dy1;
		dz = dz1;
		
		//normalize it
		double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
		if(length != 0) {
		 dx = (double)dx/length;
		 dy = (double)dy/length;
		 dz = (double)dz/length;
		}
		
	}
	
	


	
	/**
	 * Calculates if this ray intersects the bounds and splitting plane of given node.
	 * 
	 * @param node		a kd-tree node
	 * @param light		LightSource object
	 * 
	 * @return			point where intersection occurs
	 * 					null if ray does not intersect bounds
	 */
	public Point[] intersectsBoundingBox(KDNode node, LightSource light) { 
		
		Point[] pBox1 = intersectsBounds(node.bounds, light);
		if(pBox1 == null) return null;
		Point[] pBox2 = pBox1;
		
		Point split1 = intersectsSplitPlane(node.splitPlane);
		Point split2 = split1;
		return new Point[]{pBox2[0], split2, pBox2[1]};
	}
	
	/**
	 * Finds out if this ray intersects any of the six faces of 
	 * the bounding box. If there is an intersection, it will return the 
	 * intersection point closest to the ray origin.
	 * 
	 * @param b		  bounds
	 * @param light	  LightSource object
	 * @return		  nearest intersection. 
	 * 				  null, if ray does not intersect the bounding box.
	 */
	public Point[] intersectsBounds(Bounds b, LightSource light) {	
		if(b == null) return null;
		
		//there will be either 0 or 2 intersections
		ArrayList<Point> pts = new ArrayList<Point>(); 
		
		//normals are pointed outwards
		double[] nLeft = {-1, 0, 0}, nRight = {1, 0, 0}, 
				nTop = {0, 1, 0}, nBottom = {0, -1, 0}, 
				nFront = {0, 0, 1}, nBack = {0, 0, -1};
		
		//bottom face
		double A = nBottom[0], B = nBottom[1], C = nBottom[2]; 
		double x = b.xMin, y = b.yMin, z = b.zMin; //a point on the plane
		double F = -(x*A + y*B + z*C);	
		double w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);	
		Point pbot = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pbot.setNormal(A, B, C);
		if(pbot.x < b.xMin || pbot.x > b.xMax 
				|| pbot.z < b.zMin || pbot.z > b.zMax) {				
			pbot = null; // if p is not within the bounds of the floor, it does not intersect it					
		}
		if(pbot != null && !pts.contains(pbot)) pts.add(pbot);
	 
		
		//top face
		A = nTop[0]; B = nTop[1]; C = nTop[2]; 
		x = b.xMin; y = b.yMax; z = b.zMin; //a point on the plane
		F = -(x*A + y*B + z*C);
		w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);
		Point pt = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pt.setNormal(A, B, C);
		if(pt.x < b.xMin || pt.x > b.xMax 
			|| pt.z < b.zMin || pt.z > b.zMax) {				
			pt = null; 				
		}
		if(pt != null && !pts.contains(pt)) pts.add(pt);	
		
		//left
		A = nLeft[0]; B = nLeft[1]; C = nLeft[2]; 
		x = b.xMin; y = b.yMin; z = b.zMin; //a point on the plane
		F = -(x*A + y*B + z*C);
		w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);
		Point pl = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pl.setNormal(A, B, C);
		if(pl.y < b.yMin || pl.y > b.yMax 
					|| pl.z < b.zMin || pl.z > b.zMax) {				
			pl = null; 			
		} 
		if(pl != null  && !pts.contains(pl)) pts.add(pl);
	
		//right
		A = nRight[0]; B = nRight[1]; C = nRight[2]; 
		x = b.xMax; y = b.yMin; z = b.zMin; //a point on the plane
		F = -(x*A + y*B + z*C);
		w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);
		Point pr = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pr.setNormal(A, B, C);
		if(pr.y < b.yMin || pr.y > b.yMax 
					|| pr.z < b.zMin || pr.z > b.zMax) {				
			pr = null; 
		} 
		if(pr != null && !pts.contains(pr)) pts.add(pr);
	 			
		//back
		A = nBack[0]; B = nBack[1]; C = nBack[2]; 
		x = b.xMax; y = b.yMin; z = b.zMin; //a point on the plane
		F = -(x*A + y*B + z*C);
		w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);
		Point pb = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pb.setNormal(A, B, C);
		if(pb.x < b.xMin || pb.x > b.xMax 
					|| pb.y < b.yMin || pb.y > b.yMax) {				
			pb = null; 
		} 
		if(pb != null  && !pts.contains(pb)) pts.add(pb);
		
		//front
		A = nFront[0]; B = nFront[1]; C = nFront[2]; 
		x = b.xMax; y = b.yMin; z = b.zMax; //a point on the plane
		F = -(x*A + y*B + z*C);
		w = (double)(-A*x0 - B*y0 - C*z0 - F)/(A*dx + B*dy + C*dz);
		Point pf = new Point(x0 + dx*w, y0 + dy*w, z0 + dz*w);
		pf.setNormal(A, B, C);
		if(pf.x < b.xMin || pf.x > b.xMax 
					|| pf.y < b.yMin || pf.y > b.yMax) {				
			pf = null; 
		}  
		if(pf != null  && !pts.contains(pf)) pts.add(pf);

		if(pts.size() == 0) return null;
		
		//a ray always intersects a box at two points
		if(pts.size() != 2) return null;
		Point p1 = pts.get(0), p2 = pts.get(1);
		double[] dirOriginToP1 = {p1.x - x0, p1.y - y0, p1.z - z0},
				 dirOriginToP2 = {p2.x - x0, p2.y - y0, p2.z - z0};
		dirOriginToP1 = normalize(dirOriginToP1);
		dirOriginToP2 = normalize(dirOriginToP2);
		
		Point enter = null, exit = null;
		//in the same direction
		if(Math.abs(dirOriginToP1[0] - dirOriginToP2[0]) < 0.001d &&
		   Math.abs(dirOriginToP1[1] - dirOriginToP2[1]) < 0.001d && 
		   Math.abs(dirOriginToP1[2] - dirOriginToP2[2]) < 0.001d) {
			Point origin = new Point(x0, y0, z0);
			double dist1 = p1.distanceFrom(origin), dist2 = p2.distanceFrom(origin);
			enter = (dist1 < dist2) ? p1 : p2;
			exit = (dist1 > dist2) ? p1 : p2;
		} else { //opposite dir(when ray is inside box)
			
			if(Math.abs(dirOriginToP1[0] - dx) < 0.001d && 
			   Math.abs(dirOriginToP1[1] - dy) < 0.001d && 
			   Math.abs(dirOriginToP1[2] - dz) < 0.001d) {
				exit =  p1;
				enter = p2;
			} else {
				exit =  p2;
				enter = p1;
			}
		}
		return new Point[]{enter, exit};	
	}

	/**
	 * Finds intersection of this ray with given splitting plane 
	 * for a kd-tree node.
	 * 
	 * Equation of plane : Ax + By + Cz + D = 0
	 * 
	 * @param sp	SplitPlane object containing its position and dimensions
	 * @return		intersection point(if any)
	 */
	public Point intersectsSplitPlane(SplitPlane sp) {
		if(sp == null) return null;
		
		double[] norm = sp.normal;
		
		double A = norm[0], B = norm[1], C = norm[2]; 
		double x = sp.xMax, y = sp.yMax, z = sp.zMax; //a point on the plane
		double D = -(x*A + y*B + z*C);
		if((A*dx + B*dy + C*dz) == 0) return null; //ray is parallel to plane
		double t = -(double)(A*x0 + B*y0 + C*z0 + D)/(A*dx + B*dy + C*dz);
		
		Point p = new Point(x0 + dx*t, y0 + dy*t, z0 + dz*t);
		p.setNormal(A, B, C);
		 			
		return p;
	}
	
	/**
	 * Finds out if this ray intersects given triangle.
	 * 
	 * @param tri		Triangle object
	 * @param light		LightSource object
	 * 
	 * @return			Intersection point
	 * 					null if no intersection occurs
	 */
	public Point intersectsTriangle(Triangle tri, LightSource light) {
		
		Point p0 = tri.p0, p1 = tri.p1, p2 = tri.p2;
		
		//(b1, b2) : barycentric coordinates
		// where 0 < b1 < 1, 0 < b2 < 1 and 0 < b1+b2 < 1
		//for a point P on the triangle :
		//P(b1, b2) = (1 - b1 - b2)p0 + b1*p1 + b2*p2
		//
		//ray : r = o + td
		//t : distance from ray origin to the triangle
		// o + t*d = (1 - b1 - b2)p0 + b1*p1 + b2*p2
		// b1*(p0 - p1) + b2(p0 - p2) + t*d = p0 - o
		// so, we have 3 unknowns : b1, b2 and t
		// writing above equation in matrix form :
		// [p0-p1   p0 - p2   d][ b1 ] =  [ p0 - o ]
		//						[ b2 ]
		//						[ t  ]
		//solve for u, v and t :
		
		Matrix m1 = new Matrix(new double[][]{
			 {p0.x - p1.x, p0.x - p2.x, dx},
			 {p0.y - p1.y, p0.y - p2.y, dy},
			 {p0.z - p1.z, p0.z - p2.z, dz}});
		
		//check if the m1 is singular(non-invertible)
		//the determinant of a singular matrix is 0
		//here, m1 will be singular, if p0 - p1 and p0 - p2 = 0 in any 2 out of 3 dimensions
		//i.e. if p0, p1 and p2 are colinear
		if(m1.det() == 0) { 
			return null; 
		}
		
		Matrix m1Inv = m1.inverse();
		Matrix m2 = new Matrix(new double[][]{{p0.x - x0}, {p0.y - y0}, {p0.z - z0}});  
		Matrix m3 = m1Inv.times(m2);
		
		double b1 = m3.get(0, 0);
		double b2 = m3.get(1, 0);
		double t = m3.get(2, 0);
		
		if(b1 < 0 || b1 > 1) return null;
		if(b2 < 0) return null;
		if(b1 + b2 > 1) return null;
		if(t <= 0) return null;
		
		double px = x0 + t*dx;
		double py = y0 + t*dy;
		double pz = z0 + t*dz;
		Point p = new Point(px, py, pz);
		
		//interpolated normals
		if(tri.p0.normal != null && tri.p1.normal != null && tri.p2.normal != null) {
			double nx = (1 - b1 - b2)*tri.p0.normal[0] + b1*tri.p1.normal[0] + b2*tri.p2.normal[0];
			double ny = (1 - b1 - b2)*tri.p0.normal[1] + b1*tri.p1.normal[1] + b2*tri.p2.normal[1];
			double nz = (1 - b1 - b2)*tri.p0.normal[2] + b1*tri.p1.normal[2] + b2*tri.p2.normal[2];
			p.setNormal(nx, ny, nz);
		}
		else {
			//normal = (p1 - p0) cross (p2 - p0)
			Point p1Minusp0 = new Point(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
			Point p2Minusp0 = new Point(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
			
			double nx = p1Minusp0.y*p2Minusp0.z - p1Minusp0.z*p2Minusp0.y;
			double ny = -(p1Minusp0.x*p2Minusp0.z - p1Minusp0.z*p2Minusp0.x);
			double nz = p1Minusp0.x*p2Minusp0.y - p1Minusp0.y*p2Minusp0.x;
			
			p.setNormal(nx, ny, nz);
		}
		
		//interpolate texture
		if(tri.t0 != null) {//if surface has texture
			double u = (1 - b1 - b2)*tri.t0.u + b1*tri.t1.u + b2*tri.t2.u;
			double v = (1 - b1 - b2)*tri.t0.v + b1*tri.t1.v + b2*tri.t2.v;
			p.tex = new Texture(u, v);
			//p.tex = getTex(tri, p);
		} 
		
		if(p!=null) {
			double[] bumpNormal = p.normal;
			//double[] bumpNormal = tri.getBumpNormal(p.tex, p);
			double[] eye = {x0, y0, z0};
			p.color = tri.material.getColorForPoint(p, bumpNormal, eye, light);
		}
		
		return p;
		
	}
	
	public Texture getTex(Triangle tr, Point p) {
		double[] p0Minusp1 = {tr.p0.x - tr.p1.x, tr.p0.y - tr.p1.y, tr.p0.z - tr.p1.z};
		double[] p0Minusp2 = {tr.p0.x - tr.p2.x, tr.p0.y - tr.p2.y, tr.p0.z - tr.p2.z};
		double area = crossProductMag(p0Minusp1, p0Minusp2);
		double[] p0Minusp = {tr.p0.x - p.x, tr.p0.y - p.y, tr.p0.z - p.z};
		double[] p1Minusp = {tr.p1.x - p.x, tr.p1.y - p.y, tr.p1.z - p.z};
		double[] p2Minusp = {tr.p2.x - p.x, tr.p2.y - p.y, tr.p2.z - p.z};
		
		double area0 = (double)crossProductMag(p1Minusp, p2Minusp)/area;
		double area1 = (double)crossProductMag(p2Minusp, p0Minusp)/area;
		double area2 = (double)crossProductMag(p0Minusp, p1Minusp)/area;
		
		double u = tr.t0.u*area0 + tr.t1.u*area1 + tr.t2.u*area2;
		double v = tr.t0.v*area0 + tr.t1.v*area1 + tr.t2.v*area2;
		
		u=Math.abs(u%1); v=Math.abs(v%1);
		return new Texture(u, v);
	}
	
	
	private double crossProductMag(double[] A, double[] B) {
		double[] cross = new double[3];
		cross[0] = A[1]*B[2] - A[2]*B[1];
		cross[1] = -(A[0]*B[2] - A[2]*B[0]);
		cross[2] = A[0]*B[1] - A[1]*B[0];
		double mag = Math.sqrt(cross[0]*cross[0] + cross[1]*cross[1] + cross[2]*cross[2]);
		return mag;
	}
	
	 private double[] normalize(double[] A) {
    	double l = Math.sqrt(A[0]*A[0] + A[1]*A[1] + A[2]*A[2]);
    	if(l != 0) {
    		A[0] = (double)A[0]/l;
    		A[1] = (double)A[1]/l;
    		A[2] = (double)A[2]/l;
    	}
    	return A;
	}
	    
	//getter for ray origin
	public double[] getOrigin() {
		return new double[]{x0, y0, z0};
	}
	
	//getter for direction vector for this ray
	public double[] getDirection() {
		return new double[]{dx, dy, dz};
	}
	
	@Override
	public String toString() {
		return "Ray:\n" +
			   "\tO: "+x0+"  "+y0+"  "+z0 + "\n" +
			   "\tD: "+dx+"  "+dy+"  "+dz;		
	}
}
