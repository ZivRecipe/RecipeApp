package com.example.recipeapplication.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapplication.Model.RecipeDetails
import com.example.recipeapplication.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class recipesRVAdapter(private val recipeList: ArrayList<RecipeDetails>): RecyclerView.Adapter<recipesRVAdapter.ViewHolder>() {

    // Variable
    private var count: Int = 0
    private lateinit var context: Context
    private lateinit var vListener: onItemClickListener



    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        vListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): recipesRVAdapter.ViewHolder {
        Log.i("TAG", "onCreateViewHolder: " + count++)
        context = parent.context

        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.recipe_card, parent, false)

        return ViewHolder(view, vListener)
    }

    override fun onBindViewHolder(holder: recipesRVAdapter.ViewHolder, position: Int) {
        val currentItem = recipeList[position]
        holder.recipeName.text = currentItem.recipe_name.toString()
        holder.recipeDescription.text = currentItem.recipe_description.toString()

        //ImageView
        if(!currentItem.recipe_image.isNullOrEmpty()){
            Picasso.get().load(currentItem.recipe_image).into(holder.recipePicture,object:
                Callback {
                override fun onSuccess() {
                    Log.i("Picasso","Image loaded")
                }

                override fun onError(e: Exception?) {
                    Log.i("Picasso","Image not loaded ${e?.stackTraceToString() ?: null}")
                    Picasso.get().load(R.drawable.pnf).into(holder.recipePicture)
                }
            })
        }
        else{
            Picasso.get().load(R.drawable.pnf).into(holder.recipePicture)
        }


    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    // This inner class is created manually by me
    inner class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView)  {
        val recipePicture: ImageView = itemView.findViewById(R.id.RecipePicture)
        val recipeName: TextView = itemView.findViewById(R.id.RecipeName)
        val recipeDescription: TextView = itemView.findViewById(R.id.RecipeDescription)



        //We can assume this as constructor in Java
        init{
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }




}