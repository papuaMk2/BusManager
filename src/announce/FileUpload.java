package announce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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

import utils.MySingleton;

/**
 * Servlet implementation class FileUpload
 */
@WebServlet(name = "FileUploadTest", urlPatterns = { "/FileUpload" })
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
	}

	/**
	 * 受け取ったファイルを保存、updatelistを更新・作成する
	 * アップデートリストの更新日時はうｐされたファイルから取得(ファイル名と更新日時が一致する)
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		HttpSession session = request.getSession(false);
		response.setCharacterEncoding("UTF-8");
//		response.setContentType("text/html; charset=utf-8");
		System.out.println("file upload logincheck:"+session.getAttribute("login"));
		if(session.getAttribute("login") == null ){
			response.getWriter().println("Time out");
			return;
		}
		response.setContentType("text/html; charset=utf-8");
	    PrintWriter out = response.getWriter();
	    String userdirectory = MySingleton.getInstance().getUserDataDirectory()+session.getAttribute("userid");
	    String announcedirectory = userdirectory+"/announcedata";
	    File directory = new File(announcedirectory);
	    directory.mkdirs();
	    directory = null;
	    request.setCharacterEncoding("utf-8");

	    out.println("upload test");

	    DiskFileItemFactory factory = new DiskFileItemFactory();
	    ServletFileUpload sfu = new ServletFileUpload(factory);

	    AnnounceUpdateList updatelist = null;
	    File announceupdatefile = new File(userdirectory+"/"+MySingleton.getAnnounceUpdateName());
	    announceupdatefile.createNewFile();

	    //すでにアップデートリストが存在する場合
	    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userdirectory+"/"+MySingleton.getAnnounceUpdateName()))) {
		    updatelist = (AnnounceUpdateList)ois.readObject();
		    ois.close();
        } catch (IOException/* | ClassNotFoundException*/ e) {
        	response.getWriter().println("error:"+e.toString());
        	if(e.toString() != "java.io.EOFException")return;
        } catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


	    if(updatelist == null){
	    	updatelist = new AnnounceUpdateList();
	    }

	    System.out.println("fileupload:"+updatelist.toString());

	    try {
	      List<FileItem> list = sfu.parseRequest(request);
	      Iterator<FileItem> iterator = list.iterator();

	      out.println("number of items:"+list.size());
	    out.println("hasnext:"+iterator.hasNext());
	      while(iterator.hasNext()){
	    	out.println("hasnext2:"+iterator.hasNext());
	        FileItem item = (FileItem)iterator.next();
	        out.println("filetype:"+item.isFormField());
	        if(!item.isFormField()){
	        	String filename = item.getName();
	        	out.println("filename:"+filename);
	            if ((filename != null) && (!filename.equals(""))){
		        	out.println("aaa");
	            	File file = new File(filename);
	              //ファイルが存在することの確認
	            filename = file.getName();
	            File newFile = new File(announcedirectory + "/" + filename);
	            item.write(newFile);
	            updatelist.put(filename, newFile.lastModified());
	            out.println(item.getName()+" を "+announcedirectory+"/"+filename+"に保存しました");
	            }
	        }else{
	        	out.println("fieldname:"+item.getFieldName());
	        }
	      }
	    }catch (FileUploadException e) {
	      e.printStackTrace();
	    } catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userdirectory+"/"+MySingleton.getAnnounceUpdateName()))) {
            oos.writeObject(updatelist);
            oos.close();
    	    System.out.println("fileupload:"+updatelist.toString());
        } catch (IOException e) {
        }

	  }

	private Boolean isZip(String filename) {
		if (filename.lastIndexOf(".") != -1) {
			filename = filename.substring(filename.lastIndexOf("."));
		}
		Boolean result = false;
		System.out.println("downloadfile:" + filename);
		if (filename.equals(".zip")) {
			result = true;
		}
		return result;
	}


}
