package Gtfs_Servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import utils.ZipCompressUtils;

public class RouteDataProvider {
	private volatile StopsFeed _stops;
	private volatile RoutesFeed _routes;
	private volatile TripsFeed _trips;
	private volatile Stop_TimesFeed _stop_times;
	private volatile CalenderFeed _calender;
	private volatile ShapesFeed _shapes;
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;

	RouteDataProvider(String userdirectorypath) throws IOException{
		_userdirectorypath = userdirectorypath;
	}

	public String getUserDirectoryPath(){return _userdirectorypath;}

	public void setStops(StopsFeed stops){
		_stops = stops;
	}
	public void setStops(ResultSet result) throws IOException{
		_stops = new StopsFeed(_userdirectorypath,result);
	}

	public void setRoutes(RoutesFeed routes){
		_routes = routes;
	}

	public void setRoutes(ResultSet result) throws IOException{
		_routes = new RoutesFeed(_userdirectorypath,result);
	}


	public void setTrip(TripsFeed trips){
		_trips = trips;
	}

	public void setTrips(ResultSet result) throws IOException{
		_trips = new TripsFeed(_userdirectorypath,result);
	}


	public void setStop_Times(Stop_TimesFeed stop_times){
		_stop_times = stop_times;
	}

	public void setStop_Times(ResultSet result) throws IOException{
		_stop_times = new Stop_TimesFeed(_userdirectorypath,result);
	}


	public void setCalender(CalenderFeed calender){
		_calender = calender;
	}

	public void setCalender(ResultSet result) throws IOException{
		_calender = new CalenderFeed(_userdirectorypath,result);
	}

	public void setShapes(ResultSet result){
		try {
			_shapes = new ShapesFeed(_userdirectorypath,result);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}


	public StopsFeed getStops(){
		return _stops;
	}

	public RoutesFeed getRoutes(){
		return _routes;
	}

	public TripsFeed getTrips(){
		return _trips;
	}

	public Stop_TimesFeed getStop_Times(){
		return _stop_times;
	}

	public CalenderFeed getCalender(){
		return _calender;
	}

	public ShapesFeed getShapes(){
		return _shapes;
	}

	public void writeDataToFile() throws IOException{
		_stops.writeDataToFile();
		_routes.writeDataToFile();
		_trips.writeDataToFile();
		_stop_times.writeDataToFile();
		_calender.writeDataToFile();
		_shapes.writeDataToFile();
	}

	/**
	 *
	 * @param zipfilepath 出力先
	 * @param directorypath 入力元
	 */
	public void createZip(String zipfilepath,String directorypath){
	    ArrayList<String> filelist = new ArrayList<String>();
	    filelist.add(directorypath+"/"+_routes.getFileName());
	    filelist.add(directorypath+"/"+_shapes.getFileName());
	    filelist.add(directorypath+"/"+_stops.getFileName());
	    filelist.add(directorypath+"/"+_stop_times.getFileName());
	    filelist.add(directorypath+"/"+_trips.getFileName());
	    filelist.add(directorypath+"/"+_calender.getFileName());
	    filelist.add(directorypath+"/"+"agency.txt");


	    ZipCompressUtils.compressFileList(zipfilepath, filelist);
	}

}
