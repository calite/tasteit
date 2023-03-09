package com.example.tasteit_java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FragmentInfo extends Fragment {

    private static EditText etRecipeName;
    private static EditText etDescriptionRecipe;
    private static Spinner spCountries;
    private static SeekBar skDificulty;
    private ChipGroup cgTags;
    private static ArrayList<String> tags;
    private Button bAddTag;
    private static EditText etTagName;

    public FragmentInfo() {
        // Required empty public constructor
    }

    public static FragmentInfo newInstance() {
        FragmentInfo fragment = new FragmentInfo();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info, container, false);

        //textbox
        etRecipeName = view.findViewById(R.id.etRecipeName);
        etDescriptionRecipe = view.findViewById(R.id.etDescripcionRecipe);
        //seekbar
        skDificulty = view.findViewById(R.id.skDificulty);
        //spinner
        spCountries = view.findViewById(R.id.spCountry);
        ArrayAdapter<CharSequence> adapterSpinnerCountries = ArrayAdapter.createFromResource(getContext(),
                R.array.countries_array, android.R.layout.simple_spinner_item);
        adapterSpinnerCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountries.setAdapter(adapterSpinnerCountries);
        //tags
        tags = new ArrayList<>();
        cgTags = view.findViewById(R.id.cgTags);
        bAddTag = view.findViewById(R.id.bAddTag);
        etTagName = view.findViewById(R.id.etTagName);
        bAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chip chip = new Chip(getContext());
                chip.setText(etTagName.getText().toString());
                chip.setCloseIconResource(R.drawable.ic_close);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tags.remove(etTagName.getText().toString());
                        cgTags.removeView(chip);
                    }
                });
                cgTags.addView(chip);
                tags.add(etTagName.getText().toString());
            }
        });

        return view;
    }

    public static String getRecipeName(){
        return etRecipeName.getText().toString();
    }

    public static String getDescriptionRecipe() {
        return etDescriptionRecipe.getText().toString();
    }

    public static String getCountry() {
        return spCountries.getSelectedItem().toString();
    }

    public static int getDificulty() {
      return skDificulty.getProgress();
    }

    public static ArrayList getTags() {
        return tags;
    }
}