package Gtfs_Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StopsFeed extends AbstractFeed{
	private ArrayList<Stop> stops;

	private static String FILE_NAME = "stops.txt";
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;

	/**
	 * ユーザーフォルダのパス
	 * @param feeddirectorypath
	 * @throws IOException
	 */
	StopsFeed(String userdirectorypath,ResultSet result) throws IOException{
		_userdirectorypath = userdirectorypath;
		stops = new ArrayList<Stop>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long stop_id = result.getLong("stop_id");
				String stop_name = result.getString("stop_name");
				Double stop_lat = result.getDouble("stop_lat");
				Double stop_lon = result.getDouble("stop_lon");
				stops.add(new Stop(stop_id,stop_name,String.valueOf(stop_lat),String.valueOf(stop_lon)));
				}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}


	/**
	 *
	 * @param userdirectorypath
	 * @return
	 * @throws IOException
	 */
	public void writeDataToFile() throws IOException{
		File filepath = new File(_userdirectorypath+"/"+FEED_DIRECTORY+"/"+FILE_NAME);
//		if (checkBeforeWritefile(file)){
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath),"UTF-8")));
			pw.println(Stop.getColumnName());
			for(Stop stop:stops){
				pw.println(stop.getString());
			}
			pw.close();
//		}
	}


	private static class Stop{
		private Long _id;
		private String _name;
		private String _lat;
		private String _lon;
		private static String columnname = "stop_id,stop_name,stop_lat,stop_lon";


		Stop(Long id,String name,String lat,String lon){
			_id = id;
			_name = name;
			_lat = lat;
			_lon = lon;
		}

		public Long getId(){return _id;}
		public String getName(){return _name;}

		public String getString(){return _id+","+_name+","+_lat+","+_lon;}
		public static String getColumnName(){return columnname;}
	}


	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}
