package com.robmapps.keepingscore;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_FrontPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_FrontPage extends Fragment {
    View view;
    public EditText etFrontPage;

    public Frag_FrontPage() {
        // Required empty public constructor
    }

    public static Frag_FrontPage newInstance(String param1, String param2) {
        Frag_FrontPage fragment = new Frag_FrontPage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (etFrontPage != null) {
            outState.putString("EditTextValue", etFrontPage.getText().toString());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            etFrontPage.setText(savedInstanceState.getString("EditTextValue", ""));
        }
        return ;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_team_list,container,false);
        etFrontPage = view.findViewById(R.id.etFrontPage);
        //Button btnFrontPage = view.findViewById(R.id.btnFrontPage);
        /*btnFrontPage.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Bundle result=new Bundle();
                result.putString("FrontData",etFrontPage.getText().toString());
                getParentFragmentManager().setFragmentResult("DataFromFront",result);
                etFrontPage.setText("Sent");
            }
        });*/
        getParentFragmentManager().setFragmentResultListener("TeamData", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String data= (String) result.get("DataFromTeam");
                TextView tvFront = view.findViewById(R.id.tvFront);
                tvFront.setText(data);
            }
        });

        return inflater.inflate(R.layout.fragment_front_page, container, false);
    }
}