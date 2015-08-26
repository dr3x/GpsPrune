package tim.prune.save;

import java.awt.Font;

public enum FontStyle{
	Normal {
		public String toString(){
			return "Normal";
		}
	}, Bold{
		public String toString(){
			return "Bold";
		}
	}, Italic{
		public String toString(){
			return "Italic";
		}
	}, BoldItalic{
		public String toString(){
			return "Bold & Italic";
		}
	};
	
	public static FontStyle fromString( String str ) {
		if( str != null && str.trim().length() > 0 ) {
			for( FontStyle fs : FontStyle.values() ) {
				if( fs.toString().equals(str) ) {
					return fs;
				}
			}
		}
		return FontStyle.Normal;
	}
	
	public int toJavaFontStyle() {
		switch (this) {
		case Bold:
			return Font.BOLD;
		case Italic:
			return Font.ITALIC;
		case BoldItalic:
			return Font.BOLD | Font.ITALIC;
		}
		return Font.PLAIN;
	}
	
	public static FontStyle toFontStyle( int style ) {
		FontStyle ret = Normal;
		if( (style & Font.ITALIC) > 0 ) {
			ret = Italic;
		}
		if( (style & Font.BOLD) > 0 ) {
			ret = ret == Italic ? BoldItalic : Bold;
		}
		return ret;
	}
}