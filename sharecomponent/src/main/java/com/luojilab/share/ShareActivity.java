package com.luojilab.share;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.luojilab.componentservice.share.bean.Author;

/**
 * Created by mrzhang on 2017/6/20.
 */
public class ShareActivity extends AppCompatActivity {

    String bookName;

    Author author;

    private TextView tvShareTitle;
    private TextView tvShareBook;
    private TextView tvAuthor;
    private TextView tvCounty;

    private final static int RESULT_CODE = 8888;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity_share);

        tvShareTitle =  (TextView)findViewById(R.id.share_title);
        tvShareBook =   (TextView)findViewById(R.id.share_tv_tag);
        tvAuthor =   (TextView)findViewById(R.id.share_tv_author);
        tvCounty =  (TextView) findViewById(R.id.share_tv_county);

        tvShareTitle.setText("Book");

        if (bookName != null) {
            tvShareBook.setText(bookName);
        }

        if (author != null) {
            tvAuthor.setText(author.getName());
            tvCounty.setText(author.getCounty());
        }

        Intent intent = new Intent();
        intent.putExtra("result", "Share Success");
        setResult(RESULT_CODE, intent);

    }
}
