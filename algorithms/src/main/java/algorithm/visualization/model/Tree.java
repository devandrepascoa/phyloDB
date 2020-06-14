package algorithm.visualization.model;

import java.util.Arrays;

public class Tree {

	private Vertex[] roots;

	public Tree(Vertex[] roots) {
		this.roots = roots;
	}

	public Vertex[] getRoots() {
		return roots;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tree tree = (Tree) o;
		return Arrays.equals(roots, tree.roots);
	}

}
