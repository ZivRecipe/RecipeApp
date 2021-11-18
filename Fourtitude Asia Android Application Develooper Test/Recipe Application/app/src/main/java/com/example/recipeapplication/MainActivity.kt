package com.example.recipeapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.recipeapplication.Model.RecipeType
import com.example.recipeapplication.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //Firebase
    private var recipeTypeDatabaseReference =
        FirebaseDatabase.getInstance().getReference("/RecipeType")


    //List
    private var typeRecipeNameList: ArrayList<String> = arrayListOf()
    private var typeRecipeIdList: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //RecipeTypeName DropDownList
        getRecipeTypeNameData()
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_item, typeRecipeNameList)
        binding.autoCompleteTxt.setAdapter(adapter)






        binding.searchRecipeButton.setOnClickListener {
            recipeTypeDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {

                            var recipeType = item.getValue(RecipeType::class.java)
                            if (recipeType?.recipe_type_name == binding.typeRecipe.editText?.text.toString()) {
                                Log.i("Helping","Haaaaaa")
                                if (validateField()) {
                                    val intent = Intent(this@MainActivity, RecipeList::class.java)
                                    intent.putExtra("recipeTypeId", item.key.toString())
                                    intent.putExtra("recipeTypeName", binding.typeRecipe.editText?.text.toString())
                                    startActivity(intent)

                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })




        }

        binding.addRecipeBtn.setOnClickListener {
            val intent = Intent(this, AddRecipe::class.java)
//            intent.putExtra("asOwnerOrRenter", asOwnerOrRenter)
            startActivity(intent)
        }

    }

    private fun getRecipeTypeNameData() {
        recipeTypeDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    typeRecipeNameList.clear()
                    typeRecipeIdList.clear()
                    for (item in snapshot.children) {
                        var recipeType = item.getValue(RecipeType::class.java)
                        if (recipeType != null) {
                            var recipeTypeName = recipeType?.recipe_type_name.toString()
                            var recipeTypeId = item.key.toString()
                            typeRecipeNameList.add(recipeTypeName)
                            typeRecipeIdList.add(recipeTypeId)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun validateField(): Boolean {

        var canProceed = true


        var recipeType = binding.typeRecipe.editText?.text.toString()
        if (recipeType.isNullOrEmpty()) {
            canProceed = false
            binding.typeRecipe.error = "Please fill in this field."
            binding.typeRecipe.helperText == null
        } else {
            binding.typeRecipe.error = null
            binding.typeRecipe.helperText == null
        }
        return canProceed
    }


}