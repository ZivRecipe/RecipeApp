package com.example.recipeapplication.Model

import com.google.firebase.database.Exclude

data class RecipeDetails(
    var recipe_id: String? = null,
    var recipe_name: String? = null,
    var recipe_ingredients:String? =null,
    var recipe_step:String? =null,
    var recipe_description:String? =null,
    var recipe_image:String?=null,
    var recipe_status: Int? = null,
    var recipe_type_id: String? = null
    ) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "recipe_name" to recipe_name,
            "recipe_ingredients" to recipe_ingredients,
            "recipe_step" to recipe_step,
            "recipe_description" to recipe_description,
            "recipe_status" to recipe_status,
            "recipe_type_id" to recipe_type_id
        )
    }
}