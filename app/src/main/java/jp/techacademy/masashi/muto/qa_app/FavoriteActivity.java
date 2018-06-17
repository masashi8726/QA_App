package jp.techacademy.masashi.muto.qa_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoriteActivity extends AppCompatActivity  {

    private int mGenre = 0;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavoriteRef;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList = new ArrayList<>();
    private ArrayList<String> mFavoriteQuestionUidList= new ArrayList<>();
    private QuestionsListAdapter mAdapter;

    private ChildEventListener mIsFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String questionUid = dataSnapshot.getKey();

            mFavoriteQuestionUidList.add(questionUid);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String, HashMap<String,String>> map = (HashMap) dataSnapshot.getValue();
            for (Object questions :map.values()) {
                Map qData = (Map) questions;
                String title = (String) qData.get("title");
                String body = (String) qData.get("body");
                String name = (String) qData.get("name");
                String uid = (String) qData.get("uid");
                String imageString = (String) qData.get("image");
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }

                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                if (mFavoriteQuestionUidList.contains(dataSnapshot.getKey())) {

                    mQuestionArrayList.add(question);
                }
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);

                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mAdapter.setmQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        if (mFavoriteRef != null) {
            mFavoriteRef.removeEventListener(mIsFavoriteListener);
        }
        mFavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
        mFavoriteRef.addChildEventListener(mIsFavoriteListener);

        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }

        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
        mGenreRef.addChildEventListener(mEventListener);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }
}
