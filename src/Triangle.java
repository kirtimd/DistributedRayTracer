import java.awt.Color;

/**
 * Class Triangle stores the three vertices, textures, normals, material properties
 * for each triangle from the 3d model
 * 
 * @author Kirti M D
 *
 */
public class Triangle {
		
	Point p0, p1, p2;
	Texture t0, t1, t2; //tex coordinates
	double[] normal; //triangle normal, calculated from vertex normals
	double area;
	double[] color;
	Point centroid; //used while sorting triangles
	Material material;
	private double[] dpdu, dpdv, dndu, dndv; //needed for bump mapping
	
	int smoothingGroup; //for interpolating normals by group
	        			//needed for sponza
	private String shapeName;
	boolean traversed; 
	
	
	Triangle(String n, Point pt0, Point pt1, Point pt2, 
			Texture tex0, Texture tex1, Texture tex2) {//,
			//double[] norm0, double[] norm1, double[] norm2) {
		this(pt0, pt1, pt2);

		shapeName = n;
		material = new Material();
		
		t0 = tex0; t1 = tex1; t2 = tex2;
		
		//calculate partial derivatives dpdu and dpdv
		double[] p1Minusp0 = {p1.x - p0.x, p1.y - p0.y, p1.z - p0.z};
		double[] p2Minusp0 = {p2.x - p0.x, p2.y - p0.y, p2.z - p0.z};
		
		double u1Minusu0 = t1.u - t0.u, u2Minusu0 = t2.u - t0.u,
			   v1Minusv0 = t1.v - t0.v, v2Minusv0 = t2.v - t0.v;
		
		double det = u1Minusu0*v2Minusv0 - v1Minusv0*u2Minusu0;
		if(det != 0) {
			dpdu = new double[3]; dpdv = new double[3]; dndu = new double[3]; dndv = new double[3];		
			for(int i = 0; i < 3; i++) {
				dpdu[i] = (double)(v2Minusv0*p1Minusp0[i] - v1Minusv0*p2Minusp0[i]);
				dpdv[i] = (double)(-u2Minusu0*p1Minusp0[i] + u1Minusu0*p2Minusp0[i]);	
				
				//partial derivative of normal. 
				//both are {0,0,0} since triangle is flat
				dndu[i] = 0; dndv[i] = 0;
			}
						
		}
	}
	
	
	Triangle(Point pt0, Point pt1, Point pt2) {//assume vertices in anti-clockwise order
		
		p0 = pt0; p1 = pt1; p2 = pt2;
		
		//calculate triangle normal
		//normal = AB x AC
		//       = (p1 - p0) cross (p2 - p0)
		Point p1Minusp0 = new Point(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
		Point p2Minusp0 = new Point(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
		double nx = p1Minusp0.y*p2Minusp0.z - p1Minusp0.z*p2Minusp0.y;
		double ny = -(p1Minusp0.x*p2Minusp0.z - p1Minusp0.z*p2Minusp0.x);
		double nz = p1Minusp0.x*p2Minusp0.y - p1Minusp0.y*p2Minusp0.x;
		
		//area = |AB x AC|/2
		area = Math.sqrt(nx*nx + ny*ny + nz*nz) / 2 ;
		
		if(normal == null) {
		//point normals are set for sponza
		//interpolate them to get triangle normal
		if(p0.normal != null && p1.normal != null && p2.normal != null) {
			setNormal((double)(p0.normal[0] + p1.normal[0] + p2.normal[0])/3,
					  (double)(p0.normal[1] + p1.normal[1] + p2.normal[1])/3,
					  (double)(p0.normal[2] + p1.normal[2] + p2.normal[2])/3);
		}
		
		else {
			setNormal(nx, ny, nz);
			//set normals for each point
			p0.setNormal(nx, ny, nz);
			p1.setNormal(nx, ny, nz);
			p2.setNormal(nx, ny, nz);
		}
		} else {
			
		}
		
		
		material = new Material();
		//default color
		color = new double[]{2, 0, 0};
		
		centroid = new Point((double)(p0.x+p1.x+p2.x)/3, (double)(p0.y+p1.y+p2.y)/3, (double)(p0.z+p1.z+p2.z)/3);
		
		smoothingGroup = 0;
	}
	
	//copy constructor
	Triangle(Triangle tri) {
		this(new Point(tri.p0), new Point(tri.p1), new Point(tri.p2));
	}
	
	//needed to adjust area after transformation
	public double getArea() {
		Point p1Minusp0 = new Point(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
		Point p2Minusp0 = new Point(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
		double nx = p1Minusp0.y*p2Minusp0.z - p1Minusp0.z*p2Minusp0.y;
		double ny = -(p1Minusp0.x*p2Minusp0.z - p1Minusp0.z*p2Minusp0.x);
		double nz = p1Minusp0.x*p2Minusp0.y - p1Minusp0.y*p2Minusp0.x;
		
		//area = |AB x AC|/2
		area = Math.sqrt(nx*nx + ny*ny + nz*nz) / 2 ;
		return area;
	}
	
	@Override
	public boolean equals(Object o) {
		Triangle t = (Triangle)o;
		if(p0.equals(t.p0) && p1.equals(t.p1) && p2.equals(t.p2)) {
			return true;
		}
		return false;
	}
    
    
	public void setNormal(double nx, double ny, double nz) {
    	double l = Math.sqrt(nx*nx + ny*ny + nz*nz);
    	nx = (double)nx/l;
    	ny = (double)ny/l;
    	nz = (double)nz/l;
    	normal = new double[]{nx, ny, nz};
    }
	
	
	public double[] getBumpNormal(Texture t, Point pt){//, double[] normal) {
		if(material.getBumpImage() == null) return pt.normal;
		
		if(dpdu == null || dpdv == null) return pt.normal;
		
		int width = material.getBumpImage().getWidth() , //row
			height = material.getBumpImage().getHeight() ; //column
		
		int iMax = width - 1, jMax = height - 1;
		
		t.u = Math.abs(t.u % 1);
		t.v = Math.abs(t.v % 1);

		int i = (int)Math.round(t.u*iMax),
			 j = (int)Math.round(t.v*jMax);
		
		Color c = new Color(material.getBumpImage().getRGB(i, j));//(width + (i - 1))%width, (height + (j - 1))%height)); 
		Color c_u = new Color(material.getBumpImage().getRGB((i + 1)%width, j)); 
		Color c_v = new Color(material.getBumpImage().getRGB(i, (j + 1)%height)); 
		

		double[] bump = {c.getRed(), c.getGreen(), c.getBlue()};
		double[] bump_u = {c_u.getRed(), c_u.getGreen(), c_u.getBlue()};
		double[] bump_v = {c_v.getRed(), c_v.getGreen(), c_v.getBlue()};
		
		for(int k = 0; k < 3; k++) {
			bump[k] = (double)bump[k]/255;
			bump_u[k] = (double)bump_u[k]/255;
			bump_v[k] = (double)bump_v[k]/255;
		}
		
		//calculating dpdx and dpdy needs the rx and ry rays		
		double[] p = {pt.x, pt.y, pt.z};	
		double d = -dotProduct(pt.normal, p);
		//will complete dpdx and dpdy calculations later
		//currently using neighbouring pixels for displacement: bump_u[k] - bump[k]
		
		double[] dpdu1 = new double[3], dpdv1 = new double[3];
		for(int k = 0; k < 3; k++) {
			dpdu1[k] = dpdu[k] + (bump_u[k] - bump[k])*pt.normal[k] + bump[k]*dndu[k]; //pbrt pg 494
			dpdv1[k] = dpdv[k] + (bump_v[k] - bump[k])*pt.normal[k] + bump[k]*dndv[k];
		}
		double[] newNormal = crossProduct(dpdu1, dpdv1);
		newNormal = normalize(newNormal);
		
		return newNormal;
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
	
	private double[] crossProduct(double[] A, double[] B) {
		double[] cross = new double[3];
		cross[0] = A[1]*B[2] - A[2]*B[1];
		cross[1] = -(A[0]*B[2] - A[2]*B[0]);
		cross[2] = A[0]*B[1] - A[1]*B[0];
		
		return cross;
	}
		
	private double dotProduct(double[] A, double[] B) {
		double dp = 0.0f;
		
		dp = A[0]*B[0] +A[1]*B[1] +A[2]*B[2];
		
		if(dp < 0.0f) dp = 0.0f;
		return dp;
	}
	
	@Override
	public String toString() {
		return "Triangle " + " :\n"+
				"\tp0: " + p0.x + ", " + p0.y + ", " + p0.z + "\n" +
				"\tp1: " + p1.x + ", " + p1.y + ", " + p1.z + "\n" +
				"\tp2: " + p2.x + ", " + p2.y + ", " + p2.z + "\n" +
				"\t N: " + normal[0] + ", " + normal[1] + ", " + normal[2] + "\n" +
				"\tArea: " + area;
	}
}
