import com.org.entity.Entry;
import com.org.entity.SymbolTable;
import com.org.entity.TokenTable;
import com.org.entity.WordListBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilingServlet extends HttpServlet {
    /**
     * 常数
     */
    public static final int CONSTANT = 1;
    /**
     * 标识符
     */
    public static final int IDENTIFIER = 2;
    /**
     * 注释
     */
    public static final int NOTE = 3;
    /**
     * 字符常数
     */
    public static final int CHAR = 4;
    /**
     * 界符
     */
    public static final int DELIMITER = 5;
    /**
     * 未知字符
     */
    public static final int UNKNOW = 6;
    /**
     * 关键字、界符等表
     */
    public static Map<String, List<WordListBean>> entryMap = new HashMap<>();
    public List<TokenTable> tokenTableList;
    public List<SymbolTable> symbolTableList;

    @Override
    public void init() {
        String keyword= null;
        String delimeter = null;
        try {
            keyword = readToString("E:\\我的学习代码\\前端学习代码\\我的github项目\\compilingPrinciple\\src\\keyword.json");
            delimeter = readToString("E:\\我的学习代码\\前端学习代码\\我的github项目\\compilingPrinciple\\src\\delimeter.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject1 = JSONObject.fromObject(keyword);
        JSONArray jsonArr1 = JSONArray.fromObject(jsonObject1.getJSONArray("wordList"));
        JSONObject jsonObject2 = JSONObject.fromObject(delimeter);
        JSONArray jsonArr2 = JSONArray.fromObject(jsonObject2.getJSONArray("wordList"));

        Entry entry1 = (Entry)JSONObject.toBean(jsonObject1, Entry.class);
        Entry entry2 = (Entry)JSONObject.toBean(jsonObject2, Entry.class);

        List<WordListBean> wordListBeanList1 = (List<WordListBean>) JSONArray.toCollection(jsonArr1, WordListBean.class);
        entry1.setWordList(wordListBeanList1);
        List<WordListBean> wordListBeanList2 = (List<WordListBean>) JSONArray.toCollection(jsonArr2, WordListBean.class);
        entry2.setWordList(wordListBeanList2);

        entryMap.put(entry1.getType(), entry1.getWordList());//设置关键字
        entryMap.put(entry2.getType(), entry2.getWordList());//设置界符
    }

    public String readToString(String filePath) throws Exception {
        File file = new File(filePath);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s =bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        String str = sb.toString();
        return str;
    }

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
            String content = out.toString();//获取代码内容
            scanner(content);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描
     * @param content
     * @throws IOException
     */
    public void scanner(String content) throws IOException {
        int row = 0;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line = "";
        ArrayList<String> words = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            row++;
            int col = 0;
            String temp = "";
            int length = line.toCharArray().length;

            if(length > 0) {
                int sort = sort(line.charAt(col));
                if(line.charAt(col) != '\t') {
                    temp += line.charAt(col);
                }
                //一行未处理完
                while(col < length - 1) {
                    char ch = 0;
                    while(sort != DELIMITER && sort != -1 && col < length - 1) {//如果未到定界符、空格、换行，则继续读入下一字符
                        col += 1;
                        ch = line.charAt(col);
                        sort = sort(ch);
                        if(' '==ch || ch == '\t') {//读取的字符是空格或者换行符时,将sort标记为-1
                            sort = -1;
                        }
                        if(sort != DELIMITER && sort != -1) {//标识符
                           temp += line.charAt(col);
                        }
                    }
                    if(!"".equals(temp)) {//读取一个标识符完毕

                    }
                    if(sort==DELIMITER) {//读取界符完毕
                        words.add("" + ch);
                    }
                    temp = "";
                    sort = 0;
                }
            }
        }
    }

    /**
     * 识别标识符
     * @param temp
     */
    private int recog_id(String temp) {
        int sort = sort(temp.charAt(0));
        switch (sort) {
                case CONSTANT :
                    recog_id(temp);
                    break;
                case IDENTIFIER :
                    System.out.println(2);
                    break;
                case NOTE :
                    System.out.println(3);
                    break;
                case CHAR :
                    System.out.println(4);
                    break;
                case DELIMITER :
                    System.out.println(5);
                    break;
                case UNKNOW :
                    System.out.println(6);
                    break;
        }

        int token = isKeyWord(temp);
        return token;
    }

    /**
     * 识别数
     * @param temp
     * @return
     */
    private int recogdig(String temp) {
        return 0;
    }

    /**
     * 识别字符常数
     * @param temp
     * @return
     */
    private int recogstr(String temp) {
        return 0;
    }

    /**
     * 识别并去掉注释
     * @param temp
     * @param token
     * @return
     */
    private int handlecom(String temp, int token) {
        return 0;
    }

    /**
     * 是否在关键字中
     * @param temp
     * @return
     */
    private int isKeyWord(String temp) {
        List<WordListBean> wordListBeans = entryMap.get("keyword");
        for(WordListBean wordListBean : wordListBeans) {
            if(temp.equals(wordListBean.getWord())) {
                return Integer.parseInt(wordListBean.getToken());
            }
        }
        return 0;
    }

    /**
     * 对参数进行分类
     * @param ch
     * @return
     */
    public int sort(char ch) {
        /**
         * 数字
         */
        if(isdigit(ch)) {
            return CONSTANT;
        }
        /**
         * 字母
         */
        else if(isalpha(ch)) {
            return IDENTIFIER;
        }
        /**
         * 注释
         */
        else if(ch=='/') {
            return NOTE;
        }
        /**
         * 字符
         */
        else if(ch=='\'') {
            return CHAR;
        }
        /**
         * 界符
         */
        else if(isdelimeter(ch)) {
            return DELIMITER;
        }
        /**
         * 未识别
         */
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
        List<WordListBean> wordListBeans = entryMap.get("delimeter");
        for(WordListBean wordListBean : wordListBeans) {
            if(ch == wordListBean.getWord().charAt(0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是标字母
     * @param ch
     * @return
     */
    private boolean isalpha(char ch) {
        return Character.isLetter(ch);
    }

    /**
     * 常数判断：第一个字符是否是数字
     * @param ch
     * @return
     */
    private boolean isdigit(char ch) {
        return Character.isDigit(ch);
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

    public boolean isInSymbolTable(String str) {
        for(SymbolTable symbolTable : symbolTableList) {
            if(symbolTable.getName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public void insertIntoSymbolTable(String str, int token) {
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.setName(str);
        symbolTable.setToken(token);
        symbolTableList.add(symbolTable);
    }

    public void insertIntoTokenTable(String str, int token) {
        TokenTable tokenTable = new TokenTable();
        tokenTable.setWord(str);
        tokenTable.setToken(token);
        tokenTableList.add(tokenTable);
    }
}
