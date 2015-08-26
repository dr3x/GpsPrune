package tim.prune.draw;

import java.util.HashMap;
import java.util.Map;

public class DrawStatistics {

	private final Map<Class<?>,TypeStat> stats = new HashMap<Class<?>, TypeStat>();
	
	public void addStat( Class<?> type, long time ) {
		TypeStat typeStat = stats.get(type);
		if( typeStat == null ) {
			typeStat = new TypeStat();
			stats.put(type, typeStat);
		}
		typeStat.time += time;
		typeStat.count++;
	}
	
	public void dump() {
		for( Map.Entry<Class<?>, TypeStat> e : stats.entrySet() ) {
			System.out.println(e.getKey().getSimpleName() + "\t" + e.getValue().count + "\t" + (e.getValue().time/e.getValue().count));
		}
	}
	
	private final class TypeStat {
		long time;
		int count;
	}
}
