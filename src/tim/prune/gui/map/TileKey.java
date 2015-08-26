package tim.prune.gui.map;

public class TileKey {

	private final int layer;
	private final int zoom;
	private final int x;
	private final int y;
	
	public TileKey(int layer, int zoom, int x, int y) {
		super();
		this.layer = layer;
		this.zoom = zoom;
		this.x = x;
		this.y = y;
	}

	public int getLayer() {
		return layer;
	}

	public int getZoom() {
		return zoom;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + layer;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + zoom;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileKey other = (TileKey) obj;
		if (layer != other.layer)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (zoom != other.zoom)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Tile[l=" + layer + ", z=" + zoom + ", x=" + x + ", y=" + y + "]";
	}
}
