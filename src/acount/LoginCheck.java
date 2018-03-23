package acount;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCrypt;

import utils.MySingleton;

@WebServlet("/LoginCheck")
public class LoginCheck extends HttpServlet {

    protected Connection conn = null;

    public void init() throws ServletException{
//        String url = "jdbc:mysql://localhost/busApp";
//        String user = "busApp";
//        String password = "busAppPW";

        System.out.println("initialize...");

//        try {
//            Class.forName("com.mysql.jdbc.Driver").newInstance();
//            conn = DriverManager.getConnection(url+"?user="+user+"&password="+password+"&autoReconnect=true");
//        }catch (ClassNotFoundException e){
//            log("ClassNotFoundException:" + e.getMessage());
//        }catch (SQLException e){
//            log("SQLException:" + e.getMessage());
//        }catch (Exception e){
//            log("Exception:" + e.getMessage());
//        }
		try {
			conn = Pdo.getConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }

    @Override
    public void destroy(){
    	System.out.println("destroy...");
    	super.destroy();
    	System.out.println("destroy2...");
    	try {
			conn.close();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws IOException, ServletException{
    	doPost(request,response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException{
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

//		try {
//			conn = Pdo.getConnection();
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
        Boolean valid = null;
//		try {
//			System.out.println("login:"+conn);
//			valid = conn.isValid(MySingleton.getDbTimeout());
//		} catch (SQLException e1) {
//			// TODO 自動生成された catch ブロック
//			e1.printStackTrace();
//		}
//		valid = null;

		try {
			conn = Pdo.connectionCheck();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			valid = conn.isValid(MySingleton.getDbTimeout());
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

        System.out.println("LoginCheck test");
        Map<String,String[]> userdata = request.getParameterMap();

        String user = userdata.get("username")[0];
        String pass = userdata.get("password")[0];
/*        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
*/
        System.out.println("LoginCheck test2");
        HttpSession session = request.getSession(true);

        ////ぱすわーどフォーマット
//        pass = format(pass);
        System.out.println("start auth:"+user);
        String userfoldername = authUser(user, pass, session);
        System.out.println("auth completed:"+userfoldername);
        if (userfoldername != null){
            /* 認証済みにセット */
            session.setAttribute("login", "OK");

            /* 認証成功後は必ずMonthViewサーブレットを呼びだす */
//            response.sendRedirect("/schedule/MonthView");
            out.print(userfoldername);
        }else{
            /* 認証に失敗したら、ログイン画面に戻す */
            session.setAttribute("status", "Not Auth");
//            response.sendRedirect("/schedule/LoginPage");
            out.print("login miss");
        }
    }

    protected String authUser(String user, String pass, HttpSession session){
        if (user == null || user.length() == 0 || pass == null || pass.length() == 0){
            return null;
        }
        int retryCount = 3;
        do{
        try {
//            String sql = "SELECT * FROM userdata WHERE user = ? && pass = ?";
            String sql = "SELECT * FROM userdata WHERE user = ?";
//        	String sql = "SELECT * FROM userdata";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user);
//            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();
//            System.out.println("at LoginCheck result:"+rs.getFetchSize());
            if (rs.next()){
            	int userid = rs.getInt("id");
//                int roll = rs.getInt("roll");
                String username = rs.getString("user");
                String password = rs.getString("pass");
                if(BCrypt.checkpw(pass, format(password))){
                	//ログイン成功時

//                File filepath = new File(getServletConfig().getServletContext().getRealPath("/"+username));
                File filepath = new File(MySingleton.getInstance().getUserDataDirectory()+userid);
                filepath.mkdir();
                session.setAttribute("userid", Integer.toString(userid));
//                session.setAttribute("roll", Integer.toString(roll));
                session.setAttribute("username", username);

                session.setAttribute("userfoldername",userid);
                return String.valueOf(userid);
                }else{
                	return null;
                }
            }else{
                return null;
            }
        }catch (SQLException e){
        	e.printStackTrace();
        	String sqlState = e.getSQLState();
        	System.out.println("at LoginCheck error:"+sqlState);
//        	if (sqlState.equals("08S01")){
        	retryCount--;
//        	}else{
//        	retryCount = 0;
//        	}
        }
        }while(retryCount>0);
        return null;
    }

    protected String format(String str){
    	String result ="";
    	String substr = str.substring(0,7);
    	result = str.replace(substr, "$2a$10$");
    	return result;
    }
}
