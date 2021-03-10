package ca.utoronto.utm.paint;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

public class PolyLineCommand extends PaintCommand{
	private ArrayList<Integer> pointsX=new ArrayList<Integer>();
	private ArrayList<Integer> pointsY=new ArrayList<Integer>();

	public void add(Point p){ 
		this.pointsX.add(p.x);
		this.pointsY.add(p.y);
		this.setChanged();
		this.notifyObservers();
	}
	public void remove(){ 
		this.pointsX.remove(this.pointsX.size() - 1);
		this.pointsY.remove(this.pointsY.size() - 1);
		this.setChanged();
		this.notifyObservers();
	}
	public void change(int mouseX, int mouseY){ 
		this.pointsX.set(this.pointsX.size() - 1, mouseX);
		this.pointsY.set(this.pointsY.size() - 1, mouseY);
		this.setChanged();
		this.notifyObservers();
	}
	public double[] getPointsX(){
		double[] xs = new double[this.pointsX.size()];
		for (int i = 0; i<this.pointsX.size(); i++) {
			xs[i] = (double)this.pointsX.get(i);
		}
		return xs; 
	}
	public double[] getPointsY(){ 
		double[] ys = new double[this.pointsY.size()];
		for (int i = 0; i<this.pointsY.size(); i++) {
			ys[i] = (double)this.pointsY.get(i);
		}
		return ys;
	}

	public String report() {
		int r = (int)(this.getColor().getRed() * 255);
		int g = (int)(this.getColor().getGreen() * 255);
		int b = (int)(this.getColor().getBlue() * 255);

		String s = "";
		s+="Polyline\n";
		s+="\tcolor:"+r+","+g+","+b+"\n";
		s+="\tfilled:"+this.isFill()+"\n";
		s+="\tpoints\n";
		for (int i = 0; i < this.pointsX.size(); i++) {
			s+="\t\tpoint:(" + this.pointsX.get(i) + "," + this.pointsY.get(i) + ")\n";
		}
		s+= "\tend points\n";
		s+="EndPolyline\n";
		return s;
	}

	@Override
	public void execute(GraphicsContext g) {
		double[] pointsX = this.getPointsX();
		double[] pointsY = this.getPointsY();
		int sizeX = this.pointsX.size();
		g.setStroke(this.getColor());
		g.strokePolyline(pointsX, pointsY, sizeX);
	}
}
