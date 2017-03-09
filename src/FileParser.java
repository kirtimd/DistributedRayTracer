import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * Class FileParser contains methods to read obj and mtl files
 * 
 * @author Kirti M D
 */
public class FileParser {	

	/**
	 * Loads obj file and 
	 * @param objFilePath
	 * @return
	 *
	 * Note: The method currently has some code in the end specifically for the Sponza model.
	 */
	public Shape parseObjAndMtlFile(String objFilePath){//, String texFolderPath) {
		
		int i0 = objFilePath.lastIndexOf("/");
		String folderPath = objFilePath.substring(0, i0 + 1); //needed to get mtl file
		ArrayList<Point> vertices = new ArrayList<Point>();
		ArrayList<double[]> normals = new ArrayList<double[]>();
		ArrayList<Texture> textures = new ArrayList<Texture>();
		ArrayList<String> objNames = new ArrayList<String>();
		ArrayList<ArrayList<String>> objectsStr = new ArrayList<ArrayList<String>>(); //for storing input to be processed 
		ArrayList<Material> materials = new ArrayList<Material>();
		ArrayList<String> materialNames = new ArrayList<String>();
		ArrayList<ArrayList<Triangle>> objects = new ArrayList<ArrayList<Triangle>>();
		
		//the sponza is made of multiple objects/shapes
		//each object has a list of faces
		//a face maybe triangle or a polygon
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(objFilePath));
		
		
			String line = reader.readLine();
		
			while(!line.startsWith("mtllib")) { 
				line = reader.readLine();
			}
			//get mtl file name
			if(line.startsWith("mtllib")) { //mtllib sponza.mtl
				line = line.substring(7);
				String mtlFilePath = folderPath + line;
				materials = parseMTLFile(mtlFilePath);
				
				//store all material names in a separate array
				for(Material m : materials) {
					materialNames.add(m.name);
				}
			}
		
			//each object has v, vt and f data. 
			//the line after the f data ends is : # 96 polygons
			
			line = reader.readLine();
			while(line!= null) {
				//vertices : v1, v2, v3, ...
				if(line.startsWith("v ")) {
					double[] pts = stringToDoubleArray("v ", line);
					vertices.add(new Point(pts[0], pts[1], pts[2]));
				}
			
			    //texture : vt1, vt2, vt3, ... 
				if(line.startsWith("vt ")) {
					double[] pts = stringToDoubleArray("vt", line);
					textures.add(new Texture(pts[0], pts[1]));
				}
				
				//normals
				if(line.startsWith("vn ")) {
					double[] n = stringToDoubleArray("vn", line);
					normals.add(new double[]{n[0], n[1], n[2]});
					
				}
				
				//face data starts after all vertices have been read
				//each face : v1/vt1  v2/vt2  v3/vt3
				//face data starts with the line : g objName
				if(line.startsWith("g ")) {
					objNames.add(line.substring(2));
					line = reader.readLine();
					ArrayList<String> facesAndMtl = new ArrayList<String>();
					while(!line.startsWith("#")) {
						
							facesAndMtl.add(line);
						
						line = reader.readLine();
					}
					objectsStr.add(facesAndMtl);
				}
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		int noOfSmoothingGroups = 0;
		//create triangles for all objects	
		for(int i = 0; i < objectsStr.size(); i++) {
			ArrayList<Triangle> oneObject = new ArrayList<Triangle>();
			ArrayList<String> facesAndMtl = objectsStr.get(i);
			int noOfTri = 0, noOfPoly = 0;
			Material currentMat = null; int currentSmoothingGroup = 0;
			for(int j = 0; j < facesAndMtl.size(); j++) {
				String fm = facesAndMtl.get(j);
				if(fm.startsWith("usemtl")) {
					String matName = fm.substring(7);
					int matIndex = materialNames.indexOf(matName);
					if(matIndex != -1)
						currentMat = materials.get(matIndex);
					else System.out.println("Material "+matName +" not found.");
				}
				if(fm.startsWith("s")) {//smoothing group
					fm = fm.substring(2);
					if(fm.startsWith("off")) {currentSmoothingGroup = -1;}
					else currentSmoothingGroup = Integer.parseInt(fm);
					
					if(currentSmoothingGroup > noOfSmoothingGroups)
						noOfSmoothingGroups = currentSmoothingGroup;
				}
				if(fm.startsWith("f ")) {
					fm = fm.substring(2);
					String[] face = fm.split(" ");
					int[] v = new int[face.length], t = new int[face.length], n = new int[face.length];
					//get the vertex, texture and normal indices
					for(int k = 0; k < face.length; k++) { //face length = 3 or 4
						String[] vtn = face[k].split("/");
						v[k] = Integer.parseInt(vtn[0]);
						t[k] = Integer.parseInt(vtn[1]);
						if(vtn.length > 2) //if normal is given
							n[k] = Integer.parseInt(vtn[2]);
						//else normal will be calculated later in the Triangle constructor
						else n = null;
					}
					//Given indices start at 1. So subtract one
					if(v.length == 3) {
						Point p0 = vertices.get(v[0] - 1), p1 = vertices.get(v[1] - 1), p2 = vertices.get(v[2] - 1); 
						Texture tex0 = textures.get(t[0] - 1), tex1 = textures.get(t[1] - 1), tex2 = textures.get(t[2] - 1);
						if(normals.size() > 0){
							p0.normal = normals.get(n[0] - 1); p1.normal = normals.get(n[1] - 1); p2.normal = normals.get(n[2] - 1);
						}
						Triangle tr1 = new Triangle(objNames.get(i), p0, p1, p2, tex0, tex1, tex2);
						tr1.material = currentMat;
						tr1.smoothingGroup = currentSmoothingGroup;
						oneObject.add(tr1);
						noOfTri++;
					} else if(v.length == 4) { //polygon
						Point p0 = vertices.get(v[0] - 1), p1 = vertices.get(v[1] - 1),
							  p2 = vertices.get(v[2] - 1), p3 = vertices.get(v[3] - 1);
						Texture tex0 = textures.get(t[0] - 1), tex1 = textures.get(t[1] - 1), 
								tex2 = textures.get(t[2] - 1), tex3 = textures.get(t[3] - 1);

						if(normals.size() > 0) {
							p0.normal = normals.get(n[0] - 1); p1.normal = normals.get(n[1] - 1); 
							p2.normal = normals.get(n[2] - 1); p3.normal = normals.get(n[3] - 1);
						}
						Triangle tr1 = new Triangle(objNames.get(i), p0, p1, p3, tex0, tex1, tex3);
						Triangle tr2 = new Triangle(objNames.get(i), p1, p2, p3, tex1, tex2, tex3);
						
						//check if the vertices of either triangles are collinear
						//(this is happening for a few objects)
						//if the vertices are collinear, the triangle area = 0
						//if so, divide the quadrilateral using the other diagonal
						if(tr1.getArea() == 0 || tr2.getArea() == 0) {
							tr1 = new Triangle(objNames.get(i), p0, p1, p2, tex0, tex1, tex2);
							tr2 = new Triangle(objNames.get(i), p0, p2, p3, tex0, tex2, tex3);
						}
						
						tr1.material = currentMat; tr1.smoothingGroup = currentSmoothingGroup;
						tr2.material = currentMat; tr2.smoothingGroup = currentSmoothingGroup;
						
						oneObject.add(tr1);
						oneObject.add(tr2);
						noOfPoly++;
					} else if(v.length > 4) {
						System.out.println("****Polygon edges > 4");
					}
				}
			}
			objects.add(oneObject);
		}
		
		
		//to save time, remove some of the minor objects in the Sponza
		ArrayList<String> dontRender = new ArrayList<String>();
		String[] creepers = {"00","01","275","276","277","278","279","280","281"};
		dontRender.addAll(Arrays.asList(creepers));
		
		//curtains, rods and sockets
		for(int i=290;i<=329;i++) {
			//dontRender.add(i+"");
		}
		
		//lanterns
		for(int i=330;i<=365;i++) {
			dontRender.add(i+"");
		}
		
		//potted plants
		for(int i=366;i<=372;i++) {
			dontRender.add(i+"");
		}
		
		//lion faces
		dontRender.addAll(Arrays.asList(new String[]{"377","378","03"}));
		//dontRender.add("381"); // roof
		//flag poles with sockets
		for(int i=259;i<=274;i++) {
			//dontRender.add(i+"");
		}
				
		//the object ids start with "sponza_"
		for(int i = 0; i < dontRender.size(); i++) {
			String s = "sponza_"+dontRender.get(i);
			dontRender.set(i, s); 
		}
		
		
		System.out.println("Total no. of objects read: "+objects.size()); //381
		int objCount = 0;
		ArrayList<Triangle> faces = new ArrayList<Triangle>();
		for(int i = 0; i < objects.size(); i++) { 
			if(!dontRender.contains(objNames.get(i))) {
				objCount++;
				faces.addAll(objects.get(i));
			}
		}
		
		System.out.println("Total no. of objects in Shape: "+objCount); 
		Shape shape = new Shape(vertices, faces, noOfSmoothingGroups);
		return shape;
	}
	
	
	/**
	 * It reads all parameters(like texture images, 
	 * ambient, diffuse, specular coefficients) for each material type 
	 * from model's mtl file.
	 * 
	 * @param mtlFilePath 
	 * @return list of Material objects
	 */
	private ArrayList<Material> parseMTLFile(String mtlFilePath) {
		int i1 = mtlFilePath.lastIndexOf("/");
		String folderPath = mtlFilePath.substring(0, i1+1);
		ArrayList<Material> materials = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(mtlFilePath));
			materials = new ArrayList<Material>();
			String line = reader.readLine();
			Material mat = null;
			while(line != null) {
				//go to the first newmtl line
				if(line.startsWith("newmtl")) {
					String name = line.substring(6 + 1);
					double ns = 0, ni = 0, d = 0;
					double[] ka = new double[3], kd = new double[3], ks = new double[3], ke = new double[3], tf = new double[3];
					int illum = 0;
					BufferedImage ambientTexImage = null, diffuseTexImage = null, specularTexImage = null, bumpImage = null;
					line = reader.readLine();
					while(!line.equals("")) {
						line = line.trim();
						if(line.startsWith("Ns")) {
							double[] double1 = stringToDoubleArray("Ns", line); 
							ns = double1[0];
						}
						if(line.startsWith("Ni")) {
							double[] double1 = stringToDoubleArray("Ni", line); 
							ni = double1[0];
						}
						if(line.startsWith("d")) {
							double[] double1 = stringToDoubleArray("d", line); 
							d = double1[0];
						}
						if(line.startsWith("Tf")) {
							tf = stringToDoubleArray("Tf", line);
						}
						if(line.startsWith("Ka")) {
							ka = stringToDoubleArray("Ka", line);
						}
						if(line.startsWith("Kd")) {
							kd = stringToDoubleArray("Kd", line);
						}
						if(line.contains("Ks")) {
							ks = stringToDoubleArray("Ks", line);
						}
						if(line.startsWith("Ke")) {
							ke = stringToDoubleArray("Ke", line);
						}			
						if(line.startsWith("illum")) {
							int[] int1 = stringToIntArray("illum", line); 
							illum = int1[0];
						}
						if(line.contains("map_Ka")) {
							String parameter = "map_Ka";
							int i = line.indexOf(parameter) + parameter.length() + 1;
							String fileName = line.substring(i);
							ambientTexImage = readPNGFile(folderPath + fileName);
						}
						if(line.startsWith("map_Kd")) {
							String parameter = "map_Kd";
							int i = line.indexOf(parameter) + parameter.length() + 1;
							String fileName = line.substring(i);								
							diffuseTexImage = readPNGFile(folderPath + fileName);
						}
						if(line.startsWith("map_bump")) {
							String parameter = "map_bump";
							int i = line.indexOf(parameter) + parameter.length() + 1;
							String fileName = line.substring(i);
							bumpImage = readPNGFile(folderPath + fileName);
						}
						
						mat = new Material(name, illum, ns, ni, d, ka, kd, ks, ke, tf,
													ambientTexImage, diffuseTexImage, specularTexImage, bumpImage);
						
						line = reader.readLine();
						//if eof, get out of the while loop
						if(line == null) { line = ""; }
					} //while not end of newmtl
					if(mat != null) materials.add(mat);		
				} //if newmtl
				line = reader.readLine();
			} //while input not null
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return materials;
	}
	
	//Note: gi_flag.png not found in the model data
	public BufferedImage readPNGFile(String filePath) {
		filePath = filePath.replace("\\", "/");
		try {
			BufferedImage image = ImageIO.read(new File(filePath));
			return image;
		} catch (IOException e) {
			//System.out.println("Couldn't find file: " + filePath);
			//e.printStackTrace();
		}
		return null; //if file not read
	}
	
	/**
	 * Extracts double values from the string input
	 * 
	 * @param parameter   String containing name of the parameter
	 * @param line        A line from the file  
	 * @return 			  array of values(of type double)
	 */
	public double[] stringToDoubleArray(String parameter, String line) {
		
		int i = line.indexOf(parameter) + parameter.length() + 1;//+1 for the space 
		line = line.substring(i); 
		String[] arrStr = line.split(" ");
		double[] arr = new double[arrStr.length];
		for(int j = 0; j < arr.length; j++) {
			arr[j] = Double.parseDouble(arrStr[j]);
		}
		return arr;
	}
	
	/**
	 * Extracts int values from the string input
	 * 
	 * @param parameter   String containing name of the parameter
	 * @param line        A line from the file  
	 * @return 			  array of values(of type int)
	 */
	public int[] stringToIntArray(String parameter, String line) {
		int i = line.indexOf(parameter) + parameter.length() + 1;//1 for the space 
		line = line.substring(i); 
		String[] arrStr = line.split(" ");
		int[] arr = new int[arrStr.length];
		for(int j = 0; j < arr.length; j++) {
			arr[j] = Integer.parseInt(arrStr[j]);
		}
		return arr;
	}
}
