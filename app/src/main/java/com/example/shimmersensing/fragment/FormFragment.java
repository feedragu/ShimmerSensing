package com.example.shimmersensing.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.shimmersensing.R;
import com.example.shimmersensing.adapter.FormRecyclerAdapter;
import com.example.shimmersensing.utilities.QuestionTrial;
import com.example.shimmersensing.utilities.row;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FormFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FormFragment extends Fragment implements FormRecyclerAdapter.OnFormValueClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private int[] voti;


    private OnFragmentInteractionListener mListener;
    private RecyclerView formList;
    private ArrayList<QuestionTrial> formTrial;
    private FormRecyclerAdapter rAdapter;
    private Button sendForm;

    public FormFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param formTrial
     * @return A new instance of fragment QuestionaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FormFragment newInstance(ArrayList<QuestionTrial> formTrial) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, (Serializable) formTrial);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            formTrial = (ArrayList<QuestionTrial>) getArguments().getSerializable(ARG_PARAM1);
            if (formTrial != null) {
                voti = new int[formTrial.size()];
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        formList = getView().findViewById(R.id.formList);

        sendForm = getView().findViewById(R.id.buttonSendForm);

        sendForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentInteractionForm(voti);
                }
            }
        });
        RecyclerView.LayoutManager recyce = new
                LinearLayoutManager(getView().getContext(), LinearLayoutManager.VERTICAL, false);

        formList.setLayoutManager(recyce);
        formList.setNestedScrollingEnabled(false);

        rAdapter = new FormRecyclerAdapter(getView().getContext(), formTrial, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(formList.getContext(), DividerItemDecoration.VERTICAL);
        formList.addItemDecoration(itemDecor);
        formList.setAdapter(rAdapter);
    }

//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteractionForm();
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void OnFormValueClickListener(int position, int value) {
        voti[position]=value+1;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionForm(int[] voti);
    }
}
