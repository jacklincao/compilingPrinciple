package com.org.entity;

public class TokenTable {
    private String word;
    private Integer token;

    public TokenTable() {

    }

    public TokenTable(String word, Integer token) {
        this.word = word;
        this.token = token;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }
}
