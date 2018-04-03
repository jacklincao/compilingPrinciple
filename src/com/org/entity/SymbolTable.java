package com.org.entity;

/**
 * 符号表
 */
public class SymbolTable {
    private String name;
    private Integer length;
    private Integer token;
    private String type;
    private String kind;
    private String val;
    private String addr;

    public SymbolTable() {

    }

    public SymbolTable(String name, Integer length, Integer token, String type, String kind, String val, String addr) {
        this.name = name;
        this.length = length;
        this.token = token;
        this.type = type;
        this.kind = kind;
        this.val = val;
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
