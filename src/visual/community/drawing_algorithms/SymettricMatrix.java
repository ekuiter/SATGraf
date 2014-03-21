package visual.community.drawing_algorithms;

public class SymettricMatrix {
	
	private double[] matrix;
	private int size;
	private int rows;

	public SymettricMatrix(int rows) {
		this.rows = rows;
		this.size = (rows * (rows + 1)) >> 1;
		
		if (this.size > 0)
			this.matrix = new double[size];
	}
	
	private int getIndex(int i, int j) {
		int index;
		
		if (i <= j)
			index = (i * rows) - ((i - 1) * i / 2) + j - i; 
		else
			index = (j * rows) - ((j - 1) * j / 2) + i - j;
		
		return index;
	}
	
	public double getValue(int i, int j) {		
		return matrix[getIndex(i, j)];
	}
	
	public void setValue(int i, int j, double value) {
		matrix[getIndex(i, j)] = value;
	}
	
	public void assign(double value) {
		for (int i = 0; i < size; i++) {
			matrix[i] = value;
		}
	}
	
	public int getRows() {
		return rows;
	}
}
