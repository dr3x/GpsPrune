package tim.prune.gui.map;

import java.awt.image.BufferedImage;

public final class Tile {
	private final String _source;
	private final BufferedImage _image;
	private final TileKey _key;

	public Tile(String source, BufferedImage image, TileKey key) {
		this._source = source;
		this._image = image;
		this._key = key;
	}

	public Tile cloneWithKey(TileKey key) {
		return new Tile(_source, _image, key);
	}

	public BufferedImage getImage() {
		return _image;
	}

	public String getSource() {
		return _source;
	}

	public TileKey getKey() {
		return _key;
	}
}
