package de.hfu.studiportal.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hfu.funfpunktnull.R;
import de.hfu.studiportal.data.Exam;
import de.hfu.studiportal.data.Seperator;

public class ExamCategoryAdapter extends RecyclerView.Adapter<ExamCategoryAdapter.ViewHolder> {

	private final String BONUS;
	private final String MALUS;
	private final String ECTS;
	private final String NO_ECTS;
	private final String STATE_RESIGNATED;
	private final String STATE;
	private final String SEMESTER;
	private final String ATTEMPT;
	private final String PRACTICAL_WORK;
	private final String NOTE;
	private final String GRADE;
    private final Drawable IC_AN;
    private final Drawable IC_EN;
    private final Drawable IC_NB;
    private final Drawable IC_BE;
    private final Drawable IC_RE;
    private final Typeface TYPEFACE_ROBOTO_LIGHT;

    private final List<Exam> objects;

    private int animationStep = 0;

	public ExamCategoryAdapter(Context context, List<Exam> objects) {
        this.objects = objects;

        this.animationStep = this.objects.size();

		this.BONUS = context.getString(R.string.text_bonus);
		this.MALUS = context.getString(R.string.text_malus);
		this.ECTS = context.getString(R.string.text_ects);
		this.NO_ECTS = context.getString(R.string.text_no_ects);
		this.STATE_RESIGNATED = context.getString(R.string.text_state_resignated);
		this.STATE = context.getString(R.string.text_state);
		this.SEMESTER = context.getString(R.string.text_semester);
		this.ATTEMPT = context.getString(R.string.text_attempt);
		this.PRACTICAL_WORK = context.getString(R.string.text_practical_work);
		this.NOTE = context.getString(R.string.text_note);
		this.GRADE = context.getString(R.string.text_grade);
        this.IC_AN = context.getResources().getDrawable(R.drawable.ic_an);
        this.IC_BE = context.getResources().getDrawable(R.drawable.ic_be);
        this.IC_EN = context.getResources().getDrawable(R.drawable.ic_en);
        this.IC_NB = context.getResources().getDrawable(R.drawable.ic_nb);
        this.IC_RE = context.getResources().getDrawable(R.drawable.ic_re);
        this.TYPEFACE_ROBOTO_LIGHT = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

    }

    @Override
    public int getItemCount() {
        return animationStep;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate layout and create view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_exam, parent, false);
        ViewHolder h = new ViewHolder(v);
        h.textViews.get(0).setTypeface(TYPEFACE_ROBOTO_LIGHT);
        return h;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
		//Get Exam
		Exam e = this.objects.get(position);

        //Save context
        Context ctx = holder.itemView.getContext();

		//If e is a seperator hide all views, if not show them all
		if(e instanceof Seperator) {
			holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 60));

			return;

		} else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            holder.itemView.setVisibility(View.VISIBLE);
			for(TextView v : holder.textViews)
				v.setVisibility(View.VISIBLE);

		}

		//Get Kind
		Exam.Kind kind = e.getKindEnum();

		//Set title and exam no
        holder.textViews.get(0).setText(e.getName());

		//Fill Views with data
		switch(kind) {
		case KO: 
			if(e.getBonus().equals("-")) {
				//If there are no bnous ects -> hide the useless view, else -> show them
                holder.textViews.get(1).setVisibility(View.GONE);

			} else {
                holder.textViews.get(1).setText(String.format("%s: %s %s", this.BONUS, e.getBonus(), this.ECTS));

			}

			if(e.getMalus().equals("-")) {
				//If there are no malus ects -> hide the useless view, else -> show them
                holder.textViews.get(2).setVisibility(View.GONE);

			} else {
                holder.textViews.get(2).setText(String.format("%s: %s %s", this.MALUS, e.getMalus(), this.ECTS));

			}

			if(e.getMalus().equals("-") && e.getBonus().equals("-")) {
				//If both are not set, the View will be empty (both textview are hidden)! Show the first and say no ects
                holder.textViews.get(1).setVisibility(View.VISIBLE);
                holder.textViews.get(1).setText(this.NO_ECTS);
                holder.textViews.get(2).setVisibility(View.GONE);

			}

			break;

		case PL: 
		case P: 
		case G: 
			if(e.isResignated()) {
				//If e is resignated, shw special info on the topic
                holder.textViews.get(1).setText(String.format("%s: %s", this.STATE, this.STATE_RESIGNATED));
                holder.textViews.get(2).setText(String.format("%s: %s", this.NOTE, e.getNoteName(ctx)));

			} else {
				//First field
				if(e.getStateEnum() == Exam.State.AN) {
					//If e is only AN, there is no grade to show. Display state: an
                    holder.textViews.get(1).setText(String.format("%s: %s (%s %s)", this.STATE, e.getStateName(ctx), e.getECTS(), this.ECTS));

				} else {
					//e is not an -> be, nb or en. Show grade and ects
                    holder.textViews.get(1).setText(String.format("%s: %s (%s %s)", this.GRADE, e.getGrade(), e.getECTS(), this.ECTS));

				}

				//Second Field
				if(e.getKindEnum() == Exam.Kind.G) {
					//If e is generatde, show only the semester
                    holder.textViews.get(2).setText(String.format("%s: %s", this.SEMESTER, e.getSemester()));

				} else {
					//Else show attempt and semester
                    holder.textViews.get(2).setText(String.format("%s: %s (%s)", this.ATTEMPT, e.getTryCount(), e.getSemester()));

				}
			}

			break;

		case VL: 
			//Show state and sign that e is a vl
            holder.textViews.get(1).setText(String.format("%s: %s (%s %s)", this.STATE, e.getStateName(ctx), e.getECTS(), this.ECTS));
            holder.textViews.get(2).setText(this.PRACTICAL_WORK);

			break;

		case UNDEFINED:
		default:
			//This should not happen. show state an Kind
            holder.textViews.get(1).setText(String.format("%s: %s", this.STATE, e.getStateName(ctx)));
            holder.textViews.get(2).setText(e.getKind());

			break;

		}

		//Set icon
		switch (e.getStateEnum()) {
		case AN: holder.imageView.setImageDrawable(this.IC_AN); break;
		case BE: holder.imageView.setImageDrawable(this.IC_BE); break;
		case NB: holder.imageView.setImageDrawable(this.IC_NB); break;
		case EN: holder.imageView.setImageDrawable(this.IC_EN); break;
		case UNDEFINED: holder.imageView.setVisibility(View.GONE); break;
		}

		//If eis resignated, override icon with flag
		if(e.isResignated()) {
            holder.imageView.setImageDrawable(this.IC_RE);

		}
	}

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public List<TextView> textViews = new ArrayList<>(3);
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textViews.add((TextView) itemView.findViewById(R.id.textViewTitle));
            textViews.add((TextView) itemView.findViewById(R.id.textViewSubtitle1));
            textViews.add((TextView) itemView.findViewById(R.id.textViewSubtitle2));
            imageView = (ImageView) itemView.findViewById(R.id.imageViewState);

        }
    }

    public void animateIn(final Activity activity, final RecyclerView recyclerView) {
        //Reset
        this.animationStep = 0;
        this.notifyDataSetChanged();

        //Create handler
        HandlerThread sWorkerThread = new HandlerThread("WeatherWidgetProvider-worker");
        sWorkerThread.start();
        Handler h = new Handler(sWorkerThread.getLooper());

        //Run animation
        h.post(new Runnable() {
            @Override
            public void run() {
                //Fetch the size of available items
                final int max = ExamCategoryAdapter.this.objects.size();

                //Make at most 20 animation steps (more items wont be visible), but not more then items available
                for(int i=0; i<20 && i<=max; i++) {
                    final int I = i;

                    //Run UI Thread
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Animate in
                            ExamCategoryAdapter.this.animationStep = I+1 < max ? I+1 : I;
                            ExamCategoryAdapter.this.notifyItemInserted(I);
                        }
                    });

                    //Sleep 40ms before inserting the next item
                    try {
                        Thread.sleep(40);

                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }


                //Sleep the time the entry animation takes
                try {
                    Thread.sleep(recyclerView.getItemAnimator().getAddDuration());

                } catch (InterruptedException e) {
                    e.printStackTrace();

                }

                //Make the rest of the items available, this will be off-screen
                activity.runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       ExamCategoryAdapter.this.animationStep = ExamCategoryAdapter.this.objects.size();
                       ExamCategoryAdapter.this.notifyDataSetChanged();

                   }
               });
            }
        });
    }
}
