package Gtfs_Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RoutesFeed extends AbstractFeed{
	private ArrayList<Route> routes;

	private String FILE_NAME = "routes.txt";
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;

	RoutesFeed(String userdirectorypath,ResultSet result) throws NumberFormatException, IOException{
		_userdirectorypath = userdirectorypath;
		routes = new ArrayList<Route>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long route_id = result.getLong("route_id");
				String route_short_name = result.getString("route_short_name");
				String route_long_name = result.getString("route_long_name");
				int route_type = result.getInt("route_type");
				routes.add(new Route(route_id,route_short_name,route_long_name,route_type));
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
			pw.println(Route.getColumnName());
			for(Route route:routes){
				pw.println(route.getString());
			}
			pw.close();
	}


	private static class Route{
		private Long _id;
		private String _shortname;
		private String _longname;
		private String _routetype;
		private static String columnname = "route_id,route_short_name,route_long_name,route_type";


		Route(Long id,String shortname, String longname,int route_type){
			_id = id;
			_shortname = shortname;
			_longname = longname;
			_routetype = String.valueOf(route_type);
		}

		public Long getId(){return _id;}
		public String getShortName(){return _shortname;}
		public String getLongName(){return _longname;}

		public String getString(){return _id+","+_shortname+","+_longname+","+_routetype;}
		public static String getColumnName(){return columnname;}
	}


	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}
