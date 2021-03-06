package de.albsig.qischecker.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import de.albsig.qischecker.R;
import de.albsig.qischecker.data.Exam;

public class ExamActivity extends DialogHostActivity {

    public static final String ARG_EXAM = "exam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set content View
        this.setContentView(R.layout.activity_exam);

        //Set up Toolbar
        Toolbar bar = findViewById(R.id.toolbar);
        this.setSupportActionBar(bar);

        //Get arg
        Exam e = (Exam) getIntent().getExtras().get(ExamActivity.ARG_EXAM);

        //Set Title
        this.getSupportActionBar().setTitle(e.getName());

        //Set other information
        //If there is a grade
        if (e.getGrade() != null && e.getGrade().length() > 0) {
            this.setText(e.getGrade(), R.id.textGrade);
            this.setText(e.getState(), R.id.textState);

            //If there is no grade
        } else {
            this.setText(e.getState(), R.id.textGrade);
            this.setText("", R.id.textState);

        }

        //Set icon
        int d = 0;
        switch (e.getState()) {
            case "Prüfung vorhanden":
                d = R.drawable.ic_an;
                break;
            case "bestanden":
                d = R.drawable.ic_be_neu;
                break;
            case "nicht bestanden":
                d = R.drawable.ic_nb;
                break;
            case "endgültig nicht bestanden":
                d = R.drawable.ic_en;
                break;
            default:

        }

        TextView tv = (TextView) findViewById(R.id.textState);
        tv.setCompoundDrawablesWithIntrinsicBounds(d, 0, 0, 0);
    }

    private void setText(String text, int id) {
        TextView tv = (TextView) this.findViewById(id);
        tv.setText(text);

    }

}
