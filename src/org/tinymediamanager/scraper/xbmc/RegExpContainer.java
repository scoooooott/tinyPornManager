package org.tinymediamanager.scraper.xbmc;

public interface RegExpContainer {
    public void addRegExp(RegExp regexp);
    public RegExp[] getRegExps();
    public boolean hasRegExps();
}

