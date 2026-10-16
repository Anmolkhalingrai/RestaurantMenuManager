package com.example.restaurantmenu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

data class FoodItem(
    var name: String,
    var price: String,
    var image: String = "",
    var category: String = "Starters",
    var ingredients: String = ""
)

class MainActivity : Activity() {

    private val categories = listOf(
    "Salads",
    "Soups",
    "Starters",
    "Dynamites",
    "Baked Potatoes",
    "Pasta & Special",
    "Burgers",
    "Pizzas",
    "Desserts",
    "Fresh Juices",
    "Mojitos",
    "Milkshakes",
    "Smoothies",
    "Hot Coffees",
    "Cold Coffee",
    "Other Drinks",
    "L.T.O"
)

    private val items = mutableListOf<FoodItem>()
    private lateinit var list: LinearLayout
    private var pictureIndex = -1

    private val green = Color.rgb(22, 101, 52)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadItems()
        showScreen()
    }

    private fun dp(n: Int): Int =
        (n * resources.displayMetrics.density).toInt()

    private fun showScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
            setBackgroundColor(Color.rgb(248, 250, 248))
        }

        root.addView(TextView(this).apply {
            text = "IBC learning App By Anmol Rai"
            textSize = 25f
            setTypeface(null, Typeface.BOLD)
            setTextColor(green)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(16))
        })

        root.addView(Button(this).apply {
            text = "+ Add Food Item"
            setBackgroundColor(green)
            setTextColor(Color.WHITE)
            setOnClickListener { editItem(-1) }
        })

        val scroll = ScrollView(this)

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(list)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        renderItems()
    }

    private fun renderItems() {
        list.removeAllViews()

        categories.forEach { category ->
            val sectionItems = items.withIndex()
                .filter { it.value.category == category }

            list.addView(TextView(this).apply {
                text = category
                textSize = 21f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                setBackgroundColor(green)
                setPadding(dp(12), dp(12), dp(12), dp(12))
            })

            if (sectionItems.isEmpty()) {
                list.addView(TextView(this).apply {
                    text = "No items yet"
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                })
            }

            sectionItems.forEach { indexed ->
                val index = indexed.index
                val item = indexed.value

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    setBackgroundColor(Color.WHITE)
                }

                val photo = ImageView(this).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                if (item.image.isNotEmpty()) {
                    try {
                        photo.setImageURI(Uri.parse(item.image))
                    } catch (_: Exception) {
                        // Keep the picture area empty if unavailable.
                    }
                }

                card.addView(
                    photo,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(145)
                    )
                )

                // Dish name and three-dot menu.
                val heading = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                heading.addView(
                    TextView(this).apply {
                        text = item.name
                        textSize = 20f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(Color.BLACK)
                        setPadding(0, dp(8), 0, dp(4))
                    },
                    LinearLayout.LayoutParams(0, -2, 1f)
                )

                heading.addView(Button(this).apply {
                    text = "\u22EE"
                    textSize = 26f
                    minWidth = dp(48)
                    minimumWidth = dp(48)
                    setPadding(0, 0, 0, 0)
                    contentDescription = "Options for ${item.name}"
                    setOnClickListener { showDishMenu(index) }
                })

                card.addView(heading)

                card.addView(TextView(this).apply {
                    text = "Price: ${item.price}"
                    textSize = 17f
                    setTextColor(green)
                })

                if (item.ingredients.isNotBlank()) {
                    card.addView(TextView(this).apply {
                        text = "Ingredients: ${item.ingredients}"
                        textSize = 14f
                        setTextColor(Color.DKGRAY)
                        setPadding(0, dp(6), 0, 0)
                    })
                }

                list.addView(
                    card,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(12)
                    }
                )
            }
        }
    }

    // Three-dot actions for each dish.
    private fun showDishMenu(index: Int) {
        if (index !in items.indices) return

        val item = items[index]
        val popup = PopupMenu(this, list.findViewWithTag(index)
            ?: findDishMenuAnchor(index))

        popup.menu.add(0, 1, 0, "Edit dish")
        popup.menu.add(0, 2, 1, "Edit price")
        popup.menu.add(0, 3, 2, "Change food picture")
        popup.menu.add(0, 4, 3, "Ingredients list")
        popup.menu.add(0, 5, 4, "Delete dish")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> editItem(index)
                2 -> editPrice(index)
                3 -> choosePicture(index)
                4 -> editIngredients(index)
                5 -> confirmDelete(index)
            }
            true
        }

        popup.show()
    }

    // Find the three-dot button belonging to this dish.
    private fun findDishMenuAnchor(index: Int): android.view.View {
        val card = findCardForItem(index)
        return card.getChildAt(1)
            .let { it as LinearLayout }
            .getChildAt(1)
    }

    private fun findCardForItem(index: Int): LinearLayout {
        val item = items[index]
        val section = categories.indexOf(item.category)
        var currentSection = -1

        for (i in 0 until list.childCount) {
            val child = list.getChildAt(i)

            if (child is TextView &&
                child.text.toString() in categories
            ) {
                currentSection++
            }

            if (currentSection == section &&
                child is LinearLayout &&
                child.childCount >= 2
            ) {
                val heading = child.getChildAt(1) as? LinearLayout
                val title = heading?.getChildAt(0) as? TextView

                if (title?.text?.toString() == item.name) {
                    return child
                }
            }
        }

        throw IllegalStateException("Dish card not found")
    }

    private fun confirmDelete(index: Int) {
        if (index !in items.indices) return
        val item = items[index]

        AlertDialog.Builder(this)
            .setTitle("Delete food item?")
            .setMessage("Delete ${item.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                items.removeAt(index)
                saveItems()
                renderItems()
                Toast.makeText(
                    this,
                    "Food item deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun editItem(index: Int) {
        val old = if (index >= 0 && index < items.size) {
            items[index]
        } else {
            null
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }

        val name = EditText(this).apply {
            hint = "Food name"
            setSingleLine(true)
            setText(old?.name ?: "")
        }

        val categorySpinner = Spinner(this)
        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val selected = categories.indexOf(old?.category)
        categorySpinner.setSelection(
            if (selected >= 0) selected else 0
        )

        form.addView(TextView(this).apply {
            text = "Food name"
        })
        form.addView(name)

        form.addView(TextView(this).apply {
            text = "Menu section"
            setPadding(0, dp(12), 0, dp(4))
        })
        form.addView(categorySpinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (old == null) "Add Food Item" else "Edit Dish")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val foodName = name.text.toString().trim()
                    val foodCategory =
                        categorySpinner.selectedItem.toString()

                    if (foodName.isEmpty()) {
                        name.error = "Enter food name"
                        return@setOnClickListener
                    }

                    if (old == null) {
                        // New dishes also need a price.
                        dialog.dismiss()
                        addNewDishWithPrice(foodName, foodCategory)
                        return@setOnClickListener
                    }

                    old.name = foodName
                    old.category = foodCategory

                    saveItems()
                    dialog.dismiss()
                    renderItems()
                }
        }

        dialog.show()
    }

    private fun addNewDishWithPrice(
        foodName: String,
        foodCategory: String
    ) {
        val priceInput = EditText(this).apply {
            hint = "Price (e.g. 2.500 OMR)"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Enter dish price")
            .setView(priceInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val price = priceInput.text.toString().trim()

                if (price.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter a price",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                items.add(
                    FoodItem(
                        name = foodName,
                        price = price,
                        category = foodCategory
                    )
                )

                saveItems()
                renderItems()
            }
            .show()
    }

    private fun editPrice(index: Int) {
        if (index !in items.indices) return

        val item = items[index]
        val input = EditText(this).apply {
            hint = "Price in OMR"
            setSingleLine(true)
            setText(item.price)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit price")
            .setMessage(item.name)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val price = input.text.toString().trim()

                if (price.isNotEmpty()) {
                    item.price = price
                    saveItems()
                    renderItems()
                } else {
                    Toast.makeText(
                        this,
                        "Enter a price",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun editIngredients(index: Int) {
        if (index !in items.indices) return

        val item = items[index]

        val input = EditText(this).apply {
            hint = "Enter ingredients, separated by commas"
            setText(item.ingredients)
            minLines = 4
            gravity = Gravity.TOP
        }

        AlertDialog.Builder(this)
            .setTitle("Ingredients: ${item.name}")
            .setMessage("Enter the ingredients for this dish.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                item.ingredients = input.text.toString().trim()
                saveItems()
                renderItems()
            }
            .show()
    }

    private fun choosePicture(index: Int) {
        if (index !in items.indices) return

        pictureIndex = index

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityForResult(intent, 100)
    }

    @Deprecated("Uses the Android activity result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some document providers do not support persisted grants.
            }

            if (pictureIndex in items.indices) {
                items[pictureIndex].image = uri.toString()
                saveItems()
                renderItems()
            }
        }
    }

    private fun saveItems() {
        val prefs = getSharedPreferences(
            "restaurant_menu",
            MODE_PRIVATE
        )

        prefs.edit().apply {
            putInt("count", items.size)

            items.forEachIndexed { i, item ->
                putString("name_$i", item.name)
                putString("price_$i", item.price)
                putString("image_$i", item.image)
                putString("category_$i", item.category)
                putString("ingredients_$i", item.ingredients)
            }

            apply()
        }
    }

    private fun loadItems() {
        val prefs = getSharedPreferences(
            "restaurant_menu",
            MODE_PRIVATE
        )

        val count = prefs.getInt("count", 0)

        for (i in 0 until count) {
            val savedCategory = prefs.getString(
                "category_$i",
                "Starters"
            ) ?: "Starters"

            items.add(
                FoodItem(
                    name = prefs.getString("name_$i", "") ?: "",
                    price = prefs.getString("price_$i", "") ?: "",
                    image = prefs.getString("image_$i", "") ?: "",
                    category = if (savedCategory in categories) {
                        savedCategory
                    } else {
                        "Starters"
                    },
                    ingredients = prefs.getString(
                        "ingredients_$i",
                        ""
                    ) ?: ""
                )
            )
        }
    }
}
