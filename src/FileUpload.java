import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class FileUpload
 */
@WebServlet(name = "FileUploadTest", urlPatterns = { "/FileUploadTest" })
public class FileUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUpload() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub


		response.setContentType("text/html; charset=utf-8");
	    PrintWriter out = response.getWriter();
	    String path = getServletContext().getRealPath("announcedata");
	    File directory = new File(path);
	    directory.mkdirs();

	    request.setCharacterEncoding("utf-8");

	    out.println("upload test");
	    out.print(request.getDateHeader("Date"));
	    DiskFileItemFactory factory = new DiskFileItemFactory();
	    ServletFileUpload sfu = new ServletFileUpload(factory);

	    Enumeration<String> names = request.getParameterNames();
	    out.print(names.hasMoreElements());
	    while (names.hasMoreElements()){
	      String name = (String)names.nextElement();
	      out.println("parameter name:"+name);
	    }
	    out.println("message:"+request.getParameter("message"));
	    try {
	      List<FileItem> list = sfu.parseRequest(request);
	      Iterator<FileItem> iterator = list.iterator();

	      out.println("number of items:"+list.size());
	      while(iterator.hasNext()){
	        FileItem item = (FileItem)iterator.next();

	        out.println("file:"+item.getFieldName());

	        if(!item.isFormField()){
	        	String filename = item.getName();

	            if ((filename != null) && (!filename.equals(""))){
	              //ファイルが存在することの確認
	            filename = (new File(filename)).getName();
	            item.write(new File(path + "/" + filename));
//	            new File(path+"/"+filename).setLastModified()
	            out.println(item.getName()+" を "+path+"/"+filename+"に保存しました");



	            }
	        }
	      }
	    }catch (FileUploadException e) {
	      e.printStackTrace();
	    } catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        //test用セッション削除
		HttpSession session = request.getSession(false);
        session.invalidate();
        System.out.println("session destroyed");

	  }

}
