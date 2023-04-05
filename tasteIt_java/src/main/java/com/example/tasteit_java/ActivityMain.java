package com.example.tasteit_java;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasteit_java.ApiService.ApiClient;
import com.example.tasteit_java.ApiService.ApiRequests;
import com.example.tasteit_java.ApiService.RecipeId_Recipe_User;
import com.example.tasteit_java.adapters.AdapterGridViewMain;
import com.example.tasteit_java.adapters.AdapterRecyclerMain;
import com.example.tasteit_java.bdConnection.BdConnection;
import com.example.tasteit_java.clases.Recipe;
import com.example.tasteit_java.clases.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityMain extends AppCompatActivity {

    private FloatingActionButton bCreate;
    //TEMPORAL GALERIA
    private GridView gvRecipes;
    private AdapterGridViewMain adapter;
    private RecyclerView rvRecipes;
    private AdapterRecyclerMain adapterRecyclerView;
    public static ArrayList<Recipe> listRecipes = new ArrayList<>();
    private ProgressBar pgMain;

    private String token;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pgMain = findViewById(R.id.pbMain);
        rvRecipes = findViewById(R.id.rvRecipes);
        //gvRecipes = findViewById(R.id.gvRecipes);
        bCreate = findViewById(R.id.bCreate);

        bringRecipes();

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



        /*
        //grid view
        gvRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int posicion, long l) {
                Intent i = new Intent(ActivityMain.this, ActivityRecipe.class);
                i.putExtra("recipeId", listRecipes.get(posicion).getId());
                startActivity(i);
            }
        });
        gvRecipes.setOnScrollListener(new AbsListView.OnScrollListener(){
            int currentScrollState, currentVisibleItemCount, currentFirstVisibleItem, currentTotalItemCount, mLastFirstVisibleItem;
            boolean canScrollV, scrollingUp;
            @Override
            public void onScrollStateChanged (AbsListView view,int scrollState){
                currentScrollState = scrollState;
                if (scrollingUp && !canScrollV) {
                    if (currentFirstVisibleItem == 0) {
                        bringRecipes();
                        Toast.makeText(ActivityMain.this, "Refreshing...", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        scrollingUp = false;
                    }
                }
            }
            @Override
            public void onScroll (AbsListView view,int firstVisibleItem, int visibleItemCount,
                                  int totalItemCount){
                canScrollV = gvRecipes.canScrollVertically(-1);
                if(mLastFirstVisibleItem < firstVisibleItem){
                    // Scrolling down
                    scrollingUp = false;
                }
                if(mLastFirstVisibleItem > firstVisibleItem){
                    // scrolling up
                    scrollingUp = true;
                }
                mLastFirstVisibleItem = firstVisibleItem;
                currentFirstVisibleItem = firstVisibleItem;
                currentVisibleItemCount = visibleItemCount;
                currentTotalItemCount = totalItemCount;
            }
        });
        */
    }

    //MENU superior
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //Intento de sacar la img de perfil redonda
        Bitmap originalBitmap = Utils.decodeBase64(new BdConnection().retrieveUserbyUid(token).getImgProfile());

        int desiredWidth = 400;
        int desiredHeight = 400;
        float scaleWidth = ((float) desiredWidth) / originalBitmap.getWidth();
        float scaleHeight = ((float) desiredHeight) / originalBitmap.getHeight();
        float scaleFactor = Math.min(scaleWidth, scaleHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);

        Bitmap bitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

        Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth()+100, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundedBitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(15f); // ajusta este valor para cambiar el ancho del borde
        borderPaint.setColor(Color.BLACK);

        float centerX = bitmap.getWidth() / 1.75f;
        float centerY = bitmap.getHeight() / 2f;
        float radiusX = bitmap.getWidth() / 1.5f;
        float radiusY = bitmap.getHeight() / 2f;
        canvas.drawOval(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY, borderPaint);
        canvas.drawOval(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY, paint);

        BitmapDrawable roundedBitmapDrawable = new BitmapDrawable(getResources(), roundedBitmap);
        menu.getItem(0).setIcon(roundedBitmapDrawable);
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

            apiRequests.getAllRecipes().enqueue(new Callback<List<RecipeId_Recipe_User>>() {
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
        pgMain.setVisibility(View.GONE);

        listRecipes = (ArrayList<Recipe>) recipes;
        /*
        adapter = new AdapterGridViewMain(getApplicationContext(), listRecipes);
        gvRecipes.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        */

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvRecipes.setLayoutManager(linearLayoutManager);
        adapterRecyclerView = new AdapterRecyclerMain(listRecipes);
        rvRecipes.setAdapter(adapterRecyclerView);
        /*
        rvRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Verificar si el usuario se desplaza hacia arriba y está cerca del principio de la lista
                if (dy < 0 && linearLayoutManager.findFirstVisibleItemPosition() < 10) {
                    // Recargar el RecyclerView
                    Toast.makeText(ActivityMain.this, "Refreshing...", Toast.LENGTH_SHORT).show();
                    adapterRecyclerView.notifyDataSetChanged();
                }
            }
        });
        */

    }

    private void bringRecipes() {
        //olvidamos asynctask y metemos lifecycle, que es mas actual y esta mejor optimizado
        RecipesLoader recipesLoader = new RecipesLoader(ApiClient.getInstance().getService());

        recipesLoader.getRecipes().observe(this, this::onRecipesLoaded);

        recipesLoader.loadRecipes();
    }

}