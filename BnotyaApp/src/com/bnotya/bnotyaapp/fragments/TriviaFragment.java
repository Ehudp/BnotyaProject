package com.bnotya.bnotyaapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.bnotya.bnotyaapp.R;
import com.bnotya.bnotyaapp.helpers.DatabaseHelper;
import com.bnotya.bnotyaapp.models.Answer;
import com.bnotya.bnotyaapp.models.Question;

import java.util.List;

public class TriviaFragment extends Fragment implements OnCheckedChangeListener
{
	public static final String ARG_VIEW_NUMBER = "view_number";
	// Database Helper
	DatabaseHelper _db;
	
	TextView _questionView;
	RadioGroup _answers;
	
	public TriviaFragment()
	{
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.trivia_view,
				container, false);
		
		getActivity().setTitle(R.string.trivia_page_title);
		
		_questionView = (TextView) rootView.findViewById(R.id.tvQuestion);
		_answers = (RadioGroup) rootView.findViewById(R.id.rgAnswers);

		_db = DatabaseHelper.getInstance(getActivity());
		
		_db.clearDb();

		_db.createDb();

		initQuestion();
		
		return rootView;
	}
	
	private void initQuestion()
	{
		// get all questions
		List<Question> allQuestions = _db.getAllQuestions();
		// get question
		Question question = allQuestions.get(0);
		// set question		
		_questionView.setText(question.getContent());
		_questionView.setTag(question.getId());
		
		// Setup the answers
		List<Answer> allAnswers = _db.getAllAnswersByQuestion(question
				.getId());
		
		for (Answer answer : allAnswers)
		{
			RadioButton radioButton = new RadioButton(getActivity());
			radioButton.setText(answer.getContent());
			radioButton.setId((int)answer.getId());
			_answers.addView(radioButton, new RadioGroup.LayoutParams(
					RadioGroup.LayoutParams.WRAP_CONTENT,
					RadioGroup.LayoutParams.WRAP_CONTENT));
		}
		
		_answers.setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{	
		boolean result = _db.getIsCorrectAnswer(
				Long.valueOf(_questionView.getTag().toString()), (long)checkedId);
		if(result)
		{
			Toast.makeText(getActivity().getApplicationContext(),
					R.string.correct_answer, Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(getActivity().getApplicationContext(), 
					R.string.wrong_answer, Toast.LENGTH_SHORT).show();
		}
	}
}
