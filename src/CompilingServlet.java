import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;

public class CompilingServlet extends HttpServlet {
    /**
     * 常数
     */
    private final Integer CONSTANT = 1;
    /**
     * 标识符
     */
    private final Integer IDENTIFIER = 2;
    /**
     * 注释
     */
    private final Integer NOTE = 3;
    /**
     * 字符常数
     */
    private final Integer CHAR = 4;
    /**
     * 界符
     */
    private final Integer DELIMITER = 5;
    /**
     * 未知字符
     */
    private final Integer UNKNOW = 6;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            request.setCharacterEncoding("utf-8");
            Part part = request.getPart("file");
            InputStream inputStream = part.getInputStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for(int n; (n = inputStream.read(b)) != -1;) {
                out.append(new String(b, 0, n, "utf-8"));
            }
            String content = out.toString();

            BufferedReader reader = new BufferedReader(new StringReader(content));
            String line = "";
            int row = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println(row++ + "行:" + line);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对参数进行分类
     * @param ch
     * @return
     */
    public int sort(char ch) {
        if(isdigit(ch)) {
            return CONSTANT;
        }
        else if(isalpha(ch)) {
            return IDENTIFIER;
        }
        else if(ch=='/') {
            return NOTE;
        }
        else if(ch=='\\') {
            return CHAR;
        }
        else if(isdelimeter(ch)) {
            return DELIMITER;
        }
        else {
            return UNKNOW;
        }
    }

    /**
     * 界符判断
     * @param ch
     * @return
     */
    private boolean isdelimeter(char ch) {
        return false;
    }

    /**
     * 是否是标识符
     * @param ch
     * @return
     */
    private boolean isalpha(char ch) {
        return false;
    }

    /**
     * 常数判断：第一个字符是否是数字
     * @param ch
     * @return
     */
    private boolean isdigit(char ch) {
        return false;
    }

    public String getFilename(Part part) {
        if (part == null) {
            return null;
        }
        String fileName = part.getHeader("content-disposition");
        if (isBlank(fileName)) {
            return null;
        }
        return substringBetween(fileName, "filename=\"", "\"");
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0)
            return true;
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null)
            return null;
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1)
                return str.substring(start + open.length(), end);
        }
        return null;
    }

}
