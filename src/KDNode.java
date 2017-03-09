import java.util.ArrayList;

/**
 * Class KDNode represents a kd-tree node.
 * It may be the root, an intermediate node or a leaf.
 * 
 * @author Kirti M D
 *
 */
public class KDNode {
	int level; //levels 0, 3, 6, ... are X
			   //levels 1, 4, 7, ... are Y
			   //levels 2, 5, 8, ... are Z
	
	Bounds bounds; 
	KDNode A, B; //two child nodes
	SplitPlane splitPlane;
	boolean isALeaf;
	ArrayList<Triangle> leaf; //list used only if this is a leaf node
	
	KDNode() {
		bounds = null;
		A = null;
		B = null;
		splitPlane = null;
		leaf = null;
	}
}
