
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
    var category: String = "Starters"
)

class MainActivity : Activity() {

    private val categories = listOf(
        "Salads",
        "Soups",
        "Starters",
        "Dynamite Fries",
        "Burgers",
        "Pasta",
        "Pizza",
        "Juice",
        "Smoothie",
        "Mojito",
        "LTO Soft Drinks",
        "Hot Beverages",
        "Coffee"
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
            text = "Restaurant Menu Manager"
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
                    } catch (_: Exception) { }
                }

                card.addView(photo, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(145)
                ))

                card.addView(TextView(this).apply {
                    text = item.name
                    textSize = 20f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.BLACK)
                    setPadding(0, dp(8), 0, dp(4))
                })

                card.addView(TextView(this).apply {
                    text = "Price: ${item.price}"
                    textSize = 17f
                    setTextColor(green)
                })

                val buttons = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                buttons.addView(Button(this).apply {
                    text = "Edit"
                    setOnClickListener { editItem(index) }
                }, LinearLayout.LayoutParams(0, dp(48), 1f))

                buttons.addView(Button(this).apply {
                    text = "Picture"
                    setOnClickListener { choosePicture(index) }
                }, LinearLayout.LayoutParams(0, dp(48), 1f))

                buttons.addView(Button(this).apply {
                    text = "Delete"
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Delete food item?")
                            .setMessage(item.name)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete") { _, _ ->
                                items.removeAt(index)
                                saveItems()
                                renderItems()
                            }
                            .show()
                    }
                }, LinearLayout.LayoutParams(0, dp(48), 1f))

                card.addView(buttons)

                list.addView(card, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                })
            }
        }
    }

    private fun editItem(index: Int) {
        val old = if (index >= 0) items[index] else null

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }

        val name = EditText(this).apply {
            hint = "Food name"
            setSingleLine(true)
            setText(old?.name ?: "")
        }

        val price = EditText(this).apply {
            hint = "Price (e.g. 2.500 OMR)"
            setSingleLine(true)
            setText(old?.price ?: "")
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
            text = "Price"
        })
        form.addView(price)

        form.addView(TextView(this).apply {
            text = "Menu section"
            setPadding(0, dp(12), 0, dp(4))
        })
        form.addView(categorySpinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (old == null) "Add Food Item" else "Edit Food Item")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val foodName = name.text.toString().trim()
                    val foodPrice = price.text.toString().trim()
                    val foodCategory =
                        categorySpinner.selectedItem.toString()

                    if (foodName.isEmpty() || foodPrice.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Enter food name and price",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (old == null) {
                        items.add(
                            FoodItem(
                                foodName,
                                foodPrice,
                                "",
                                foodCategory
                            )
                        )
                    } else {
                        old.name = foodName
                        old.price = foodPrice
                        old.category = foodCategory
                    }

                    saveItems()
                    dialog.dismiss()
                    renderItems()
                }
        }

        dialog.show()
    }

    private fun choosePicture(index: Int) {
        pictureIndex = index
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
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
            } catch (_: Exception) { }

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
                    prefs.getString("name_$i", "") ?: "",
                    prefs.getString("price_$i", "") ?: "",
                    prefs.getString("image_$i", "") ?: "",
                    if (savedCategory in categories)
                        savedCategory else "Starters"
                )
            )
        }
    }
}
