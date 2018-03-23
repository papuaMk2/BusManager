package announce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AnnounceFeed {
	private ArrayList<Announce> announces;

	private String FILE_NAME = "announces.txt";
	private String FEED_DIRECTORY = "announce_feed";
	private String _userdirectorypath;

	AnnounceFeed(String userdirectorypath,ResultSet result) throws NumberFormatException, IOException{
		_userdirectorypath = userdirectorypath;
		announces = new ArrayList<Announce>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long announce_id = result.getLong("announce_id");
				Double announce_lat = result.getDouble("announce_lat");
				Double announce_lon = result.getDouble("announce_lon");
				String filename = result.getString("filename");
				String stop_sequences = String.valueOf(result.getInt("stop_sequences"));

				announces.add(new Announce(announce_id,filename,announce_lat,announce_lon,stop_sequences));
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
			pw.println(Announce.getColumnName());
			for(Announce announce:announces){
				pw.println(announce.getString());
			}
			pw.close();
	}


	public static class Announce{
		private Long _id;
		private double _longitude;
		private double _latitude;
		private String _filename;
		private String _stopSequences;
		private static String columnname = "announce_id,filename,latitude,longitude,stop_sequences";


		Announce(Long id,String filename, double latitude, double longitude,String stop_sequences){
			_id = id;
			_filename = filename;
			_longitude = longitude;
			_latitude = latitude;
			_stopSequences = stop_sequences;
		}

		public Long getId(){return _id;}
		public String getFileName(){return _filename;}
		public double getLatitude(){return _latitude;}
		public double getLongitude(){return _longitude;}
		public String getStopSequences(){return _stopSequences;}

		public String getString(){return _id+","+_filename+","+_latitude+","+_longitude+","+_stopSequences;}
		public static String getColumnName(){return columnname;}
	}
}
