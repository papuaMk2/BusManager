package Gtfs_Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ShapesFeed extends AbstractFeed{
	private ArrayList<Shape> shapes;

	private static String FILE_NAME = "shapes.txt";
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;
	/**
	 * ユーザーフォルダのパス
	 * @param feeddirectorypath
	 * @throws IOException
	 */
	ShapesFeed(String userdirectorypath,ResultSet result) throws IOException{
		_userdirectorypath = userdirectorypath;
		shapes = new ArrayList<Shape>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			while(result.next()){
				Long shape_id = result.getLong("shape_id");
				double shape_pt_lat = result.getDouble("shape_pt_lat");
				double shape_pt_lon = result.getDouble("shape_pt_lon");
				int shape_sequence = result.getInt("shape_pt_sequence");
				shapes.add(new Shape(shape_id,shape_pt_lat,shape_pt_lon,shape_sequence));
				}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}






	/**
	 * shapesリストを返す
	 * @return
	 */
	public ArrayList<Shape> getShapes(){return shapes;}






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
			pw.println(Shape.getColumnName());
			for(Shape stop_time:shapes){
				pw.println(stop_time.getString());
			}
			pw.close();
//		}
	}


	public static class Shape{
		private Long _shapeid;
		private double _latitude;
		private double _longitude;
		private int _shapesequence;
		private static String columnname = "shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence";



		public Shape(Long _shapeid, double _latitude, double _longitude, int _shapesequence) {
			this._shapeid = _shapeid;
			this._latitude = _latitude;
			this._longitude = _longitude;
			this._shapesequence = _shapesequence;
		}


		public Long get_shapeid() {
			return _shapeid;
		}

		public double get_latitude() {
			return _latitude;
		}

		public double get_longitude() {
			return _longitude;
		}

		public int get_shapesequence() {
			return _shapesequence;
		}

		public static String getColumnname() {
			return columnname;
		}

		public String getString(){return _shapeid+","+_latitude+","+_longitude+","+_shapesequence;}
		public static String getColumnName(){return columnname;}
	}


	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}
