package com.org.entity;

import java.util.List;

/**
 * 关键字、运算符、常数、界符、标识符实体
 */
public class Entry {
    /**
     * type : keyword
     * wordList : [{"word":"abstract","token":"1"},{"word":"assert","token":"2"},{"word":"boolean","token":"3"},{"word":"break","token":"4"},{"word":"byte","token":"5"},{"word":"case","token":"6"},{"word":"catch","token":"7"},{"word":"char","token":"8"},{"word":"class","token":"9"},{"word":"const","token":"10"},{"word":"continue","token":"11"},{"word":"default","token":"12"},{"word":"do","token":"13"},{"word":"double","token":"14"},{"word":"else","token":"15"},{"word":"enum","token":"16"},{"word":"extends","token":"17"},{"word":"final","token":"18"},{"word":"finally","token":"19"},{"word":"float","token":"20"},{"word":"for","token":"21"},{"word":"goto","token":"22"},{"word":"if","token":"23"},{"word":"implements","token":"24"},{"word":"import","token":"25"},{"word":"instanceof","token":"26"},{"word":"interface","token":"27"},{"word":"int","token":"28"},{"word":"long","token":"29"},{"word":"native","token":"30"},{"word":"new","token":"31"},{"word":"package","token":"32"},{"word":"private","token":"33"},{"word":"protected","token":"34"},{"word":"public","token":"35"},{"word":"return","token":"36"},{"word":"strictfp","token":"37"},{"word":"short","token":"38"},{"word":"static","token":"39"},{"word":"super","token":"40"},{"word":"switch","token":"41"},{"word":"synchronized","token":"42"},{"word":"this","token":"43"},{"word":"throw","token":"44"},{"word":"throws","token":"45"},{"word":"transient","token":"46"},{"word":"void","token":"47"},{"word":"try","token":"48"},{"word":"volatile","token":"49"},{"word":"while","token":"50"}]
     */
    private String type;
    private List<WordListBean> wordList;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<WordListBean> getWordList() {
        return wordList;
    }

    public void setWordList(List<WordListBean> wordList) {
        this.wordList = wordList;
    }
}
