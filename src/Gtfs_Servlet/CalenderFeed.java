package Gtfs_Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.tools.ant.types.FileList.FileName;

public class CalenderFeed extends AbstractFeed{
	private ArrayList<Calender> calenders;

	private static String FILE_NAME = "calendar.txt";
	private String FEED_DIRECTORY = "google_transit_feed";
	private String _userdirectorypath;


	CalenderFeed(String userdirectorypath, ResultSet result){
		_userdirectorypath = userdirectorypath;
		calenders = new ArrayList<Calender>();
		/**
		 * ファイルの読み込み処理
		 */
		try {
			ArrayList<Integer> weekList = new ArrayList<Integer>();
			while(result.next()){
				weekList = new ArrayList<Integer>();
				Long service_id = result.getLong("service_id");
				weekList.add(result.getInt("monday"));
				weekList.add(result.getInt("tuesday"));
				weekList.add(result.getInt("wednesday"));
				weekList.add(result.getInt("thursday"));
				weekList.add(result.getInt("friday"));
				weekList.add(result.getInt("saturday"));
				weekList.add(result.getInt("sunday"));
				String startdate = result.getString("startdate");
				String enddate = result.getString("enddate");
				calenders.add(new Calender(service_id,weekList,startdate,enddate));
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
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath)));
			pw.println(Calender.getColumnName());
			for(Calender calender:calenders){
				pw.println(calender.getString());
			}
			pw.close();
//		}
	}


	private static class Calender{
		private Long _serviceid;
		private ArrayList<Integer> _weekcalender; //月火水木金土日
		private String _startdate;
		private String _enddate;
		private static String columnname = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date";


		Calender(Long serviceid,ArrayList<Integer> weekcalender,String startdate,String enddate){
			_serviceid = serviceid;
			_weekcalender = new ArrayList<Integer>(weekcalender);
			_startdate = startdate;
			_enddate = enddate;
		}

		public Long getServiceId(){return _serviceid;}
		public ArrayList<Integer> getCalender(){return _weekcalender;}
		public String getStartDate(){return _startdate;}
		public String getEndDate(){return _enddate;}

		public String getString(){
			String weekstr="";
			for(Integer i:_weekcalender){
				weekstr=weekstr+i+",";
			}
			return _serviceid+","+weekstr+_startdate+","+_enddate;
			}

		public static String getColumnName(){return columnname;}
	}


	@Override
	public String getFileName() {
		// TODO 自動生成されたメソッド・スタブ
		return FILE_NAME;
	}
}
