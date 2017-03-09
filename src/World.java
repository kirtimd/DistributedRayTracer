
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class World initializes the ray tracer.
 * The camera position, image plane, kd-tree for the model are set up, 
 * and rays are initialized and traced.
 * 
 * 
 * @author Kirti M D
 * 
 */
public class World {

	
	/**
	 * Prompts user for path, and forwards the path and grid size
	 *  to initializeAndTrace()
	 *  
	 * @param args    command-line arguments(ignored)
	 */
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter path to the .obj file: ");
		String objFilePath = sc.nextLine(); //FileNotFoundEx handled in FileParser
				
		World w = new World();
		int grid = 3;//super-sampling grid size		
		w.initializeAndTrace(grid, objFilePath);
		sc.close();
	}	
	
	/**
	 * Load the model data, position camera and image plane.
	 * Initialize and trace rays and display the final image using calculates pixel values.
	 * 
	 * @param g    			size of grid for super-sampling
	 * @param objFilePath	path to the .obj and .mtl file containing the 3D model
	 */
	public void initializeAndTrace(int g, String objFilePath) {
		
		long startTime = System.currentTimeMillis();
		
		int width = 800, height = 600; //output image size
		
		//set camera position	
		double cx = 230, cy = 350, cz = 1100; 
		//double cx = -200, cy = 550, cz = 1200;//for soft shadows output
		
		double[] eye = new double[]{cx, cy, cz};
		
		double imagePlaneZ = 500; 
		
		double imagePlaneHt = height;
		double imagePlaneWidth = width;
		
		
		double imagePlaneXMin = cx - imagePlaneWidth*0.5d, imagePlaneXMax = cx + imagePlaneWidth*0.5d,
			   imagePlaneYMin = cy - imagePlaneHt*0.5d, imagePlaneYMax = cy + imagePlaneHt*0.5d;
		

		double[] lightPosition = new double[]{00,700,200};//{-100f, 700, 200f };
																
		double length = 0.25f, breadth = 0.25f;
		LightSource light = new LightSource(lightPosition, length, breadth, 2);
		
		double sc = 1;
		//transform for the whole scene	
		Transform transform = new Transform(sc, sc, sc, //scale
											350, 285, 0, //rotate y = 80
											0,0,0);//-350, 0, 0); //translate
		/*
		//for soft shadows 
		Transform transform = new Transform(sc, sc, sc, //scale
											0,280,0,  	 //rotate
											0,00,00); //translate				
		*/
		FileParser fp = new FileParser();
		Shape shape = fp.parseObjAndMtlFile(objFilePath);
		shape.interpolateNormals();
		System.out.println("Total no. of triangles/faces: " + shape.getNoOfFaces());
		
		shape.createKDTree();
		
		System.out.println("Field of view : \n"+
						   "\twidth  : "+imagePlaneWidth+"\n"+
						   "\theight : "+imagePlaneHt);

		int noOfRaysX = g*width;
		int noOfRaysY = g*height;
		Ray[][] rays = new Ray[noOfRaysX][noOfRaysY];
				
		Point[][] imagePlane = new Point[noOfRaysX][noOfRaysY];
		
		double[][] radianceR = new double[noOfRaysX][noOfRaysY];
		double[][] radianceG = new double[noOfRaysX][noOfRaysY];
		double[][] radianceB = new double[noOfRaysX][noOfRaysY];
		
		double xRange = (imagePlaneXMax - imagePlaneXMin);
		double yRange = (imagePlaneYMax - imagePlaneYMin);
		
		double incX = (double)xRange/noOfRaysX; 
		double incY = (double)yRange/noOfRaysY;
		Pixel[][] pixels = new Pixel[noOfRaysX][noOfRaysY];
		
		double m = imagePlaneXMin; //row
		for(int i = 0; i < noOfRaysX; i++) {
			double n = imagePlaneYMax; //column
			for(int j = 0; j < noOfRaysY; j++) {
				
				pixels[i][j] = new Pixel(m, n, m + incX, n - incY, imagePlaneZ);
				pixels[i][j].row = i; pixels[i][j].column = j;
				n = n - incY;
			}
			m = m + incX;	
		}
		
		//add jitter to image plane
		if(g > 1) {
			for(int i = 0; i < noOfRaysX; i++) {
				for(int j = 0; j < noOfRaysY; j++) {
						pixels[i][j].addJitter();
				}
			}
		}
		
		for(int i = 0; i < noOfRaysX; i++) {
			for(int j = 0; j < noOfRaysY; j++) {
				imagePlane[i][j] = pixels[i][j].position;
			}
		}
		
		//initialize ray origin and direction
		for(int i = 0; i < noOfRaysX; i++) {
			for(int j = 0; j < noOfRaysY; j++) {
				rays[i][j] = new Ray(eye[0], eye[1], eye[2], imagePlane[i][j].x - eye[0], imagePlane[i][j].y - eye[1], imagePlane[i][j].z -  eye[2]);
			}			
		}
		
		System.out.println("Tracing...");
		//start tracing
		for(int i = 0; i < noOfRaysX; i++) {
			for(int j = 0; j < noOfRaysY; j++) {
				double[] finalColor = new double[3];
				
				Point p = shape.intersectedByRay(rays[i][j], light, transform);
				if(p != null) {				
					double[] shadowColor = new double[3];
					
					for(int k = 0; k < light.points.length; k++) {
						for(int l = 0; l < light.points[0].length; l++) {
							double t = 0.001f;
							double x0 = (1 - t)*p.x + t*light.points[k][l].x,
						    y0 = (1 - t)*p.y + t*light.points[k][l].y,
						    z0 = (1 - t)*p.z + t*light.points[k][l].z;
							double[] shadowDir = {light.points[k][l].x-x0, light.points[k][l].y-y0, light.points[k][l].z-z0};
							Ray shadowRay = new Ray(x0, y0, z0, shadowDir[0], shadowDir[1], shadowDir[2]);
							//find cos of angle between normal and shadow ray
							shadowDir = normalize(shadowDir);	
							double AdotB = dotProduct(p.normal, shadowDir);
							//cos(theta) = A dot B / |A| * |B|
							double cos = AdotB;
							if(cos <= 0 ) {//  <=
								double d = 0.5f;
								shadowColor[0]+=p.color[0]*d; shadowColor[1] += p.color[1]*d; shadowColor[2] += p.color[2]*d  ;
							} else {
								ArrayList<Point> intersectionPts = new ArrayList<Point>();
								
								Point p1 = shape.intersectedByRay(shadowRay, light, transform);
								intersectionPts.add(p1);
								Point p2 = light.points[k][l];//shadowRay.intersectsLightSource(light, transform);
								intersectionPts.add(p2);
								Point closest = p.closestIntersection(intersectionPts);
								if(closest != null)
									if(!closest.equals(p2)) {//if closest pt is not the light source, 
															 //then pt is not visible
										//make the color darker, to indicate shadow
										double d = 0.5f;
										shadowColor[0]+=p.color[0]*d; shadowColor[1] += p.color[1]*d; shadowColor[2] += p.color[2]*d  ;
								} else { //visible
									shadowColor[0] += p.color[0];shadowColor[1] += p.color[1]; shadowColor[2] += p.color[2];
								}
							}
						}//light j
					}//light i
					//shadow
						
					int noOfLightSamples = light.points.length * light.points[0].length;
					shadowColor[0] /= noOfLightSamples;
					shadowColor[1] /= noOfLightSamples;		
					shadowColor[2] /= noOfLightSamples;
							
					finalColor[0] = shadowColor[0];
					finalColor[1] = shadowColor[1];
					finalColor[2] = shadowColor[2];
					
					
				}	else finalColor = new double[]{0.815d,0.949d,1};//sky blue
				radianceR[i][j] += finalColor[0]; radianceG[i][j] += finalColor[1]; radianceB[i][j] += finalColor[2];
			}
		}
		
		//initialize the output image, whose pixels will be set using values calculated above
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		
		//set color
		double R, G, B;
		for(int i = 0; i < noOfRaysX - g; i+=g) {
			for(int j = 0; j < noOfRaysY - g; j+=g) {
				R = 0; G = 0; B = 0;
				
				//add all samples that lie inside one grid
				for(int k = 0; k < g; k++) {
					for(int p = 0; p < g; p++) {
						double sigma = Math.sqrt((double)1/(2*Math.PI));
						int dx = k - g/2, dy = p - g/2;
						double gaussian = (Math.exp(-(double)(dx*dx + dy*dy)/(2*sigma*sigma)));
						
						gaussian = (double)((gaussian - (-1))*(g - 0))/(1 - (-1)) + 0;
						gaussian = 1; //box
						R += gaussian*radianceR[i + k][j + p];
						G += gaussian*radianceG[i + k][j + p];
						B += gaussian*radianceB[i + k][j + p];
					}
				}
				
				//divide by g*g to get average color for the pixel
				R = (double)R/(g*g);  
				G = (double)G/(g*g);  
				B = (double)B/(g*g);  
				
				if(R > 1) R = 1; 
				if(G > 1) G = 1;
				if(B > 1) B = 1;
				
				image.setRGB(i/g, j/g, new Color((float)R, (float)G, (float)B).getRGB());
			}
		}
		
		//reconstruction
		//box filter for now
		int boxLength = 9; //keep value odd	
		double[][] R2 = radianceR, G2 = radianceG, B2 = radianceB;
		for(int i = boxLength/2; i < noOfRaysX - boxLength/2; i++) {
			for(int j = boxLength/2; j < noOfRaysY  - boxLength/2; j++) {
				
				for(int k = - boxLength/2; k <= boxLength/2; k++) {
					for(int l = - boxLength/2; l <= boxLength/2; l++) {
						R2[i][j] += radianceR[i + k][j + l];
						G2[i][j] += radianceG[i + k][j + l];
						B2[i][j] += radianceB[i + k][j + l];
					}
				}
				R2[i][j] = (double)R2[i][j]/(boxLength*boxLength);
				G2[i][j] = (double)G2[i][j]/(boxLength*boxLength);
				B2[i][j] = (double)B2[i][j]/(boxLength*boxLength);
				
			}
		}
				
		radianceR = R2; radianceG = G2; radianceB = B2;
		
		long finishTime = System.currentTimeMillis();
		long timeTaken = (finishTime - startTime);
		int minutes = (int)timeTaken/(60*1000); double seconds = (double)(timeTaken%(60*1000))/1000;
		String renderTime = "Rendered in " + minutes + " minutes, " + seconds + " seconds.";
		System.out.println(renderTime);
		
		Graphics2D g2d = image.createGraphics();
	    g2d.drawImage(image, 0, 0, null);
	    JFrame frame = new JFrame("Ray Tracing - Cornell Box");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	    panel.add(new JLabel(new ImageIcon(image)));
	    
	    JLabel infoLabel = new JLabel();
	    String info = renderTime;
	    info += "<br>Super-sampling grid size: " + g + " x " + g;
	    info += "<br>Focal plane at z = " + imagePlaneZ;
	    info += "<br>Camera at (" +eye[0]+", "+eye[1]+", "+eye[2]+")";
	    info += "<br>Light at (" + lightPosition[0] + ", " + lightPosition[1] + ", " + lightPosition[2] + ")";
	    infoLabel.setText("<html><body>" + info + "</body></html>");//using html tags for text formatting
	    
	    panel.add(infoLabel); 
	    frame.add(panel);
	    frame.repaint();
		frame.pack();
		frame.setVisible(true);
		frame.setTitle("The Sponza Model using Distributed Ray Tracing");
	}

	private double dotProduct(double[] A, double[] B) {
		double dp = A[0]*B[0] +A[1]*B[1] +A[2]*B[2];		
		//if product is less than 0, return 0
		if(dp < 0.0f) dp = 0.0f;
		return dp;
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
}
