package tim.prune.drawing.undo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tim.prune.DrawApp;
import tim.prune.drawing.DrawingItem;
import tim.prune.drawing.Mutator;

public class ChangeInterceptor implements MethodInterceptor {

	private final Map<Method,Method> getters = new HashMap<Method, Method>();
	private final Map<Method,String> descriptions = new HashMap<Method, String>();
	private final DrawApp app;
	
	public ChangeInterceptor(DrawApp app) {
		this.app = app;
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		UndoDrawingEdit undo = null;
		if( app.isUndoEnabled() ) {
			
			Mutator mutator = method.getAnnotation(Mutator.class);
			Method getter = null;
			String description = null;
			
			// is there an accessor? If so, we should compare the old version
			if( !getters.containsKey(method) ) {
				String accessor = mutator.accessor();
				if( accessor != null && !"".equals(accessor) ) {
					getter = obj.getClass().getMethod(accessor);
				}
				getters.put(method, getter);
			} else {
				getter = getters.get(method);
			}
			
			if( getter != null ) {
				Object old = getter.invoke(obj);
				if( old == null && args[0] == null || 
						(old != null && args[0] != null && old.equals(args[0]) )) {

					// skip undo and dirty set
					return proxy.invokeSuper(obj, args);
				}
			}
			
			// is there a name? If so, use it for the undo action
			if( !descriptions.containsKey(method) ) {
				description = mutator.name();
				descriptions.put(method, description);
			} else {
				description = descriptions.get(method);
			}
			
			// something has changed, add some undo action
			undo = new UndoDrawingEdit(UndoDrawingEdit.OP_EDIT, description, (DrawingItem) obj);
		}
		
		try {
			Object ret = proxy.invokeSuper(obj, args);
			if( undo != null ) {
				undo.setRedoItem((DrawingItem) obj);
				app.getUndoStack().add(undo);
				app.getDrawing().setDirty(true);
			}
			return ret;
		} catch ( Exception e ) {
			throw e;
		}
	}
}
