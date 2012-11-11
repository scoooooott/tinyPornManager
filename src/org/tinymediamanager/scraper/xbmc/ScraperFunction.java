package org.tinymediamanager.scraper.xbmc;

import java.util.ArrayList;
import java.util.List;

public class ScraperFunction implements RegExpContainer {
    private boolean clearBuffers = true;
    private int dest = 0;
    private boolean appendBuffer = false;
    private String name;
    private List<RegExp> regexps = new ArrayList<RegExp>();
    
    public ScraperFunction() {
    }
    public boolean isClearBuffers() {
        return clearBuffers;
    }
    public void setClearBuffers(boolean clearBuffers) {
        this.clearBuffers = clearBuffers;
    }
    public int getDest() {
        return dest;
    }
    public void setDest(int dest) {
        this.dest = dest;
    }
    
    public void setName(String nodeName) {
        this.name = nodeName;
    }
    
    public String getName() {
        return this.name;
    }
    public void addRegExp(RegExp regexp) {
        regexps.add(regexp);
    }
    public RegExp[] getRegExps() {
        return regexps.toArray(new RegExp[regexps.size()]);
    }
    public boolean hasRegExps() {
        return regexps !=null && regexps.size()>0;
    }
    public boolean isAppendBuffer() {
        return appendBuffer;
    }
    public void setAppendBuffer(boolean appendBuffer) {
        this.appendBuffer = appendBuffer;
    }    
}
