package com.jonlatane.quizzy.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jonlatane.quizzy.BR;
import com.jonlatane.quizzy.io.EnrolledQuiz;
import com.jonlatane.quizzy.viewmodel.QuizCardViewModel;

import java.io.IOException;
import java.util.List;

/**
 * Created by jonlatane on 11/24/15.
 */
public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.ViewHolder> {

    private List<EnrolledQuiz> items;
    private int itemLayout;

    public QuizListAdapter(List<EnrolledQuiz> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    public void addQuiz(Context context) throws IOException {
        EnrolledQuiz newQuiz = EnrolledQuiz.addQuiz("", context);
        items.add(newQuiz);
        notifyItemInserted(items.size() - 1);
    }

    public void deleteQuiz(String quizId, Context context) {
        for(int i = 0; i < items.size(); i++) {
            if(items.get(i).getQuizId().equals(quizId)) {
                items.get(i).delete(context);
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public void updateAndNotify(Context context) {
        this.items = EnrolledQuiz.getEnrolledQuizzes(context);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, itemLayout, parent, false);
        return new ViewHolder(binding);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        EnrolledQuiz quiz = items.get(position);
        QuizCardViewModel viewModel = new QuizCardViewModel(quiz, holder.itemView.getContext());
        holder.binding.setVariable(BR.quizCard, viewModel);
        holder.itemView.setTag(viewModel);
    }

    @Override public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding binding;
        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
