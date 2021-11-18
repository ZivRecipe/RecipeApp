package com.example.recipeapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.recipeapplication.Model.RecipeDetails
import com.example.recipeapplication.databinding.ActivityAddRecipeBinding
import com.example.recipeapplication.databinding.ActivityViewRecipeDetailsBinding
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class ViewRecipeDetails : AppCompatActivity() {
    //Firebase
    private var recipeDetailsDatabaseReference =
        FirebaseDatabase.getInstance().getReference("/RecipeDetails")

    private lateinit var binding: ActivityViewRecipeDetailsBinding

    private lateinit var recipeId:String
    private lateinit var recipeTypeName:String
    private lateinit var recipeTypeId:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeId = intent.getStringExtra("recipeId").toString()
        recipeTypeName = intent.getStringExtra("recipeTypeName").toString()
        recipeTypeId = intent.getStringExtra("recipeTypeId").toString()
        //If id from Recipe ID is not null, then it will continue

        if (!recipeId.isNullOrEmpty()) {
            recipeDetailsDatabaseReference.child(recipeId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val recipeDetails = task.result?.getValue(RecipeDetails::class.java)
                    if (recipeDetails != null) {

                        if (!recipeDetails.recipe_image.isNullOrEmpty()) {
                            Picasso.get().load(recipeDetails.recipe_image)
                                .into(binding.RecipeDetailsPicture, object :
                                    Callback {
                                    override fun onSuccess() {
                                        Log.i("Picasso", "Image loaded")
                                    }

                                    override fun onError(e: Exception?) {
                                        Log.i(
                                            "Picasso",
                                            "Image not loaded ${e?.stackTraceToString() ?: null}"
                                        )
                                        Picasso.get().load(R.drawable.pnf)
                                            .into(binding.RecipeDetailsPicture)
                                    }
                                })
                        }


                        binding.RecipeDetailsName.setText(recipeDetails.recipe_name)
                        binding.RecipeDetailsDescription.setText(recipeDetails.recipe_description)
                        binding.RecipeDetailsIngredients.setText(recipeDetails.recipe_description)
                        binding.RecipeDetailsStep.setText(recipeDetails.recipe_step)

                    }


                }


            }


        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_delete -> {

                recipeDetailsDatabaseReference.child(recipeId).child("recipe_status").setValue(0)
                Toast.makeText(this, "Recipe Delete Sucessfully", Toast.LENGTH_SHORT).show()
                finish()
            }

            R.id.nav_edit -> {
                val update = "update"
                val intent = Intent(this, AddRecipe::class.java)
                intent.putExtra("recipeId",recipeId)
                intent.putExtra("recipeTypeName",recipeTypeName)
                intent.putExtra("recipeTypeId",recipeTypeId)
                intent.putExtra("update",update)
                startActivity(intent)
                finish()
            }


        }
        return super.onOptionsItemSelected(item)
    }
}