package tim.prune.drawing;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import tim.prune.DrawApp;
import tim.prune.drawing.undo.ChangeInterceptor;

/**
 * 
 * @author mriley
 */
public class ItemFactory implements CallbackFilter {
	
	public static boolean isProxy( Class<?> clazz ) {
		return Enhancer.isEnhanced(clazz);
	}
	
	public static String getClassName( Class<?> clazz ) {
		return isProxy(clazz) ? getClassName(clazz.getSuperclass()) : clazz.getName();
	}
	
	public static String getClassName( DrawingItem item ) {
		return getClassName(item.getClass());
	}
	
	private final ChangeInterceptor interceptor;

	public ItemFactory(DrawApp app) {
		this.interceptor = new ChangeInterceptor(app);
	}

	@SuppressWarnings("unchecked")
	public <T extends DrawingItem> T createItem( Class<T> itemType ) {
		try {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(itemType);
			enhancer.setCallbackFilter(this);
			enhancer.setCallbacks(new Callback[] {NoOp.INSTANCE, interceptor});
			return (T) enhancer.create();
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int accept(Method method) {
		return method.isAnnotationPresent(Mutator.class) ? 1 : 0;
	}
}
