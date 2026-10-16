
package com.example.restaurantmenu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
    private lateinit var backButton: Button

    private var selectedCategory: String? = null
    private var pictureIndex = -1

    private val forest = Color.rgb(20, 65, 47)
    private val green = Color.rgb(30, 91, 65)
    private val gold = Color.rgb(196, 157, 83)
    private val cream = Color.rgb(249, 246, 237)
    private val ink = Color.rgb(40, 43, 37)
    private val muted = Color.rgb(111, 111, 99)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadItems()
        showScreen()
    }

    private fun dp(n: Int): Int =
        (n * resources.displayMetrics.density).toInt()

    private fun roundedBackground(
        color: Int,
        radius: Int = 16,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius).toFloat()
            setColor(color)
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun makeButton(
        label: String,
        backgroundColor: Int = green,
        textColor: Int = white
    ): Button {
        return Button(this).apply {
            text = label
            setTextColor(textColor)
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            background = roundedBackground(backgroundColor, 14)
            isAllCaps = false
            minHeight = dp(48)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun showScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(10))
            setBackgroundColor(cream)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(22), dp(16), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(forest, green)
            ).apply {
                cornerRadius = dp(22).toFloat()
            }
        }

        header.addView(makeText("✦  IBC  ✦", 14f, gold, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
        })

        header.addView(makeText(
            "IBC learning App By Anmol Rai",
            23f,
            white,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        })

        header.addView(makeText(
            "SURRENDER TO THE TASTE",
            11f,
            Color.rgb(239, 219, 170),
            true
        ).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.15f
        })

        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        )

        val addButton = makeButton(
            "＋   Add Food Item",
            forest,
            white
        )
        addButton.setOnClickListener { editItem(-1) }

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
        )

        backButton = makeButton(
            "←   Back to All Categories",
            gold,
            forest
        ).apply {
            visibility = View.GONE
            setOnClickListener {
                selectedCategory = null
                renderItems()
            }
        }

        root.addView(
            backButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        )

        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = false
        }

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

        backButton.visibility =
            if (selectedCategory == null) View.GONE else View.VISIBLE

        val categoriesToShow =
            selectedCategory?.let { listOf(it) } ?: categories

        categoriesToShow.forEach { category ->

            val sectionItems = items.withIndex()
                .filter { it.value.category == category }

            val categoryHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(13), dp(14), dp(13))
                background = roundedBackground(forest, 15, gold)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedCategory = category
                    renderItems()
                }
            }

            categoryHeader.addView(
                makeText("✦", 17f, gold, true).apply {
                    setPadding(0, 0, dp(10), 0)
                }
            )

            categoryHeader.addView(
                makeText(category, 19f, white, true),
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            categoryHeader.addView(
                makeText("${sectionItems.size} items", 11f, gold, true)
            )

            list.addView(
                categoryHeader,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(10)
                }
            )

            if (sectionItems.isEmpty()) {
                val empty = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(12), dp(20), dp(12), dp(20))
                    background = roundedBackground(white, 16, gold)
                }

                empty.addView(makeText(
                    "✧",
                    25f,
                    gold,
                    true
                ).apply {
                    gravity = Gravity.CENTER
                })

                empty.addView(makeText(
                    "No dishes added yet",
                    15f,
                    muted
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(5), 0, 0)
                })

                list.addView(
                    empty,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(12)
                    }
                )
            }

            sectionItems.forEach { indexed ->
                val index = indexed.index
                val item = indexed.value

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), dp(10), dp(10), dp(14))
                    background = roundedBackground(white, 20, gold)
                    elevation = dp(3).toFloat()
                }

                val photo = ImageView(this).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = roundedBackground(cream, 16)
                    contentDescription = item.name
                    clipToOutline = true
                }

                if (item.image.isNotEmpty()) {
                    try {
                        photo.setImageURI(Uri.parse(item.image))
                    } catch (_: Exception) {
                        // Keep the picture area available for replacement.
                    }
                }

                card.addView(
                    photo,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(260)
                    )
                )

                val heading = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                heading.addView(
                    makeText(item.name, 20f, forest, true).apply {
                        setPadding(dp(2), dp(10), dp(4), dp(6))
                    },
                    LinearLayout.LayoutParams(0, -2, 1f)
                )

                heading.addView(Button(this).apply {
                    text = "⋮"
                    textSize = 26f
                    setTextColor(forest)
                    background = roundedBackground(cream, 12)
                    minWidth = dp(44)
                    minimumWidth = dp(44)
                    minHeight = dp(44)
                    setPadding(0, 0, 0, 0)
                    contentDescription = "Options for ${item.name}"
                    setOnClickListener { view ->
                        showDishMenu(index, view)
                    }
                })

                card.addView(heading)

                card.addView(View(this).apply {
                    setBackgroundColor(gold)
                }, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
                ))

                val priceRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(2), dp(9), dp(2), dp(4))
                }

                priceRow.addView(
                    makeText("PRICE", 11f, muted, true),
                    LinearLayout.LayoutParams(0, -2, 1f)
                )

                priceRow.addView(
                    makeText(displayPrice(item.price), 19f, green, true)
                )

                card.addView(priceRow)

                if (item.ingredients.isNotBlank()) {
                    card.addView(makeText(
                        "Ingredients",
                        12f,
                        gold,
                        true
                    ).apply {
                        setPadding(dp(2), dp(8), 0, dp(2))
                    })

                    card.addView(makeText(
                        item.ingredients,
                        14f,
                        ink
                    ).apply {
                        setPadding(dp(2), 0, dp(2), dp(4))
                    })
                }

                list.addView(
                    card,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(14)
                    }
                )
            }
        }
    }

    private fun displayPrice(price: String): String {
        val clean = price.trim()
        return if (clean.contains("OMR", ignoreCase = true)) {
            clean
        } else {
            "OMR $clean"
        }
    }

    private fun showDishMenu(index: Int, anchor: View) {
        if (index !in items.indices) return

        val popup = PopupMenu(this, anchor)
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
        val old = if (index in items.indices) items[index] else null

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
        categorySpinner.setSelection(if (selected >= 0) selected else 0)

        form.addView(makeText("Food name", 14f, forest, true))
        form.addView(name)

        form.addView(makeText(
            "Menu section",
            14f,
            forest,
            true
        ).apply {
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
            hint = "Price in OMR (e.g. 2.500)"
            setSingleLine(true)
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
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
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
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
            hint = "Ingredients, separated by commas"
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
                // Some image providers do not support persisted access.
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
