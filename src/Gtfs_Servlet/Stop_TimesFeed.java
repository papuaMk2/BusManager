package Gtfs_Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Stop_TimesFeed extends AbstractFeed{
	private ArrayList<Stop_Time> stop_times;

	private static String FILE_NAME = "stop_times.txt";
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;
	/**
	 * ユーザーフォルダのパス
	 * @param feeddirectorypath
	 * @throws IOException
	 */
	Stop_TimesFeed(String userdirectorypath,ResultSet result) throws IOException{
		_userdirectorypath = userdirectorypath;
		stop_times = new ArrayList<Stop_Time>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long trip_id = result.getLong("trip_id");
				String arrival_time = result.getString("arrival_time");
				String departure_time = result.getString("departure_time");
				Long stop_id = result.getLong("stop_id");
				int stop_sequence = result.getInt("stop_sequences");
				stop_times.add(new Stop_Time(trip_id,arrival_time,departure_time,stop_id,stop_sequence));
				}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}






	/**
	 * stop_timesリストを返す
	 * @return
	 */
	public ArrayList<Stop_Time> getStop_Times(){return stop_times;}






	/**
	 *
	 * @param userdirectorypath
	 * @return
	 * @throws IOException
	 */
	public void writeDataToFile() throws IOException{
		File filepath = new File(_userdirectorypath+"/"+FEED_DIRECTORY+"/"+FILE_NAME);
//		if (checkBeforeWritefile(file)){
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath)));
			pw.println(Stop_Time.getColumnName());
			for(Stop_Time stop_time:stop_times){
				pw.println(stop_time.getString());
			}
			pw.close();
//		}
	}


	public static class Stop_Time{
		private Long _tripid;
		private String _arrivaltime;
		private String _departuretime;
		private Long _stopid;
		private int _stopsequence;
		private static String columnname = "trip_id,arrival_time,departure_time,stop_id,stop_sequence";

		Stop_Time(Long tripid,String arrivaltime,String departuretime,Long stopid,Integer stopsequence){
			_tripid = tripid;
			_arrivaltime = arrivaltime;
			_departuretime = departuretime;
			_stopid = stopid;
			_stopsequence = stopsequence;
		}

		public Long getTripId(){return _tripid;}
		public String getArrivalTime(){return _arrivaltime;}
		public String getDepartureTime(){return _departuretime;}
		public Long getStopId(){return _stopid;}
		public Integer getStopSequence(){return _stopsequence;}

		public String getString(){return _tripid+","+_arrivaltime+","+_departuretime+","+_stopid+","+_stopsequence;}
		public static String getColumnName(){return columnname;}
	}


	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}
