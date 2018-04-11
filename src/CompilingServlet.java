import com.org.entity.*;
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
    private static final int ID_TOKEN = 1212;//标识符token
    private static final int NUMBER_TOKEN = 1213;
    private static final int ERROR_CODE = 110;//错误
    private static final int INT_CODE = 10000;//整数
    private static final int NUMBER_CODE = 10001;//实数
    private static final int COM_TOKEN = 10002;//注释
    private static final int EXCEPT_TOKEN = 60;//除号
    private static final int CHAR_TOKEN = 10004;//字符常数
    /**
     * 关键字、界符等表
     */
    public static Map<String, List<WordListBean>> entryMap = new HashMap<>();
    public List<TokenTable> tokenTableList = new ArrayList<>();
    public List<SymbolTable> symbolTableList = new ArrayList<>();
    public List<ErrorWord> errorTableList = new ArrayList<>();


    @Override
    public void init() {
        String keyword = null;
        String delimeter = null;
        String calculate = null;
        try {
            keyword = readToString("E:\\我的学习代码\\前端学习代码\\我的github项目\\compilingPrinciple\\src\\keyword.json");
            delimeter = readToString("E:\\我的学习代码\\前端学习代码\\我的github项目\\compilingPrinciple\\src\\delimeter.json");
            calculate = readToString("E:\\我的学习代码\\前端学习代码\\我的github项目\\compilingPrinciple\\src\\calculate.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject1 = JSONObject.fromObject(keyword);
        JSONArray jsonArr1 = JSONArray.fromObject(jsonObject1.getJSONArray("wordList"));
        JSONObject jsonObject2 = JSONObject.fromObject(delimeter);
        JSONArray jsonArr2 = JSONArray.fromObject(jsonObject2.getJSONArray("wordList"));
        JSONObject jsonObject3 = JSONObject.fromObject(calculate);
        JSONArray jsonArr3 = JSONArray.fromObject(jsonObject3.getJSONArray("wordList"));

        Entry entry1 = (Entry) JSONObject.toBean(jsonObject1, Entry.class);
        Entry entry2 = (Entry) JSONObject.toBean(jsonObject2, Entry.class);
        Entry entry3 = (Entry) JSONObject.toBean(jsonObject3, Entry.class);

        List<WordListBean> wordListBeanList1 = (List<WordListBean>) JSONArray.toCollection(jsonArr1, WordListBean.class);
        entry1.setWordList(wordListBeanList1);
        List<WordListBean> wordListBeanList2 = (List<WordListBean>) JSONArray.toCollection(jsonArr2, WordListBean.class);
        entry2.setWordList(wordListBeanList2);
        List<WordListBean> wordListBeanList3 = (List<WordListBean>) JSONArray.toCollection(jsonArr3, WordListBean.class);
        entry3.setWordList(wordListBeanList3);

        entryMap.put(entry1.getType(), entry1.getWordList());//设置关键字
        entryMap.put(entry2.getType(), entry2.getWordList());//设置界符
        entryMap.put(entry3.getType(), entry3.getWordList());//设置运算符
        System.out.println("init completed");
    }

    public String readToString(String filePath) throws Exception {
        File file = new File(filePath);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
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
        try {
            request.setCharacterEncoding("utf-8");
            Part part = request.getPart("file");
            InputStream inputStream = part.getInputStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = inputStream.read(b)) != -1; ) {
                out.append(new String(b, 0, n, "utf-8"));
            }
            String content = out.toString();//获取代码内容
            scanner(content);
            sendToClient(request, response);
            this.tokenTableList = new ArrayList<>();
            this.symbolTableList = new ArrayList<>();
            this.errorTableList = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描
     *
     * @param content
     * @throws IOException
     */
    public void scanner(String content) throws IOException {
        int row = 0;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line = "";
        while ((line = reader.readLine()) != null) {
            row++;
            int col = 0;
            String temp = "";
            int length = line.toCharArray().length;

            if (length > 0) {
                int sort = sort(line.charAt(col));
                if (line.charAt(col) != '\t') {
                    temp += line.charAt(col);
                }
                //一行未处理完
                while (col < length - 1) {
                    char ch = 0;
                    while (sort != DELIMITER && sort != -1 && col < length - 1) {//如果未到定界符、空格、换行，则继续读入下一字符
                        col += 1;
                        ch = line.charAt(col);
                        sort = sort(ch);
                        if((temp.equals("/") && ch=='*') || ch=='/'){
                            String otherLine = line.substring(col);//剩余的字符串
                            int result = handlecom(otherLine);//识别除号或注释
                            if(result == EXCEPT_TOKEN) {//除号的种别码
                                System.out.println("不是/**/注释");
                            }
                            else {
                                col = length - 1;
                                sort = -1;
                                temp = "";
                            }
                        }
                        if (' ' == ch || ch == '\t') {//读取的字符是空格或者换行符时,将sort标记为-1
                            sort = -1;
                        }
                        if (sort != DELIMITER && sort != -1) {//标识符
                            temp += line.charAt(col);
                        }
                    }

                    if (!"".equals(temp)) {//读取一个标识符完毕
                        int type = sort(temp.charAt(0));
                        int result = 0;
                        int keyWord = isKeyWord(temp);
                        if (keyWord != 0) {
                            //关键字
//                            insertIntoSymbolTable(temp, keyWord, row);
                            insertIntoTokenTable(temp, keyWord);
                        } else {
                            if (type == IDENTIFIER) {
                                //标识符
                                result = recog_id(temp);
                            } else if (type == CONSTANT) {
                                result = recogdig(temp);
                            } else if (type == CHAR) {
                                //字符或字符串
                                result = recogstr(temp);
                            }
                            else if (type == NOTE) {
                                //单行注释
                                col = length - 1;
                                sort = -1;
                                temp = "";
//                                result = handlecom(temp);
                            }
                            else if (type == UNKNOW){
                                //未识别出的字符，查找java的相关字符表,
                                int token = isKeyWord(temp);
                                if(token!=0) {
                                    insertIntoTokenTable(temp, token);
                                }
                                else {
                                    result = ERROR_CODE;
                                }
                            }
                            else {
                                result = ERROR_CODE;
                            }
                            insertIntoSymbolTable(temp, result, row);
                            if (result != ERROR_CODE) {
                                insertIntoTokenTable(temp, result);
                            }
                            else {
                                insertIntoErrorTable(temp, row);
                            }
                        }
                        System.out.println(temp);
                        if (sort == DELIMITER) {//读取界符完毕
                            int delimeterToken = isdelimeter(""+ch);
                            if(type == IDENTIFIER) {
                                insertIntoSymbolTable(temp, delimeterToken, row);
                            }
                            insertIntoTokenTable(""+ch, delimeterToken);
                            System.out.println(ch);
                        }
                    }
                    temp = "";
                    sort = 0;
                }
            }
        }
    }

    private void insertIntoErrorTable(String temp, int row) {
        ErrorWord error = new ErrorWord();
        error.setIndex("" + row);
        error.setWord(temp);
        errorTableList.add(error);
    }

    private void sendToClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");

        JSONArray tokenArray = JSONArray.fromObject(tokenTableList);
        JSONArray errorArray = JSONArray.fromObject(errorTableList);
        JSONArray symbolArray = JSONArray.fromObject(symbolTableList);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", tokenArray);
        jsonObject.put("error", errorArray);
        jsonObject.put("symbol", symbolArray);
        Writer out = response.getWriter();
        out.write(jsonObject.toString());
        out.flush();
    }

    /**
     * 识别标识符
     *
     * @param temp
     */
    private int recog_id(String temp) {
        int state = 0;
        int colNo = 0;
        char ch = temp.charAt(colNo);
        while (state != 2) {
            switch (state) {
                case 0:
                    if (isalpha(ch)) state = 1;
                    else return ERROR_CODE;
                    break;
                case 1:
                    if ((isalpha(ch) || isdigit(ch)) && colNo < temp.length()-1) state = 1;
                    else state = 2;
            }
            if(colNo < temp.length() - 1) {
                ch = temp.charAt(colNo++);
            }
        }
        return ID_TOKEN;
    }

    /**
     * 识别数
     *
     * @param temp
     * @return
     */
    private int recogdig(String temp) {
        int state = 0;
        int colNo = 0;
        char ch = temp.charAt(colNo);
        while (state != 7) {
            switch (state) {
                case 0:
                    if (isdigit(ch)) state = 1;
                    else return ERROR_CODE;
                    break;
                case 1:
                    if (isdigit(ch) && colNo < temp.length()-1) state = 1;
                    else if (ch == '.') state = 2;
                    else {
                        return INT_CODE;
                    }
                    break;
                case 2:
                    if (isdigit(ch)) state = 3;
                    else return ERROR_CODE;
                    break;
                case 3:
                    if (isdigit(ch)) state = 3;
                    else if ((ch == 'E') || (ch == 'e')) state = 4;
                    else {
                        return NUMBER_CODE;//实数类型
                    }
                    break;
                case 4:
                    if (isdigit(ch)) state = 6;
                    else if (issign(ch)) state = 5;//读入+、-
                    else return ERROR_CODE;
                    break;
                case 5:
                    if(isdigit(ch)) state = 6;
                    else {
                        state = 7;
                        return ERROR_CODE;
                    }
                    break;
                case 6:
                    if(isdigit(ch)) state = 6;
                    else {
                        state = 7;
                        return NUMBER_CODE;
                    }
            }
            if(colNo < temp.length() - 1) {
                ch = temp.charAt(colNo++);
            }
        }
        return NUMBER_TOKEN;//返回数的token
    }

    private boolean issign(char ch) {
        if (ch == '+' || ch == '-') {
            return true;
        }
        return false;
    }

    /**
     * 识别字符常数
     * @param temp
     * @return
     */
    private int recogstr(String temp) {
        int state = 0;
        int colNo = 0;
        char ch = temp.charAt(colNo);
        while(state != 2) {
            switch (state) {
                case 0 :
                    state = 1;
                    break;
                case 1 :
                    if(ch == '\'' || ch == '\"') state = 2;
                    else state = 1;
            }
            if(colNo < temp.length() - 1) {
                ch = temp.charAt(colNo++);
            }
        }
        return CHAR_TOKEN;
    }

    /**
     * 识别并去掉注释
     * @param temp
     * @return
     */
    private int handlecom(String temp) {
        int state = 0;
        int colNo = 0;
        char ch = temp.charAt(colNo++);
        if(temp.length() <= 1) {
            return EXCEPT_TOKEN;
        }
        else {
            char ch1 = temp.charAt(colNo);
            if(ch=='*') {
                do {
                    ch1 = temp.charAt(colNo++);
                }while ((ch1 != '*') && colNo<temp.length());
                if(ch1 != '\n') {
                    ch1 = temp.charAt(colNo);
                    if(ch1 == '/') {
                        return COM_TOKEN;//返回注释的token
                    }
                }
                else {
                    return COM_TOKEN;
                }
            }
        }
        return EXCEPT_TOKEN;
    }

    /**
     * 是否在关键字中
     * @param temp
     * @return token
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
        else if(ch=='\'' || ch == '\"') {
            return CHAR;
        }
        /**
         * 界符
         */
        else if(isdelimeter(""+ch)!=0) {
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
     * @param temp
     * @return
     */
    private int isdelimeter(String temp) {
        List<WordListBean> wordListBeans = entryMap.get("delimeter");
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(temp)) {
                return Integer.parseInt(wordListBean.getToken());//返回界符
            }
        }
        List<WordListBean> wordListBeans1 = entryMap.get("calculate");
        for(WordListBean wordListBean : wordListBeans1) {
            if(wordListBean.getWord().equals(temp)) {
                return Integer.parseInt(wordListBean.getToken());//返回运算符
            }
        }
        return 0;
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

    /**
     * 检查是否在符号表中
     * @param str
     * @return
     */
    public boolean isInSymbolTable(String str) {
        for(SymbolTable symbolTable : symbolTableList) {
            if(str.equals(symbolTable.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 插入符号表
     * @param str
     * @param token
     */
    public void insertIntoSymbolTable(String str, int token, int row) {
        if(isInSymbolTable(str)) {
            return;
        }
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.setName(str);
        symbolTable.setToken(token);
        symbolTable.setAddr("" + row);
        symbolTable.setLength(str.length());
        symbolTableList.add(symbolTable);
    }

    public void insertIntoTokenTable(String str, int token) {
        TokenTable tokenTable = new TokenTable();
        tokenTable.setWord(str);
        tokenTable.setToken(token);
        tokenTableList.add(tokenTable);
    }
}
