package ca.utoronto.utm.paint;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;
/**
 * Parse a file in Version 1.0 PaintSaveFile format. An instance of this class
 * understands the paint save file format, storing information about
 * its effort to parse a file. After a successful parse, an instance
 * will have an ArrayList of PaintCommand suitable for rendering.
 * If there is an error in the parse, the instance stores information
 * about the error. For more on the format of Version 1.0 of the paint 
 * save file format, see the associated documentation.
 * 
 * @author 
 *
 */
public class PaintFileParser {
	private int lineNumber = 0; // the current line being parsed
	private String errorMessage =""; // error encountered during parse
	private PaintModel paintModel; 
	
	/**
	 * Below are Patterns used in parsing 
	 */
	private Pattern pFileStart=Pattern.compile("^PaintSaveFileVersion1.0$", Pattern.COMMENTS | Pattern.MULTILINE);
	
	private Pattern pCircleStart=Pattern.compile("^Circle$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pColor=Pattern.compile("^color:([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]),([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]),([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$", Pattern.COMMENTS | Pattern.MULTILINE);
	// check to make sure the given number is within the accepted RGB range between each comma
	private Pattern pFill=Pattern.compile("^filled:((true)|(false))$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pCentre=Pattern.compile("^center:[(][\\d]+,[\\d]+[)]$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pRadius=Pattern.compile("^radius:[\\d]+$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pCircleEnd=Pattern.compile("^EndCircle$", Pattern.COMMENTS | Pattern.MULTILINE);
	
	private Pattern pRectangleStart=Pattern.compile("^Rectangle$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pp1=Pattern.compile("^p1:[(][\\d]+,[\\d]+[)]$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pp2=Pattern.compile("^p2:[(][\\d]+,[\\d]+[)]$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pRectangleEnd=Pattern.compile("^EndRectangle$", Pattern.COMMENTS | Pattern.MULTILINE);
	
	private Pattern pSquiggleStart=Pattern.compile("^Squiggle$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pPoints=Pattern.compile("^points$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pPoint=Pattern.compile("^point:[(][\\d]+,[\\d]+[)]$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pEndPoints=Pattern.compile("^endpoints$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pSquiggleEnd=Pattern.compile("^EndSquiggle$", Pattern.COMMENTS | Pattern.MULTILINE);
	
	private Pattern pPolylineStart=Pattern.compile("^Polyline$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pPolylineEnd=Pattern.compile("^EndPolyline$", Pattern.COMMENTS | Pattern.MULTILINE);
	private Pattern pblank=Pattern.compile("^[\\s]*[\\t]*[\\n]*$");
	private Pattern pFileEnd=Pattern.compile("^EndPaintSaveFile$", Pattern.COMMENTS | Pattern.MULTILINE);

	
	/**
	 * Store an appropriate error message in this, including 
	 * lineNumber where the error occurred.
	 * @param mesg
	 */
	private void error(String mesg){
		this.errorMessage = "Error in line "+lineNumber+" "+mesg;
	}
	
	/**
	 * 
	 * @return the error message resulting from an unsuccessful parse
	 */
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	/**
	 * Parse the inputStream as a Paint Save File Format file.
	 * The result of the parse is stored as an ArrayList of Paint command.
	 * If the parse was not successful, this.errorMessage is appropriately
	 * set, with a useful error message.
	 * 
	 * @param inputStream the open file to parse
	 * @param paintModel the paint model to add the commands to
	 * @return whether the complete file was successfully parsed
	 */
	public boolean parse(BufferedReader inputStream, PaintModel paintModel) {
		this.paintModel = paintModel;
		this.errorMessage="";
		
		// During the parse, we will be building one of the 
		// following commands. As we parse the file, we modify 
		// the appropriate command.
		
		CircleCommand circleCommand = new CircleCommand(new Point(0, 0), 0); 
		RectangleCommand rectangleCommand = new RectangleCommand(new Point(0, 0), new Point(0, 0));
		SquiggleCommand squiggleCommand = new SquiggleCommand();
		PolyLineCommand polylineCommand = new PolyLineCommand();
	
		try {	
			int state=0; Matcher m; String l;
			
			this.lineNumber=0;
			while ((l = inputStream.readLine()) != null) {
				this.lineNumber++;
				System.out.println(lineNumber+" "+l+" "+state);
				m=pblank.matcher(l);
				if(!(m.matches())) {
					l = l.replaceAll("\\s+","").replaceAll("\\n+", "").replaceAll("\\t+", "");
					switch(state){
						case 0:
							m=pFileStart.matcher(l);
							if(m.matches()){
								state=1;
								break;
							}
							else if (l.strip().equals("\n")) {
								state = 0;
								break;
							}
							error("Expected Start of Paint Save File");
							return false;
						case 1: // Looking for the start of a new object or end of the save file
							m=pCircleStart.matcher(l);
							if(m.matches()){
								state=2; 
								break;
							}
							else if (l.strip().equals("\n")) {
								state = 1;
								break;
							}
							m=pRectangleStart.matcher(l);
							if(m.matches()) {
								state = 7;
								break;
							}
							m=pSquiggleStart.matcher(l);
							if(m.matches()) {
								state = 12;
								break;
							}
							m=pPolylineStart.matcher(l);
							if(m.matches()) {
								state = 17;
								break;
							}
							m=pFileEnd.matcher(l);
							if(m.matches()) {
								state= 22;
								break;
							}
							error("Expected start of shape object or end of file");
							return false;
				
						case 2: //Looking to see if the current line is a circle color (All other cases involving color work similarly)
							m=pColor.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":"); //["color", "r,g,b"]
								String[] tempS2 = tempS1[1].split(","); //["rValue", "gValue", "bValue"]
								// This will always work as the regex pattern ensure the string always has the right number of values and that they exist
								int r = Integer.parseInt(tempS2[0]);
								int g = Integer.parseInt(tempS2[1]);
								int b = Integer.parseInt(tempS2[2]);
								circleCommand.setColor(Color.rgb(r, g, b));
								state = 3;
								break;
							}
							error("Expected proper color formatting");
							return false;
						case 3: //Looking to see if the current line is a circle fill argument
							m=pFill.matcher(l);
							if(m.matches()) {
								String[] tempS = l.split(":"); //["filled", "true|false"] 
								circleCommand.setFill(Boolean.parseBoolean(tempS[1]));
								state = 4;
								break;
							}
							error("Expected proper fill formatting");
							return false;
						case 4: //Looking to see if the current line is a circle centre (All point object cases work similarly)
							m=pCentre.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].substring(1, tempS1[1].length() - 1).split(",");
								circleCommand.setCentre(new Point(Integer.parseInt(tempS2[0]), Integer.parseInt(tempS2[1])));
								state = 5;
								break;
							}
							error("Expected proper centre point formatting");
							return false;
						case 5: //Looking to see if the current line is a circle radius
							m=pRadius.matcher(l);
							if (m.matches()) {
								String[] tempS = l.split(":");
								circleCommand.setRadius(Integer.parseInt(tempS[1]));
								state = 6;
								break;
							}
							error("Expected proper radius formatting");
							return false;
						case 6:  //Looking to see if the current line is the end of the current circle object
							m=pCircleEnd.matcher(l);
							if(m.matches()) {
								this.paintModel.addCommand(circleCommand);
								circleCommand = new CircleCommand(new Point(0, 0), 0);
								state = 1;
								break;
							}
							error("Expected end of circle object");
							return false;
						case 7:  //Looking to see if the current line is a rectangle color
							m=pColor.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].split(",");
								int r = Integer.parseInt(tempS2[0]);
								int g = Integer.parseInt(tempS2[1]);
								int b = Integer.parseInt(tempS2[2]);
								rectangleCommand.setColor(Color.rgb(r, g, b));
								state = 8;
								break;
							}
							error("Expected proper color formatting");
							return false;
						case 8:  //Looking to see if the current line is a rectangle fill argument
							m=pFill.matcher(l);
							if(m.matches()) {
								String[] tempS = l.split(":");
								rectangleCommand.setFill(Boolean.parseBoolean(tempS[1]));
								state = 9;
								break;
							}
							error("Expected proper fill formatting");
							return false;
						case 9:  //Looking to see if the current line is a rectangle point p1
							m=pp1.matcher(l);
							if(m.matches()){
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].substring(1, tempS1[1].length() - 1).split(",");
								rectangleCommand.setP1(new Point(Integer.parseInt(tempS2[0]), Integer.parseInt(tempS2[1])));
								state = 10;
								break;
							}
							error("Expected proper p1 formatting");
							return false;
						case 10:   //Looking to see if the current line is a rectangle point p2
							m=pp2.matcher(l);
							if(m.matches()){
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].substring(1, tempS1[1].length() - 1).split(",");
								rectangleCommand.setP2(new Point(Integer.parseInt(tempS2[0]), Integer.parseInt(tempS2[1])));
								state = 11;
								break;
							}
							error("Expected proper p2 formatting");
							return false;
						case 11:  //Looking to see if the current line is the end of the current rectangle object
							m=pRectangleEnd.matcher(l);
							if(m.matches()) {
								this.paintModel.addCommand(rectangleCommand);
								rectangleCommand = new RectangleCommand(new Point(0, 0), new Point(0, 0));
								state = 1;
								break;
							}
							error("Expected end of rectangle object");
							return false;
						case 12:  //Looking to see if the current line is a squiggle color
							m=pColor.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].split(",");
								int r = Integer.parseInt(tempS2[0]);
								int g = Integer.parseInt(tempS2[1]);
								int b = Integer.parseInt(tempS2[2]);
								squiggleCommand.setColor(Color.rgb(r, g, b));
								state = 13;
								break;
							}
							error("Expected proper color formatting");
							return false;
						case 13:  //Looking to see if the current line is a squiggle fill
							m=pFill.matcher(l);
							if(m.matches()) {
								String[] tempS = l.split(":");
								squiggleCommand.setFill(Boolean.parseBoolean(tempS[1]));
								state = 14;
								break;
							}
							error("Expected proper fill formatting");
							return false;
						case 14: //Looking to see if the current line is the start of a points list
							m=pPoints.matcher(l);
							if(m.matches()) {
								state = 15;
								break;
							}
							error("Expected proper color formatting");
							return false;
						case 15: //Looking to see if the current line is a squiggle point or the end of the points list
							m=pPoint.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].substring(1, tempS1[1].length() - 1).split(",");
								squiggleCommand.add(new Point(Integer.parseInt(tempS2[0]), Integer.parseInt(tempS2[1])));
								break;
							}
							m=pEndPoints.matcher(l);
							if(m.matches()) {
								state = 16;
								break;
							}
							error("Expected proper point or end of point list");
							return false;
						case 16: //Looking to see if the current line is the end of the current squiggle object
							m=pSquiggleEnd.matcher(l);
							if(m.matches()) {
								this.paintModel.addCommand(squiggleCommand);
								squiggleCommand = new SquiggleCommand();
								state = 1;
								break;
							}
							error("Expected end of squiggle object");
							return false;
						case 17: //Looking to see if the current line is a color
							m=pColor.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].split(",");
								int r = Integer.parseInt(tempS2[0]);
								int g = Integer.parseInt(tempS2[1]);
								int b = Integer.parseInt(tempS2[2]);
								polylineCommand.setColor(Color.rgb(r, g, b));
								state = 18;
								break;
							}
							error("Expected proper color format");
							return false;
						case 18://Looking to see if the current line is a fill argument
							m=pFill.matcher(l);
							if(m.matches()) {
								String[] tempS = l.split(":");
								polylineCommand.setFill(Boolean.parseBoolean(tempS[1]));
								state = 19;
								break;
							}
							error("Expected proper fill format");
							return false;
						case 19://Looking to see if the current line starts the list of points for this polyline
							m=pPoints.matcher(l);
							if(m.matches()) {
								state = 20;
								break;
							}
							error("Expected proper start of point list");
							return false;
						case 20://Looking to see if the current line is a polyline point
							m=pPoint.matcher(l);
							if(m.matches()) {
								String[] tempS1 = l.split(":");
								String[] tempS2 = tempS1[1].substring(1, tempS1[1].length() - 1).split(",");
								polylineCommand.add(new Point(Integer.parseInt(tempS2[0]), Integer.parseInt(tempS2[1])));
								break;
							}
							m=pEndPoints.matcher(l);
							if(m.matches()) {
								state = 21;
								break;
							}
							error("Expected proper point or end of point list");
							return false;
						case 21://Looking to see if the current line is a color
							m=pPolylineEnd.matcher(l);
							if(m.matches()) {
								this.paintModel.addCommand(polylineCommand);
								polylineCommand = new PolyLineCommand();
								state = 1;
								break;
							}
							error("Expected proper end of polyline object");
							return false;
						case 22://Looking to see if any other lines exist past the file end
							error("Extra lines past expected end of file");
							return false;
							
								
						}
					}
				}
		}  catch (Exception e){
			error("Unexpected error, please verify integrity of file");
		}
		return true;
	}
}
