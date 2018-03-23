import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeLibrary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.ryosuke.gtfs_realtime.FeedMessageProvider;

import announce.AnnounceUpdateList;
import utils.MySingleton;

@WebServlet(name = "date", urlPatterns = { "/date" })
public class Date extends HttpServlet{

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 HH:mm:ss");
		response.setContentType("text/html; charset=utf-8");
		response.getWriter().append("Served at: ").append(sdf.format(c.getTime()));
		response.getWriter().println("wawawa");
//		ServletContext _context = getServletConfig().getServletContext();
//		FeedMessageProvider provider = (FeedMessageProvider)_context.getAttribute("50");
//		System.out.println("listsize:"+GtfsRealtimeLibrary.createFeedMessageBuilder().build().getEntityCount());
//		provider.setVehiclePositions(GtfsRealtimeLibrary.createFeedMessageBuilder().build());

		
		
		
		//		HttpSession session = request.getSession(false);
//		int intervalTime = session.getMaxInactiveInterval();
//		response.getWriter().println("セッションの有効期限:"+intervalTime+"秒です。");
		response.getWriter().println("announce update");
//		File file = new File(MySingleton.getInstance().getUserDataDirectory()+"31/announce_update");
//	    AnnounceUpdateList list = null;
//	    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MySingleton.getInstance().getUserDataDirectory()+"31/"+MySingleton.getAnnounceUpdateName()))) {
//		    list = (AnnounceUpdateList)ois.readObject();
//		    ois.close();
//        } catch (IOException e) {
//        	response.getWriter().println("error");
//        	e.printStackTrace();
//        	return;
//        } catch (ClassNotFoundException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
//	    response.getWriter().println(list.toString());
//	    BCryptPasswordEncoder encorder = new BCryptPasswordEncoder(10);
//	    String hashedstr = encorder.encode("password");
//	    response.getWriter().println(hashedstr);

//		String filepath = "/home/iiya/public_html/bus/userdata/36/google_transit_feed/agency.txt";
//		String contents = "agency_name,agency_url,agency_timezone\ndemo,http://ilis.cis.ibaraki.ac.jp/~iiya/bus/controller/UserRegistration.php,Asia/Tokyo";
//		File file = new File(filepath);
//		FileWriter filewriter = new FileWriter(file);
//		filewriter.write(contents);
//		filewriter.close();
	}


	  private static int STRETCH_COUNT = 1000;

	  /*
	   * salt＋ハッシュ化したパスワードを取得
	   */
	  public static String getSaltedPassword(String password, String userId) {
	    String salt = getSha256(userId);
	    return getSha256(salt + password);
	  }

	  /*
	   * salt + ストレッチングしたパスワードを取得(推奨)
	   */
	  public static String getStretchedPassword(String password, String userId) {
	    String salt = getSha256(userId);
	    String hash = "";

	    for (int i = 0; i < STRETCH_COUNT; i++) {
	      hash = getSha256(hash + salt + password);
	    }

	    return hash;
	  }

	  /*
	   * 文字列から SHA256 のハッシュ値を取得
	   */
	  private static String getSha256(String target) {
	    MessageDigest md = null;
	    StringBuffer buf = new StringBuffer();
	    try {
	      md = MessageDigest.getInstance("SHA-256");
	      md.update(target.getBytes());
	      byte[] digest = md.digest();

	      for (int i = 0; i < digest.length; i++) {
	        buf.append(String.format("%02x", digest[i]));
	      }

	    } catch (NoSuchAlgorithmException e) {
	      e.printStackTrace();
	    }

	    return buf.toString();
	  }
}
