package com.example.tasteit_java;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasteit_java.ApiService.ApiClient;
import com.example.tasteit_java.ApiService.ApiRequests;
import com.example.tasteit_java.ApiService.RecipeId_Recipe_User;
import com.example.tasteit_java.adapters.AdapterEndlessRecyclerMain;
import com.example.tasteit_java.bdConnection.BdConnection;
import com.example.tasteit_java.clases.OnItemNavSelectedListener;
import com.example.tasteit_java.clases.OnLoadMoreListener;
import com.example.tasteit_java.clases.Recipe;
import com.example.tasteit_java.clases.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ActivityMain extends AppCompatActivity {

    private AdapterEndlessRecyclerMain adapterEndlessRecyclerMain;
    private int skipper;
    private FloatingActionButton bCreate;
    private ShimmerFrameLayout shimmer;

    private RecyclerView rvRecipes;
    private String token;

    private String accessToken;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accessToken = Utils.getUserAcessToken();

        rvRecipes = findViewById(R.id.rvRecipes);
        bCreate = findViewById(R.id.bCreate);
        skipper = 0;

        rvRecipes.setHasFixedSize(true);
        bringRecipes();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvRecipes.setLayoutManager(linearLayoutManager);

        adapterEndlessRecyclerMain = new AdapterEndlessRecyclerMain(rvRecipes);
        rvRecipes.setAdapter(adapterEndlessRecyclerMain);

        adapterEndlessRecyclerMain.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if(adapterEndlessRecyclerMain.getItemCount() > 28) { //habra que ponerle un limite (que en principio puede ser el total de recipes en la bbdd o algo fijo para no sobrecargar el terminal)
                    Toast.makeText(ActivityMain.this, "Finiquitao con " + adapterEndlessRecyclerMain.getItemCount() + " recetas", Toast.LENGTH_SHORT).show();
                } else {
                    adapterEndlessRecyclerMain.dataList.add(null);
                    adapterEndlessRecyclerMain.notifyItemInserted(adapterEndlessRecyclerMain.getItemCount() - 1);

                    skipper += 10;
                    bringRecipes();
                }
            }

            @Override
            public void update() {
                adapterEndlessRecyclerMain.dataList.add(0, null);
                adapterEndlessRecyclerMain.notifyItemInserted(0);

                skipper = 0;
                adapterEndlessRecyclerMain.dataList.clear();
                bringRecipes();
            }
        });

        //menu superior
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //boton crear receta
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ActivityMain.this, ActivityNewRecipe.class);
                startActivity(i);
            }
        });

        //recoger token usuario firebase
        token = Utils.getUserToken();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Utils.refreshToken();
                Logger.getGlobal().log(Level.INFO,"Attempting to refresh token");
            }
        },1,1800000);

    }

    //MENU superior
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        shimmer = findViewById(R.id.shimmer);
        shimmer.startShimmer();

        getMenuInflater().inflate(R.menu.main_menu, menu);

        //Bitmap bitmap = Utils.uriToBitmap(this, "https://firebasestorage.googleapis.com/v0/b/tasteit-java.appspot.com/o/images%2F035d70df-1048-4c15-ba6a-c4d81d44a026?alt=media&token=d2c0ebf1-3b4e-40a4-9162-94fbc2070008");
        //BitmapDrawable roundedBitmapDrawable = new BitmapDrawable(getResources(), Utils.getRoundBitmapWithImage(bitmap));
        //menu.getItem(0).setIcon(roundedBitmapDrawable);

        BottomNavigationView fcMainMenu = findViewById(R.id.fcMainMenu);
        fcMainMenu.setSelectedItemId(R.id.bHome);
        fcMainMenu.setOnItemSelectedListener(new OnItemNavSelectedListener(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //ojo que puede petar
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
            case R.id.iProfile:
                //NEO4J
                Intent i = new Intent(ActivityMain.this, ActivityProfile.class);
                startActivity(i);
                //FIN NEO4J

                return true;
            case R.id.iCloseSesion:
                signOut();
                return true;

            case R.id.iDarkMode:
                if(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);}
        }
        return super.onOptionsItemSelected(item);
    }

    //LOGOUT
    public void callSignOut(View view){
        signOut();
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity (new Intent(this, ActivityLogin.class));
    }

    //carga de recetas asyncrona
    private class RecipesLoader {

        private final ApiRequests apiRequests;
        private final MutableLiveData<List<Recipe>> recipeLiveData;

        public RecipesLoader(ApiRequests apiRequests) {
            this.apiRequests = apiRequests;
            recipeLiveData = new MutableLiveData<>();
        }

        public LiveData<List<Recipe>> getRecipes() {
            return recipeLiveData;
        }

        public void loadRecipes() {
            apiRequests.getRecipes(skipper).enqueue(new Callback<List<RecipeId_Recipe_User>>() {
                @Override
                public void onResponse(Call<List<RecipeId_Recipe_User>> call, Response<List<RecipeId_Recipe_User>> response) {
                    if (response.isSuccessful()) {
                        List<RecipeId_Recipe_User> recipeApis = response.body();
                        List<Recipe> recipes = new ArrayList<>();

                        //tratamos los datos
                        for (RecipeId_Recipe_User recipeApi : recipeApis) {
                            Recipe recipe = new Recipe(
                                    recipeApi.getRecipeDetails().getName(),
                                    recipeApi.getRecipeDetails().getDescription(),
                                    (ArrayList<String>) recipeApi.getRecipeDetails().getSteps(),
                                    recipeApi.getRecipeDetails().getDateCreated(),
                                    recipeApi.getRecipeDetails().getDifficulty(),
                                    recipeApi.getUser().getUsername(),
                                    recipeApi.getRecipeDetails().getImage(),
                                    recipeApi.getRecipeDetails().getCountry(),
                                    (ArrayList<String>) recipeApi.getRecipeDetails().getTags(),
                                    (ArrayList<String>) recipeApi.getRecipeDetails().getIngredients(),
                                    recipeApi.getRecipeId(),
                                    recipeApi.getUser().getToken()
                            );
                            recipes.add(recipe);
                        }
                        recipeLiveData.postValue(recipes);
                    } else {
                        // La solicitud no fue exitosa
                    }
                }

                @Override
                public void onFailure(Call<List<RecipeId_Recipe_User>> call, Throwable t) {
                    // Hubo un error en la solicitud
                    Toast.makeText(ActivityMain.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onRecipesLoaded(List<Recipe> recipes) {
        // Actualizar la UI con la lista de recetas

        if(adapterEndlessRecyclerMain.getItemCount() > 0) {
            adapterEndlessRecyclerMain.dataList.remove(adapterEndlessRecyclerMain.getItemCount() - 1);
        }

        adapterEndlessRecyclerMain.dataList.addAll(recipes);
        adapterEndlessRecyclerMain.setLoaded();
        adapterEndlessRecyclerMain.notifyDataSetChanged();

        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
    }

    private void bringRecipes() {
        //olvidamos asynctask y metemos lifecycle, que es mas actual y esta mejor optimizado
        RecipesLoader recipesLoader = new RecipesLoader(ApiClient.getInstance(accessToken).getService());
        recipesLoader.getRecipes().observe(this, this::onRecipesLoaded);
        recipesLoader.loadRecipes();
    }

}