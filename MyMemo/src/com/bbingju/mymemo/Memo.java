package com.bbingju.mymemo;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.UUID;

/**
 * Created by bbingju on 15. 7. 14.
 */
@ParseClassName("Memo")
public class Memo extends ParseObject {

    public static ParseQuery<Memo> getQuery() {
        return ParseQuery.getQuery(Memo.class);
    }

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public boolean isDraft() {
        return getBoolean("isDraft");
    }

    public void setDraft(boolean isDraft) {
        put("isDraft", isDraft);
    }

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAuthor(ParseUser author) {
        put("author", author);
    }

    public String getContent() {
        return getString("content");
    }

    public void setContent(String content) {
        put("content", content);
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }
}
