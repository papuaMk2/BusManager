package announce;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Announce_TripFeed {
	private ArrayList<Announce_Trip> announce_trips;

	private String FILE_NAME = "trips.txt";
	private String FEED_DIRECTORY = "announce_feed";

	private String _userdirectorypath;

	Announce_TripFeed(String userdirectorypath, ResultSet result) throws NumberFormatException, IOException{
		_userdirectorypath = userdirectorypath;
		announce_trips = new ArrayList<Announce_Trip>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long announce_id = result.getLong("announce_id");
				Long trip_id = result.getLong("trip_id");
				int announce_sequence =result.getInt("announce_sequences");
				announce_trips.add(new Announce_Trip(trip_id,announce_id,announce_sequence));
				}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}


	/**
	 * 引数のユーザーディレクトリのファイルに書き込む
	 * @param userdirectorypath
	 * @return
	 * @throws IOException
	 */
	public void writeDataToFile() throws IOException{
		File filepath = new File(_userdirectorypath+"/"+FEED_DIRECTORY+"/"+FILE_NAME);
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath)));
			pw.println(Announce_Trip.getColumnName());
			for(Announce_Trip announce_trip:announce_trips){
				pw.println(announce_trip.getString());
			}
			pw.close();
	}


	public static class Announce_Trip{
		private Long _tripid;
		private Long _announceid;
		private int _announceSequence;
		private static String columnname = "trip_id,announce_id,announce_sequences";

		Announce_Trip(Long tripid,Long announce_id,int announce_sequence){
			_tripid = tripid;
			_announceid = announce_id;
			_announceSequence = announce_sequence;
		}


		public Long getId(){return _tripid;}
		public Long getAnnounceId(){return _announceid;}
		public String getString(){return _tripid+","+_announceid+","+_announceSequence;}
		public int getAnnounceSequence(){return _announceSequence;}
		public static String getColumnName(){return columnname;}
	}
}
