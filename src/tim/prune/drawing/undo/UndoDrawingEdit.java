package tim.prune.drawing.undo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tim.prune.App;
import tim.prune.DrawApp;
import tim.prune.drawing.Drawing;
import tim.prune.drawing.DrawingItem;
import tim.prune.undo.UndoException;
import tim.prune.undo.UndoOperation;

public class UndoDrawingEdit implements UndoOperation {

	public static final int OP_CREATE = 0x0001;
	public static final int OP_EDIT = 0x0002;
	public static final int OP_DELETE = 0x0003;

	private String description;
	private long itemId;
	private int operation;
	private byte[] data;
	private byte[] redoData;
	
	public UndoDrawingEdit(int operation, String description, 
			DrawingItem drawingItem) {
		setOperation(operation);
		setItemId(drawingItem.getId());
		setDescription(description);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			drawingItem.writeExternal(oos);
			oos.flush();
		} catch (IOException ignoredCauseItWontHappenhere) {}
		setData(baos.toByteArray());
	}
	
	public UndoDrawingEdit(int operation, DrawingItem drawingItem) {
		this( operation, null, drawingItem );
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public long getItemId() {
		return itemId;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public int getOperation() {
		return operation;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setRedoItem(DrawingItem redoItem) {
		if (redoItem != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				redoItem.writeExternal(oos);
				oos.flush();
			} catch (IOException ignoredCauseItWontHappenhere) {}
			setRedoData(baos.toByteArray());
		}
	}

	public void setRedoData(byte[] redoData) {
		this.redoData = redoData;
	}

	public byte[] getRedoData() {
		return redoData;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		switch (operation) {
		case UndoDrawingEdit.OP_CREATE:
			return "add drawing";
		case UndoDrawingEdit.OP_DELETE:
			return "delete drawing";
		case UndoDrawingEdit.OP_EDIT:
			if( description != null && !"".equals(description)) {
				return description;
			}
			return "edit drawing";
		}
		return "";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void performUndo(App app) throws UndoException {
		DrawApp drawApp = (DrawApp)app;
		Drawing drawing = drawApp.getDrawing();
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Class clazz = Class.forName(ois.readUTF());
			ois = new ObjectInputStream(new ByteArrayInputStream(data)); // reset not supported

			app.setUndoEnabled(false);
			switch (operation) {
			case UndoDrawingEdit.OP_CREATE:
				DrawingItem item = (DrawingItem)clazz.newInstance();
				item.readExternal(ois);
				drawing.removeItem(item);
				drawApp.getToolbox().deactivate();
				break;
			case UndoDrawingEdit.OP_DELETE:
				DrawingItem addItem = drawing.addItem(clazz);
				addItem.readExternal(ois);
				break;
			case UndoDrawingEdit.OP_EDIT:
				item = (DrawingItem)drawApp.getFactory().createItem(clazz);
				item.readExternal(ois);
				drawing.replaceItem(item);
				if( drawApp.getSelected() != null ) {
					drawApp.setSelected(item);
					if( drawApp.getToolbox().getActive() != null ) {
						drawApp.getToolbox().deactivate();
						drawApp.setSelected(item);
						drawApp.getToolbox().activateToolFor(item);
					}
				}
				break;
			}
			
			drawApp.getCanvas().repaint();
		} catch ( Exception ignored ) {
			ignored.printStackTrace();
		} finally {
			app.setUndoEnabled(true);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void performRedo(App app) throws UndoException {
		DrawApp drawApp = (DrawApp)app;
		Drawing drawing = drawApp.getDrawing();
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Class clazz = Class.forName(ois.readUTF());

			app.setUndoEnabled(false);
			switch (operation) {
			case UndoDrawingEdit.OP_CREATE:
				ois = new ObjectInputStream(new ByteArrayInputStream(data)); // reset not supported
				DrawingItem addItem = drawing.addItem(clazz);
				addItem.readExternal(ois);
				break;
			case UndoDrawingEdit.OP_DELETE:
				ois = new ObjectInputStream(new ByteArrayInputStream(data)); // reset not supported
				DrawingItem item = (DrawingItem)clazz.newInstance();
				item.readExternal(ois);
				drawing.removeItem(item);
				drawApp.getToolbox().deactivate();
				break;
			case UndoDrawingEdit.OP_EDIT:
				ois = new ObjectInputStream(new ByteArrayInputStream(redoData));
				item = (DrawingItem)drawApp.getFactory().createItem(clazz);
				item.readExternal(ois);
				drawing.replaceItem(item);
				if( drawApp.getSelected() != null ) {
					drawApp.setSelected(item);
					if( drawApp.getToolbox().getActive() != null ) {
						drawApp.getToolbox().deactivate();
						drawApp.setSelected(item);
						drawApp.getToolbox().activateToolFor(item);
					}
				}
				break;
			}
			drawApp.getCanvas().repaint();
		} catch ( Exception ignored ) {
			ignored.printStackTrace();
		} finally {
			app.setUndoEnabled(true);
		}
	}
}
