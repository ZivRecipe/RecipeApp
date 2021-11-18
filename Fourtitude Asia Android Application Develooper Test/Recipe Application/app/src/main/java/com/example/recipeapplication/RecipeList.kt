package com.example.recipeapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapplication.Adapter.recipesRVAdapter
import com.example.recipeapplication.Model.RecipeDetails
import com.example.recipeapplication.databinding.ActivityMainBinding
import com.example.recipeapplication.databinding.ActivityRecipeListBinding
import com.google.firebase.database.*

class RecipeList : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeListBinding

    //Map of Database Reference and Listener
    private val listenerMap = mutableMapOf<DatabaseReference, ValueEventListener>()
    //Initialize UI Component Here


    //List
    private var recipeDetailsList: ArrayList<RecipeDetails> = arrayListOf()
    //Database
    private var recipeDetailsDatabase = FirebaseDatabase.getInstance().getReference("/RecipeDetails")

    private lateinit var recipeTypeId:String
    private lateinit var recipeTypeName:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_recipe_list)
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeTypeId = intent.getStringExtra("recipeTypeId").toString()
        recipeTypeName = intent.getStringExtra("recipeTypeName").toString()


        //Launch Recycle View Vehicle List
        binding.recipeListRV.layoutManager = LinearLayoutManager(this)
        binding.recipeListRV.setHasFixedSize(true)
        getFirebaseData()

    }

    private fun getFirebaseData(){

        var listener = recipeDetailsDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    recipeDetailsList.clear()
                    for(item in snapshot.children){
                        var recipeDetails = item.getValue(RecipeDetails::class.java)

                        //recipe_status  0:delete recipe 1:recipe available
                        if(recipeDetails?.recipe_type_id == recipeTypeId && recipeDetails?.recipe_status == 1){
                            recipeDetails.recipe_id = item.key.toString()
                         recipeDetailsList.add(recipeDetails)
                        }



                    }

                    val adapter = recipesRVAdapter(recipeDetailsList)
                    binding.recipeListRV.adapter = adapter
                    adapter.setOnItemClickListener(object :
                        recipesRVAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@RecipeList , ViewRecipeDetails::class.java)
                           intent.putExtra("recipeId",recipeDetailsList[position].recipe_id)
                            intent.putExtra("recipeTypeName",recipeTypeName)
                            intent.putExtra("recipeTypeId",recipeTypeId)
                            startActivity(intent)
                            finish()
                        }
                    })
                    adapter.notifyDataSetChanged()


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })




        listenerMap[recipeDetailsDatabase] = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        for(item in listenerMap){
            var ref = item.key
            ref.removeEventListener(item.value)
        }
    }
}