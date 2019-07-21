package de.albsig.qischecker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.albsig.qischecker.R;
import de.albsig.qischecker.data.ExamCategory;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

//Instances of this class are fragments representing a single
//object in our collection.
public class ExamCategoryFragment extends Fragment {
    public static final String ARG_CATEGORY = "category";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(
                R.layout.fragment_exam_category, container, false);

        //Save Category
        ExamCategory c = (ExamCategory) getArguments().get(ARG_CATEGORY);

        //Find list and init
        RecyclerView list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        list.setItemAnimator(new SlideInUpAnimator());

        //Set Adapter
        ExamCategoryAdapter adapter = new ExamCategoryAdapter(this.getActivity(), c.getAllExams());
        list.setAdapter(adapter);

        //Animate Entry
        adapter.animateIn(this.getActivity(), list);

        return rootView;
    }
}