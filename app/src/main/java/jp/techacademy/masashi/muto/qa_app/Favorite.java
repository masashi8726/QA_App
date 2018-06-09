package jp.techacademy.masashi.muto.qa_app;

import java.io.Serializable;

public class Favorite implements Serializable{
    private String mBody;
    private String mName;
    private String mUid;
    private String mFavoriteUid;

    public Favorite(String body, String name, String uid, String favoriteUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mFavoriteUid = favoriteUid;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getAnswerUid() {
        return mFavoriteUid;
    }
}
