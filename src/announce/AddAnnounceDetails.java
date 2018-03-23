package announce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import acount.Pdo;
import announce.AnnounceList.AnnounceData;
import utils.MySingleton;

/**
 * Servlet implementation class AddRouteData
 */
@WebServlet("/AddAnnounceDetails")
public class AddAnnounceDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ServletContext _context = null;
	private String PROVIDERNAME = "announceFeed"; // _contextにインスタンスを登録するときに利用する名前
													// ＊例:userid:PROVIDENAME

	// private String ZIP_FILE_PATH = null; //圧縮したファイルの絶対パス
	// private String DIRECTORY_PATH = null; //圧縮するフォルダの絶対パス
	private String ROUTEINSTANCE = "routeinstance"; // sessionに関連付けられているルートインスタンスの名前
	private Connection conn;

	/**
	 * 例：圧縮後ファイルへの絶対パス
	 * CONTEXT_DIRECTORY+"/"+session.getAttribute("username")+"/"+ZIPFILE_NAME
	 */
	// private String ZIPFILE_NAME = "google_transit.zip"; //圧縮後のファイル名
	// private String DIRECTORY_NAME = "google_transit_feed"; //圧縮するフォルダのフォルダ名
	// private String CONTEXT_DIRECTORY = null;
	private String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddAnnounceDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("init in servlet");
		super.init(config);
		_context = getServletConfig().getServletContext();
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		// ZIP_FILE_PATH = _context.getRealPath("/"+ZIPFILE_NAME);
		// DIRECTORY_PATH = _context.getRealPath("/sample-feed");
		// _path = new File(_context.getRealPath("/"+ZIPFILE_NAME));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
		// response.setContentType("text/html; charset=utf-8");
		System.out.println("announce logincheck:" + session.getAttribute("login"));
		if (session.getAttribute("login") == null)
			return;
		try {
			conn = Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = request.getReader();
		String userid = (String) session.getAttribute("userid");
		// ユーザーディレクトリのパス
		String directory = MySingleton.getInstance().getUserDataDirectory() + "/"
				+ session.getAttribute("userfoldername");
		// AnnounceProvider provider;
		// announceproviderの取得
		// if((provider =
		// (AnnounceProvider)_context.getAttribute(userid+":"+PROVIDERNAME)) ==
		// null){
		// _context.setAttribute(userid+":"+PROVIDERNAME, provider = new
		// AnnounceProvider(directory));
		// }

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}

		Gson gson = new Gson();

		AnnounceList list = gson.fromJson(sb.toString(), AnnounceList.class);
		AnnounceProvider provider = new AnnounceProvider(directory);
		Pdo.insertAnnounces(userid, list);

		/**
		 * announcedata 取得
		 */
		provider.setAnnounces(Pdo.getAnnounceData(userid));
		provider.setAnnounce_Trips(Pdo.getAnnounceTripData(userid));

		provider.writeDataToFile();
		out.println(sb.toString());

	}
}