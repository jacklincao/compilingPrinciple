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
    /**
     * 运算符
     */
    private static final int CALCULATE = 7;
    private static final int ID_TOKEN = 1212;//标识符token
    private static final int NUMBER_TOKEN = 1213;
    private static final int ERROR_CODE = 110110;//错误
    private static final int INT_CODE = 10000;//整数
    private static final int NUMBER_CODE = 10001;//实数
    private static final int COM_TOKEN = 10002;//注释
    private static final int EXCEPT_TOKEN = 60;//除号
    private static final int CHAR_TOKEN = 10004;//字符常数
    private static int colNo = 0;//当前的读取的列
    private static String nowLine = "";//当前读取的行

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
     * @param content
     * @throws IOException
     */
    public void scanner(String content) throws IOException {
        int row = 0;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line = "";
        while ((line = reader.readLine()) != null) {
            row++;
            colNo = 0;
            int length = line.toCharArray().length;//当前行的长度

            if (length > 0) {
                nowLine = line;
                System.out.println(row);
                if(row == 50) {
                    System.out.println(1);
                }
                while(length > 0 && line.charAt(colNo) == '\t'){
                    line = line.substring(1);
                    nowLine = line;
                    length = line.toCharArray().length;
                }
                if(length > 0) {
                    if (line.charAt(colNo) != '\t') {
                        while (colNo < length) {
                                char ch;
                                ch = line.charAt(colNo++);//第一个字符
                                int sort = sort(ch);
                                String hadRead = "" + ch;
                                //读入第一个字符,进行判断
                                if(sort == IDENTIFIER) {//进入标识符识别
                                    String id = recog_id(ch);//获取标识符
                                    if(!(""+ERROR_CODE).equals(id)) {//返回的不是错误码
                                        int token = isKeyWord(id);//0:不是关键字,非0:是关键字
                                        if(token == 0){
                                            if(!isInSymbolTable(id)) {//不在symbol中
                                                insertIntoSymbolTable(id, token, row);
                                            }
                                        }
                                        insertIntoTokenTable(id, token);
                                    }
                                    else {
                                        insertIntoErrorTable(id, row);
                                    }
                                }
                                else if(sort == CONSTANT) {//常数
                                    int preCol = colNo - 1;
                                    int number = recogdig(ch);
                                    int finCol = colNo;
                                    String id;
                                    if(preCol == finCol) {
                                        id = "" + ch;
                                    }
                                    else {
                                        id = nowLine.substring(preCol, finCol);
                                    }
                                    if(ERROR_CODE!=(number)) {//未出错情况
                                        if(!isInSymbolTable(""+number)) {
                                            insertIntoSymbolTable(id, number, row);
                                        }
                                        else {
                                            insertIntoTokenTable(id, number);
                                        }
                                    }
                                    else {
                                        insertIntoErrorTable(id, number);
                                    }
                                }
                                else if(sort == NOTE) {//注释
                                    int token = handlecom(ch);
                                    if(token == EXCEPT_TOKEN) {//等于除号
                                        insertIntoTokenTable("/", EXCEPT_TOKEN);
                                    }
                                    else {//是注释
                                        colNo = nowLine.length();
                                    }
                                }
                                else if(sort == DELIMITER) {//定界符
                                    //TODO 识别界符
                                    String str = recogdel(ch);
                                    if(!"ERROR".equals(str)) {
                                        int delimeterToken = getDelimeterToken(str);
                                        if(delimeterToken != 0) {//是界符
                                            insertIntoTokenTable(str, delimeterToken);
                                        }
                                    }
                                }
                                else if(sort == CALCULATE) {//运算符
                                    //TODO 识别运算符
                                    String str = recogcal(ch);
                                    if(!"ERROR".equals(str)) {
                                        int calculateToken = getCalculateToken(str);
                                        if(calculateToken!=0) {
                                            insertIntoTokenTable(str, calculateToken);
                                        }
                                    }
                                }
                                else if(sort == CHAR) {//字符常数
                                    int preCol = colNo;
                                    int token = recogstr(ch);
                                    int finCol = colNo;
                                    String id = nowLine.substring(preCol, finCol-1);
                                    if(!isInSymbolTable(id)) {
                                        insertIntoSymbolTable(id, token, row);
                                    }
                                    else {
                                        insertIntoTokenTable(id, token);
                                    }
                                }
                                else {//未知的字符
                                    if(ch != ' ') {
                                        insertIntoErrorTable(""+ch, row);
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    private int getCalculateToken(String str) {
        List<WordListBean> wordListBeans = entryMap.get("calculate");
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(str)) {
                return Integer.parseInt(wordListBean.getToken());//返回界符
            }
        }
        return 0;
    }

    private int getDelimeterToken(String str) {
        List<WordListBean> wordListBeans = entryMap.get("delimeter");
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(str)) {
                return Integer.parseInt(wordListBean.getToken());//返回界符
            }
        }
        return 0;
    }

    /**
     * 识别运算符
     * @param ch
     */
    private String recogcal(char ch) {
        String temp = "" + ch;
        char newCh = nowLine.charAt(colNo++);
        temp += newCh;

        List<WordListBean> wordListBeans = entryMap.get("calculate");
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(temp)) {
                return wordListBean.getWord();//返回界符
            }
        }
        colNo--;
        temp = "" + ch;
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(temp)) {
                return wordListBean.getWord();//返回界符
            }
        }
        return "ERROR";
    }

    /**
     * 识别界符
     * @param ch
     */
    private String recogdel(char ch) {
        List<WordListBean> wordListBeans = entryMap.get("delimeter");
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(""+ch)) {
                return wordListBean.getWord();//返回界符
            }
        }
        String temp = "" + ch;
        temp +=nowLine.charAt(colNo++);
        for(WordListBean wordListBean : wordListBeans) {
            if(wordListBean.getWord().equals(temp)) {
                return wordListBean.getWord();//返回界符
            }
        }
        colNo--;
        return "ERROR";
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
     * @param ch
     * 返回标识符
     */
    private String recog_id(char ch) {
        int state = 0;
        String id = "";

        while (state != 2) {
            switch (state) {
                case 0://0状态，读入一个字母，变成1状态
                    if (isalpha(ch)) {
                        id += ch;
                        state = 1;
                    }
                    else {
                        return "" + ERROR_CODE;
                    }
                    break;
                case 1:
                    if ((isalpha(ch) || isdigit(ch))) {
                        id += ch;
                        state = 1;
                    }
                    else {
                        state = 2;
                        id = id.substring(0, id.length());
                        colNo--;
                        continue;
                    }
            }
            ch = nowLine.charAt(colNo++);
        }
        return id;
    }

    /**
     * 识别数
     *
     * @param ch
     * @return
     */
    private int recogdig(char ch) {
        int state = 0;
        while (state != 7) {
            switch (state) {
                case 0://初始状态
                    if (isdigit(ch)) {
                        state = 1;
                    }
                    else {
                        return ERROR_CODE;
                    }
                    break;
                case 1://读取到的是数字
                    if (isdigit(ch)) {
                        state = 1;
                    }
                    else if (ch == '.') {
                        state = 2;
                    }
                    else if (ch == 'e' || ch=='E') {
                        state = 4;
                    }
                    else {
                        colNo--;
                        return INT_CODE;
                    }
                    break;
                case 2://读取到小数点
                    if (isdigit(ch)) {
                        state = 3;
                    }
                    else {
                        return ERROR_CODE;
                    }
                    break;
                case 3://读取到的是数字
                    if (isdigit(ch)) {
                        state = 3;
                    }
                    else if ((ch == 'E') || (ch == 'e')) {
                        state = 4;
                    }
                    else {
                        colNo--;
                        return NUMBER_CODE;//实数类型
                    }
                    break;
                case 4://读取到的是科学计数法:e、E
                    if (isdigit(ch)) {
                        state = 6;
                    }
                    else if (issign(ch)) {
                        state = 5;//读入+、-
                    }
                    else {
                        return ERROR_CODE;
                    }
                    break;
                case 5://读取到的是自加、自减
                    if(isdigit(ch)) {
                        state = 6;
                    }
                    else {
                        return ERROR_CODE;
                    }
                    break;
                case 6://读取到的是数字
                    if(isdigit(ch)) state = 6;
                    else {
                        colNo--;
                        state = 7;
                        return NUMBER_CODE;
                    }
            }
            ch = nowLine.charAt(colNo++);
        }
        return ERROR_CODE;
    }

    private boolean issign(char ch) {
        if (ch == '+' || ch == '-') {
            return true;
        }
        return false;
    }

    /**
     * 识别字符常数
     * @param ch
     * @return
     */
    private int recogstr(char ch) {
        int state = 0;
        while(state != 2) {
            switch (state) {
                case 0 :
                    state = 1;
                    break;
                case 1 :
                    if(ch == '\'' || ch == '\"') {
                        return colNo;
                    }
                    else {
                        state = 1;
                    }
            }
            ch = nowLine.charAt(colNo++);
        }
        return ERROR_CODE;
    }

    /**
     * 识别并去掉注释
     * @param ch
     * @return
     */
    private int handlecom(char ch) {
        int state = 0;
        char newch = nowLine.charAt(colNo++);
        if(newch=='*') {
            do {
                newch = nowLine.charAt(colNo++);
            } while ((newch != '*') && colNo < nowLine.length());
            if (newch != '\n') {
                newch = nowLine.charAt(colNo++);
                if (newch == '/') {
                    return COM_TOKEN;//返回注释的token
                }
            } else {
                return COM_TOKEN;
            }
        }
        else if (newch == '/') {
            colNo = nowLine.length();
            return COM_TOKEN;
        }
        return EXCEPT_TOKEN;//返回除号
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
     * 对第一个字符进行分类
     * @param ch
     * @return
     */
    public int sort(char ch) {
        if(isdigit(ch)) {//数字
            return CONSTANT;
        }
        else if(isalpha(ch)) {//字母
            return IDENTIFIER;
        }
        else if(ch=='/') {//注释
            return NOTE;
        }
        else if(ch=='\'' || ch == '\"') {//字符
            return CHAR;
        }
        else if(isdelimeter(""+ch)!=0) {//界符
            return DELIMITER;
        }
        else if(isCalculate("" + ch)!=0) {//运算符
            return CALCULATE;
        }
        else {//未识别
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

        return 0;
    }

    private int isCalculate(String temp) {
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
