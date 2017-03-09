import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class Material stores the material properties for each triangle and
 * performs shading calculations for all intersections.
 * 
 * @author Kirti M D
 *
 */
public class Material {
	String name;
	private int illum; //shading model type
	private double[] ka, //specular coefficient (individual values for R, G and B)
					 kd, //diffuse 
					 ks, //specular 
					 ke; //emissivity
			 
	private double ns, //specular exponent
				   ni, //refractive index
				   d;  //dissolve factor :
					   //0.0 : transparent, 1.0 : opaque 
			  		   //tr = 1 - d
	
	private double[] tf; //transmission factor
						 //specified in terms of RGB 
						 //allows only specific colors to pass through
						 //eg : {0 1 0} allows only green to pass through
	
	
	
	private double[] ambientColor, diffuseColor, specularColor; //used if no texture provided
	
	//texture
	private BufferedImage ambientTexImage, diffuseTexImage, specularTexImage, bumpImage; //png files
	
	//default color and coefficient values
	Material() {
		name = "default";
		ambientColor = new double[]{0.9d, 0.9d, 0.9d};
		specularColor = new double[]{0.8d, 0.8d, 0.7d};
		diffuseColor = new double[]{0.80d, 0.8d, 0.8d};
		ka = new double[]{0.7d, 0.7d, 0.7d};
		ks = new double[]{0.2, 0.2, 0.2};
		kd = new double[]{0.45d, 0.45d, 0.45d};
		ns = 16d;
	
	}
	
	//constructor used for 3d models
	Material(String name1, int illum1, double ns1, double ni1, double d1,
			 double[] ka1, double[] kd1, double[] ks1, double[] ke1, double[] tf1, 
			 BufferedImage ambiTex, BufferedImage diffTex, BufferedImage specTex, BufferedImage bump) {
		name = name1;		
		illum = illum1;
		ns = ns1;
		ni = ni1;
		d = d1;
		ka = ka1;
		kd = kd1;
		ks = ks1;
		ke = ke1;
		tf = tf1;
		ambientTexImage = ambiTex;
		diffuseTexImage = diffTex;
		specularTexImage = specTex;
		bumpImage = bump;
		
	}
	
	
	/**
	 *  Calculate color using Phong shading for given point
	 *  
	 * @param p				Intersection point 
	 * @param bumpNormal	bump normal calculated from the given image
	 * @param eye			position of the camera
	 * @param light			LightSource object
	 * 
	 * @return				RGB color for point p
	 */
	public double[] getColorForPoint(Point p, double[] bumpNormal, double[] eye, LightSource light) {
		double[] color = {0, 0, 0};//initialize
		
		//for all 
		for(int i = 0; i < light.points.length; i++) {
			for(int j = 0; j < light.points[0].length; j++) {
				
				double[] S = new double[]{light.points[i][j].x - p.x, light.points[i][j].y - p.y, light.points[i][j].z - p.z};
				S = normalize(S);
				
				double[] N = bumpNormal;//getBumpNormal(p.tex, p.normal);
				
				if(N == null) N = p.normal;
				ambientColor = mapTexture(p.tex, 0);	
				diffuseColor = mapTexture(p.tex, 1);
				
				double[] R = reflect(S, N); 
				R = normalize(R);
				double[] V = {eye[0] - p.x, eye[1] - p.y, eye[2] - p.z};
				V = normalize(V);
				double RdotV = dotProduct(R, V);
				
				for(int l = 0; l < 3; l++) {
					color[l] += (double)(ka[l]*ambientColor[l]*light.ambient[l] 
							+ kd[l]*diffuseColor[l]*dotProduct(S, N)*light.diffuse[l] 
							+ ks[l]*Math.pow(RdotV, ns)*light.specular[l]);
					
				}
					
			}
		}
		
		int noOfPts = light.points.length * light.points[0].length;
		double[] finalColor = {(double)color[0]/noOfPts, (double)color[1]/noOfPts, (double)color[2]/noOfPts};
		return finalColor;
	}
	
	
	/**
	 * Finds ambient/diffuse/specular color from corresponding images. 
	 * Performs texture mapping using bilinear interpolation.
	 * 
	 * @param t				texture co-ordinates(u, v)	
	 * @param imageType		type of texture image(ambient,diffuse or specular)
	 * @return				color at given (u, v)
	 */
	private double[] mapTexture(Texture t, int imageType) {
		BufferedImage image = null;
		switch(imageType) {
		case 0 : 
			image = ambientTexImage;
			break;
		case 1 :
			image = diffuseTexImage;
			break;
		case 2 :
			image = specularTexImage;
			break;
		}
		if(image == null) return new double[]{0,0,0};//ambientColor;
		
		//u and v values range from 0 to 1
		//we need to convert them to the image indices (0 to image row or column)
				
		int width = image.getWidth() , //row
			height = image.getHeight(); //column
		
		int iMax = width - 1, jMax = height - 1;
		
		
		t.u = 1 - t.u;
		t.v = 1 - t.v;
		t.u = Math.abs(t.u % 1);
		t.v = Math.abs(t.v % 1);
		double uInc = (double)1/width, vInc = (double)1/height;
		double u0 = 0, v0 = 0, u1 = 1, v1 = 1;
		while(u0 < t.u) {
			u0 = u0 + uInc;
		}
		if(u0 > t.u) u0 = u0 - uInc;
		u1 = u0 + uInc;
		
		while(v0 < t.v) {
			v0 = v0 + vInc;
		}
		if(v0 > t.v) v0 = v0 - vInc;
		v1 = v0 + vInc;
		
		int[] i = new int[4], j = new int[4];
		i[0] = (int)Math.round(u0*iMax) % width; j[0] = (int)Math.round(v0*jMax) % height;
		i[1] = (int)Math.round(u0*iMax) % width; j[1] = (int)Math.round(v1*jMax) % height;
		i[2] = (int)Math.round(u1*iMax) % width; j[2] = (int)Math.round(v0*jMax) % height;
		i[3] = (int)Math.round(u1*iMax) % width; j[3] = (int)Math.round(v1*jMax) % height;
							
		double deltaU = t.u - u0, deltaV = t.v - v0;
		double red = 0, green = 0, blue = 0;
		
		Color c0 = new Color(image.getRGB(i[0], j[0]));
		Color c1 = new Color(image.getRGB(i[1], j[1]));
		Color c2 = new Color(image.getRGB(i[2], j[2]));
		Color c3 = new Color(image.getRGB(i[3], j[3]));
		red = (1 - deltaU)*(1-deltaV)*c0.getRed() + deltaU*(1-deltaV)*c2.getRed()
				+ (1-deltaU)*deltaV*c1.getRed() + deltaU*deltaV*c3.getRed();
		red = (double)red/255;
		green = (1 - deltaU)*(1-deltaV)*c0.getGreen() + deltaU*(1-deltaV)*c2.getGreen()
				+ (1-deltaU)*deltaV*c1.getGreen() + deltaU*deltaV*c3.getGreen();
		green = (double)green/255;
		blue = (1 - deltaU)*(1-deltaV)*c0.getBlue() + deltaU*(1-deltaV)*c2.getBlue()
				+ (1-deltaU)*deltaV*c1.getBlue() + deltaU*deltaV*c3.getBlue();
		blue = (double)blue/255;
		double[] color = {red,green,blue};
		
		/*
		//average of the four pixels
		for(int k = 0; k < 4; k++) {
			Color c = new Color(ambientTexImage.getRGB(i[k], j[k])); //getRGB() returns an int representing a color
			red += (double)c.getRed()/255;
			green += (double)c.getGreen()/255;
			blue += (double)c.getBlue()/255;
		}
		*/
		
		return color;
		
	}
	
	/**
	 * Finds reflection vector, given the incident vector and surface normal
	 * 
	 * @param S			vector representing incident light
	 * @param N			surface normal
	 * @return			array containing the reflection vector
	 */
	private double[] reflect(double[] S, double[] N) {
		double[] R = new double[3];
		double SdotN = dotProduct(S, N);
		//R = 2(S.N)N - S
		R[0] = 2*SdotN*N[0] - S[0];
		R[1] = 2*SdotN*N[1] - S[1];
		R[2] = 2*SdotN*N[2] - S[2];
		return R;
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

    private double dotProduct(double[] A, double[] B) {
		double dp = A[0]*B[0] +A[1]*B[1] +A[2]*B[2];		
		//if product is less than 0, return 0
		if(dp < 0.0f) dp = 0.0f;
		return dp;
	}
    
    //this getter is used in Triangle class, while calculating bump normal
    public BufferedImage getBumpImage() {
    	return bumpImage;
    }
    
    
}
