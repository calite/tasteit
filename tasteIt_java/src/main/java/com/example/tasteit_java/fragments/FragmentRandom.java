package com.example.tasteit_java.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.tasteit_java.ActivityRandom;
import com.example.tasteit_java.R;
import com.example.tasteit_java.clases.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.logging.Level;

public class FragmentRandom extends Fragment {
    GestureDetector gestureDetector;

    private Recipe recipe;

    public FragmentRandom() {
        // Required empty public constructor
    }

    public static FragmentRandom newInstance(Recipe recipe) {
        FragmentRandom fragment = new FragmentRandom();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random, container, false);
        if(getArguments() != null){


            TextView tvRecipeName = view.findViewById(R.id.tvRecipeName);
            TextView tvNameCreator = view.findViewById(R.id.tvNameCreator);
            TextView tvDescription = view.findViewById(R.id.tvDescription);
            TextView tvDifficulty = view.findViewById(R.id.tvDifficulty);
            TextView tvCountry = view.findViewById(R.id.tvCountry);
            ChipGroup cgIngredients = view.findViewById(R.id.cgIngredients);

            tvRecipeName.setText(recipe.getName());
            tvNameCreator.setText(recipe.getCreator());
            tvDescription.setText(recipe.getDescription());
            tvDifficulty.setText(recipe.getDescription());
            tvCountry.setText(recipe.getCountry());
            for(String s : recipe.getIngredients()) {
                Chip chip = new Chip(getContext());
                chip.setText(s);
                chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.maroon)));
                chip.setTextColor(Color.WHITE);
                cgIngredients.addView(chip);
            }


        }
        // Inflate the layout for this fragment

        return view;
    }
}