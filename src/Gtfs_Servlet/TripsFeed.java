package Gtfs_Servlet;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TripsFeed extends AbstractFeed{
	private ArrayList<Trip> trips;

	private String FILE_NAME = "trips.txt";
	private String FEED_DIRECTORY = "google_transit_feed";

	private String _userdirectorypath;

	TripsFeed(String userdirectorypath,ResultSet result) throws NumberFormatException, IOException{
		_userdirectorypath = userdirectorypath;
		trips = new ArrayList<Trip>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long trip_id = result.getLong("trip_id");
				Long route_id = result.getLong("route_id");
				Long service_id = result.getLong("service_id");
				Long shape_id = result.getLong("shape_id");
				trips.add(new Trip(trip_id,route_id,service_id,shape_id));
				}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}



	/**
	 * 引数のユーザーフォルダのファイルに書き込む
	 * @param userdirectorypath
	 * @return
	 * @throws IOException
	 */
	public void writeDataToFile() throws IOException{
		File file = new File(_userdirectorypath+"/"+FEED_DIRECTORY+"/"+FILE_NAME);
//		if (checkBeforeWritefile(file)){
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println(Trip.getColumnName());
			for(Trip trip:trips){
				pw.println(trip.getString());
			}
			pw.close();
//		}
	}



	private static class Trip{
		private Long _id;
		private Long _routeid;
		private Long _serviceid;
		private Long _shapeid;
		private static String columnname = "route_id,service_id,trip_id,shape_id";

		Trip(Long id,Long routeid,Long serviceid,Long shapeid){
			_id = id;
			_routeid = routeid;
			_serviceid = serviceid;
			_shapeid = shapeid;
		}

		public Long getId(){return _id;}
		public Long getRouteId(){return _routeid;}
		public Long getServiceId(){return _serviceid;}
		public Long getShapeId(){return _shapeid;}

		public String getString(){
			String result;
			if(_shapeid == null){
				result = _routeid+","+_serviceid+","+_id+",";
			}else {
			result = _routeid+","+_serviceid+","+_id+","+_shapeid;}
			return result;
		}
		public static String getColumnName(){return columnname;}
	}



	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}

