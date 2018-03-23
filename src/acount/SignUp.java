package acount;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




@WebServlet("/SignUp")
public class SignUp extends HttpServlet{
	private String USERDATA_DIRECTORY = "/home/iiya/public_html/bus/userdata/";

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, FileNotFoundException{
//		String username = new String(request.getParameter("username").getBytes("8859_1"),"utf-8");
//		String url = request.getParameter("URL");
//		String timezone = request.getParameter("timezone");
		String foldername = request.getParameter("foldername");

		Connection conn = null;
		int userid =  Integer.parseInt(request.getParameter("userid"));
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		String sql = "SELECT * FROM userdata WHERE id =?";
		String username = "";
		String url = "";
		String timezone = "";
		try {
		PreparedStatement stmt = conn.prepareStatement(sql);

			stmt.setInt(1, userid);

		ResultSet result = stmt.executeQuery();

		while(result.next()){
			username = result.getString("user");
			url = result.getString("URL");
			timezone = result.getString("timezone");
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
//		System.out.println(username);
//		System.out.println(url);
//		System.out.println(timezone);
//		System.out.println(foldername);
		String userdirectory = USERDATA_DIRECTORY+foldername+"/"+"google_transit_feed";
		File filepath = new File(userdirectory);
        filepath.mkdirs();
        File file = new File(userdirectory+"/"+"agency.txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")));
			pw.println("agency_name,agency_url,agency_timezone");
			pw.println(username+","+url+","+timezone);
			pw.close();
	}
}
