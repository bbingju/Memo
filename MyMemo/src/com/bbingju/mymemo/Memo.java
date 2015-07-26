/*
 * "THE BEER-WARE LICENSE"
 *
 * <pjhwang@gmail.com> wrote this file.  As long as you retain this
 * notice you can do whatever you want with this stuff. If we meet
 * some day, and you think this stuff is worth it, you can buy me a
 * beer in return.
 *
 * - Byung Ju Hwang.
 */

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

    public String getMemo() {
        return getString("memo");
    }

    public void setMemo(String memo) {
        put("memo", memo);
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }
}
