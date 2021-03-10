package ca.utoronto.utm.paint;

import javafx.scene.input.MouseEvent;

class PolygonManipulatorStrategy extends ShapeManipulatorStrategy {
	PolygonManipulatorStrategy(PaintModel paintModel) {
		super(paintModel);
	}
	private PolyLineCommand polylineCommand = new PolyLineCommand();
	private boolean clicked = false;
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (!this.clicked) {
			this.clicked = true;
			this.polylineCommand.add(new Point((int)e.getX(), (int)e.getY()));
			this.polylineCommand.add(new Point((int)e.getX(), (int)e.getY()));
			this.addCommand(this.polylineCommand);
		}
		else {
			this.polylineCommand.add(new Point((int)e.getX(), (int)e.getY()));
		}
	}

	@Override
	public void mouseRightClicked(MouseEvent e) {
		if(this.clicked) {
			this.clicked = false;
			this.polylineCommand.remove();
			this.polylineCommand = new PolyLineCommand();
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		if (this.clicked) {
			this.polylineCommand.change((int)e.getX(), (int)e.getY());
		}
	}
}
