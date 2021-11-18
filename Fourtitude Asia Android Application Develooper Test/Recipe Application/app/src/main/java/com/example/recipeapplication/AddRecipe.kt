package com.example.recipeapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.recipeapplication.Model.RecipeDetails
import com.example.recipeapplication.Model.RecipeType
import com.example.recipeapplication.databinding.ActivityAddRecipeBinding
import com.example.recipeapplication.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.*

class AddRecipe : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding

    //Firebase
    private var recipeDetailsDatabaseReference = FirebaseDatabase.getInstance().getReference("/RecipeDetails")
    private var recipeTypeDatabaseReference = FirebaseDatabase.getInstance().getReference("/RecipeType")

    //List
    private var typeRecipeList: ArrayList<String> = arrayListOf()

    private var imageUr: Intent? = null

    private var update = "add"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addRecipeButton.visibility = View.VISIBLE
        binding.updateRecipeButton.visibility = View.GONE


        //RecipeTypeName DropDownList
        getRecipeTypeNameData()
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_item, typeRecipeList)
        binding.autoCompleteTxt.setAdapter(adapter)


        //select image into the ImageButton
        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                Log.i("PHOTO", data.toString())
                binding.recipeImage.setImageURI(data?.data)
                imageUr = result.data
            } else {
                Log.i(
                    "PHOTO", "CANCELLED"
                ) } }
        binding.recipeImage.setOnClickListener {
            var intent = Intent()
            intent.type = ("image/*")
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent) }



        binding.addRecipeButton.setOnClickListener {

            var recipeType = binding.typeRecipe.editText?.text.toString()
            var recipeName = binding.recipeName.text
            var recipeIngredients = binding.ingredients.text
            var recipeStep = binding.step.text
            var recipeDescription = binding.recipeDescription.text
            var recipe_image = "null"
            var recipeStatus = 1


            recipeTypeDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            var recipeTypes = item.getValue(RecipeType::class.java)
                            if (recipeTypes?.recipe_type_name == recipeType) {

                                if (validateField()) {
                                    val recipeDetailsKey = recipeDetailsDatabaseReference.push().key
                                    var recipeDetailsVal = RecipeDetails(
                                        recipeDetailsKey,
                                        recipeName.toString(),
                                        recipeIngredients.toString(),
                                        recipeStep.toString(),
                                        recipeDescription.toString(),
                                        recipe_image,
                                        recipeStatus,
                                        item.key.toString()
                                    )

                                    recipeDetailsDatabaseReference.child(recipeDetailsKey.toString())
                                        .setValue(recipeDetailsVal).addOnCompleteListener {
                                            if (it.isSuccessful) {

                                                var uuidRecipeImage = UUID.randomUUID().toString()
                                                if (uuidRecipeImage != null) {
                                                    val recipeImageString = "RecipeImage"
                                                    val storageReference = FirebaseStorage.getInstance().getReference("$recipeImageString/$uuidRecipeImage")
                                                    val filePath = imageUr?.data
                                                    if (filePath != null) {

                                                        var uploadTask = storageReference.putFile(filePath)
                                                        uploadTask.addOnSuccessListener() { it ->
                                                            storageReference.downloadUrl.addOnSuccessListener {
                                                                Log.i("FIREBASE", "Location ${it.toString()}")

                                                                var imageReference = it.toString()
                                                                var ref = FirebaseDatabase.getInstance()
                                                                    .getReference("/RecipeDetails")
                                                                    .child(recipeDetailsKey!!)
                                                                    .child("recipe_image")
                                                                    .setValue(imageReference)
                                                                finish()
                                                            }
                                                        }.addOnFailureListener {
                                                            Log.e("FIREBASE", "Profile picture add failure")
                                                        }
                                                    }
                                                }




                                            }
                                        }
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
        update = intent.getStringExtra("update").toString()
        var recipeId = intent.getStringExtra("recipeId").toString()
        var recipeTypeName = intent.getStringExtra("recipeTypeName").toString()
        var recipeTypeId = intent.getStringExtra("recipeTypeId").toString()

        if(update == "update"){
            removeHelperText()
            binding.addRecipeButton.visibility = View.GONE
            binding.updateRecipeButton.visibility = View.VISIBLE
            if (!recipeId.isNullOrEmpty()) {
                recipeDetailsDatabaseReference.child(recipeId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val recipeDetails = task.result?.getValue(RecipeDetails::class.java)

                        if (!recipeDetails?.recipe_image.isNullOrEmpty()) {
                            Picasso.get().load(recipeDetails!!.recipe_image)
                                .into(binding.recipeImage, object :
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
                                            .into(binding.recipeImage)
                                    }
                                })
                        }
                        binding.typeRecipe.hint = recipeTypeName
                        binding.recipeName.setText(recipeDetails!!.recipe_name)
                        binding.ingredients.setText(recipeDetails!!.recipe_ingredients)
                        binding.step.setText(recipeDetails!!.recipe_step)
                        binding.recipeDescription.setText(recipeDetails!!.recipe_description)
                        binding.autoCompleteTxt.setOnItemClickListener { parent, view, position, id ->
                            binding.typeRecipe.hint = "Type of Recipe"
                        }

                    }
                }
            }

        }



        binding.updateRecipeButton.setOnClickListener{
            var recipeType = binding.typeRecipe.editText?.text.toString()
            var recipeName = binding.recipeName.text
            var recipeIngredients = binding.ingredients.text
            var recipeStep = binding.step.text
            var recipeDescription = binding.recipeDescription.text
            var recipe_image = "null"
            var recipeStatus = 1

            if (validateField()) {
                var recipeDetailsVal = RecipeDetails(
                    recipeId,
                    recipeName.toString(),
                    recipeIngredients.toString(),
                    recipeStep.toString(),
                    recipeDescription.toString(),
                    recipe_image,
                    recipeStatus,
                    recipeTypeId
                )

                recipeDetailsDatabaseReference.child(recipeId).updateChildren(recipeDetailsVal.toMap())
                Toast.makeText(this,"Recipe Update Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }





            finish()
        }

    }

    private fun removeHelperText(){
        binding.typeRecipe.helperText = null
        binding.recipeNameContainer.helperText = null
        binding.recipeDescriptionContainer.helperText = null
        binding.stepContainer.helperText = null
        binding.ingredientsContainer.helperText = null

    }

    private fun validateField(): Boolean {

        var canProceed = true


        var recipeType = binding.typeRecipe.editText?.text.toString()
        if (recipeType.isNullOrEmpty()) {
            canProceed = false
            binding.typeRecipe.error = "Please fill in this field."
            binding.typeRecipe.helperText = null
        } else {
            binding.typeRecipe.error = null
            binding.typeRecipe.helperText = null
        }


        var recipeName = binding.recipeName.text.toString()
        if(recipeName .isNullOrEmpty()){
            canProceed = false
            binding.recipeName.error = "Please fill in this field."
            binding.recipeNameContainer.helperText = null
        }else{
            binding.recipeName.error = null
            binding.recipeNameContainer.helperText = null
        }

        var recipeIngredients = binding.ingredients.text.toString()
        if(recipeIngredients .isNullOrEmpty()){
            canProceed = false
            binding.ingredients.error = "Please fill in this field."
            binding.ingredientsContainer.helperText = null
        }else{
            binding.ingredients.error = null
            binding.ingredientsContainer.helperText = null
        }

        var recipeStep = binding.step.text.toString()
        if(recipeStep .isNullOrEmpty()){
            canProceed = false
            binding.step.error = "Please fill in this field."
            binding.stepContainer.helperText = null
        }else{
            binding.step.error = null
            binding.stepContainer.helperText = null
        }

        var recipeDescription = binding.recipeDescription.text.toString()
        if(recipeDescription.isNullOrEmpty()){
            canProceed = false
            binding.recipeDescription.error = "Please fill in this field."
            binding.recipeDescriptionContainer.helperText = null
        }else{
            binding.recipeDescription.error = null
            binding.recipeDescriptionContainer.helperText = null
        }

        return canProceed
    }

    private fun getRecipeTypeNameData() {
        recipeTypeDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    typeRecipeList.clear()
                    for (item in snapshot.children) {
                        var recipeType = item.getValue(RecipeType::class.java)
                        if (recipeType != null) {
                            var recipeTypeName = recipeType?.recipe_type_name.toString()
                            typeRecipeList.add(recipeTypeName)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}