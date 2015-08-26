package tim.prune.drawing;

public enum PathEffect {

	None(null), 
	Dash(new float[] {10f,6f}), 
	DashDot(new float[] {10f,6f,1f,6f});
	
	private final float[] pattern;
	
	PathEffect(float[] pattern) {
		this.pattern = pattern;
	}
	
	public float[] getPattern() {
		return pattern;
	}
}
