package com.example.tasteit_java.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.tasteit_java.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FragmentIngredientsNewRecipe extends Fragment {

    private static EditText etIngredientName;
    private static ChipGroup cgIngredients;
    private static ArrayList<String> ingredients = new ArrayList<>();
    private Button bAddIngredient;

    private static boolean loadRecipe = false;

    public FragmentIngredientsNewRecipe() {
        // Required empty public constructor
    }

    public static FragmentIngredientsNewRecipe newInstance() {
        FragmentIngredientsNewRecipe fragment = new FragmentIngredientsNewRecipe();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ingredients, container, false);

        //ingredientes
        cgIngredients = view.findViewById(R.id.cgIngredients);
        bAddIngredient = view.findViewById(R.id.bAddIngredient);
        etIngredientName = view.findViewById(R.id.etIngredientName);
        bAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etIngredientName.getText().length() > 0) {
                    Chip chip = new Chip(getContext());
                    chip.setText(etIngredientName.getText().toString());
                    chip.setCloseIconResource(R.drawable.ic_close);
                    chip.setCloseIconVisible(true);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.maroon)));
                    chip.setTextColor(Color.WHITE);

                    chip.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ingredients.remove(etIngredientName.getText().toString());
                            cgIngredients.removeView(chip);
                        }
                    });
                    cgIngredients.addView(chip);
                    ingredients.add(etIngredientName.getText().toString());
                    etIngredientName.setText("");
                } else {
                    Toast.makeText(getContext(), R.string.error_no_ingredient, Toast.LENGTH_SHORT).show();
                }
            }
        });


        if(loadRecipe){
            for(String s:ingredients){
                Chip chip = new Chip(getContext());
                chip.setText(s);
                chip.setCloseIconResource(R.drawable.ic_close);
                chip.setCloseIconVisible(true);
                chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.maroon)));
                chip.setTextColor(Color.WHITE);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ingredients.remove(s);
                        cgIngredients.removeView(chip);
                    }
                });
                cgIngredients.addView(chip);
            }


        }

        return view;
    }


    public static ArrayList<String> getIngredients() {
        return ingredients;
    }
    public static EditText getEtIngredientName() { return etIngredientName; }

    //TODO
    public static void setIngredients(ArrayList<String> ingridientsList){
        loadRecipe = true;
        ingredients = ingridientsList;
    }
}