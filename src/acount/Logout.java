package acount;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Logout
 */
@WebServlet("/Logout")
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;


	private static String LOGOUT_MESSAGE = "logout";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Logout() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * リクエスト受け取ったらそのセッション破棄
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//session確認
		System.out.println("staet logout");
		HttpSession session = request.getSession(false);
		if(session == null){
			System.out.println("session dont exist");
			return;
		}

		int userid = Integer.parseInt((String)session.getAttribute("userid"));

		String trainNumber = null;
		if(request.getParameter("trainNumber") != null){
			trainNumber = request.getParameter("trainNumber");
		}
		try {
			Pdo.connectionCheck();
		} catch (InstantiationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		if(trainNumber != null){
			int vehicleid =  Pdo.getVehicleID(userid, trainNumber);
			Pdo.setVehicleIdEnabledState(vehicleid,true);
		}
		session.invalidate();
		System.out.println("session clear");
		response.getWriter().println(LOGOUT_MESSAGE);
	}

}
