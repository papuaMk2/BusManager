package Gtfs_Servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import Gtfs_Servlet.RouteData.BusStation;
import acount.Pdo;
import utils.MySingleton;


/**
 * Servlet implementation class AddRouteData
 */
@WebServlet("/UpdateFeed")
public class UpdateFeed extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ServletContext _context = null;
	private String PROVIDERNAME = "provider"; //_contextにインスタンスを登録するときに利用する名前　＊例:userid:PROVIDENAME

//	private String ZIP_FILE_PATH = null;   //圧縮したファイルの絶対パス
//	private String DIRECTORY_PATH = null;  //圧縮するフォルダの絶対パス
	private String ROUTEINSTANCE = "routeinstance";  //sessionに関連付けられているルートインスタンスの名前

	private Logger _log = LoggerFactory.getLogger(AddRouteData.class);
	private Connection conn = null;
	/**
	 * 例：圧縮後ファイルへの絶対パス
	 * 　　CONTEXT_DIRECTORY+"/"+session.getAttribute("username")+"/"+ZIPFILE_NAME　
	 */
	private String ZIPFILE_NAME = "google_transit.zip";  //圧縮後のファイル名
	private String DIRECTORY_NAME = "google_transit_feed";  //圧縮するフォルダのフォルダ名
	private String CONTEXT_DIRECTORY = null;
	private String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";

	private static int DISTANCE = 20;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateFeed() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException{
		System.out.println("init in servlet");
		super.init(config);
		_context = getServletConfig().getServletContext();
//		ZIP_FILE_PATH = _context.getRealPath("/"+ZIPFILE_NAME);
//		DIRECTORY_PATH = _context.getRealPath("/sample-feed");
//		_path = new File(_context.getRealPath("/"+ZIPFILE_NAME));
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("addroute start");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
		try {

			conn = Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
        int userid = Integer.parseInt(request.getParameter("userid"));
		String userdatadirectory = MySingleton.getInstance().getUserDataDirectory()+userid;
        updateFile(userid,userdatadirectory);
	}


	private void updateFile(int userid,String userdirectorypath) throws IOException{
		RouteDataProvider provider = new RouteDataProvider(userdirectorypath);

		/**
		 * Agencyフィード作成
		 */

		/**
		 * Routesフィード作成
		 */
		provider.setRoutes(Pdo.getRoutes(userid));

		/**
		 * calendeフィード作成
		 */
		provider.setCalender(Pdo.getCalendars(userid));

		/**
		 * shapeフィード作成
		 */
		provider.setShapes(Pdo.getShapes(userid));

		/**
		 * tripsフィード作成
		 */
		provider.setTrips(Pdo.getTrips(userid));

		/**
		 * Stopsフィードの作成
		 */
		provider.setStops(Pdo.getStops(userid));

		/**
		 * stoptimes
		 */
		provider.setStop_Times(Pdo.getStop_times(userid));


		writeDataToFile(userdirectorypath, provider);
	}

	/**
	 * ファイルへの書き込みとフォルダの圧縮を行う
	 * @param directory ユーザーフォルダへの絶対パス
	 * @param route ルートインスタンス
	 * @throws IOException
	 */
	protected synchronized void writeDataToFile(String directory,RouteDataProvider provider) throws IOException {
	    String directorypath = directory+"/"+DIRECTORY_NAME; //圧縮するフォルダの絶対パス
	    String zipfilepath = directory+"/"+ZIPFILE_NAME;  //圧縮後のファイルの絶対パス
	    File newdirectory = new File(directorypath);
	    newdirectory.mkdirs();

	    provider.writeDataToFile();

	    provider.createZip(zipfilepath, directorypath);
//	    ZipCompressUtils.compressFileList(zipfilepath, );
//	    ZipCompressUtils.compressDirectory(zipfilepath, directorypath);
	  }

}
