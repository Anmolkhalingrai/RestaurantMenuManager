
package com.example.restaurantmenu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

data class FoodItem(
    var name: String,
    var price: String,
    var image: String = "",
    var category: String = "Starters",
    var ingredients: String = "",
    var available: Boolean = true
)

class MainActivity : Activity() {

    private val categories = listOf(
        "Salads", "Soups", "Starters", "Dynamites",
        "Baked Potatoes", "Pasta & Special", "Burgers",
        "Pizzas", "Desserts", "Fresh Juices", "Mojitos",
        "Milkshakes", "Smoothies", "Hot Coffees",
        "Cold Coffee", "Other Drinks", "L.T.O"
    )

    private val items = mutableListOf<FoodItem>()
    private val quantities = mutableMapOf<Int, Int>()

    private lateinit var list: LinearLayout
    private lateinit var backButton: Button
    private lateinit var addButton: Button
    private lateinit var modeButton: Button

    private var selectedCategory: String? = null
    private var pictureIndex = -1
    private var customerMode = false

    private val forest = Color.rgb(20, 65, 47)
    private val green = Color.rgb(30, 91, 65)
    private val gold = Color.rgb(196, 157, 83)
    private val cream = Color.rgb(249, 246, 237)
    private val ink = Color.rgb(40, 43, 37)
    private val muted = Color.rgb(111, 111, 99)
    private val white = Color.WHITE
    private val red = Color.rgb(169, 48, 48)

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
            setPadding(dp(12), dp(8), dp(12), dp(8))
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
            setPadding(dp(16), dp(20), dp(16), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(forest, green)
            ).apply {
                cornerRadius = dp(22).toFloat()
            }
        }

        header.addView(makeText(
            "✦  IBC  ✦", 14f, gold, true
        ).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
        })

        header.addView(makeText(
            "Restaurant Menu", 25f, white, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        })

        header.addView(makeText(
            "SURRENDER TO THE TASTE", 11f,
            Color.rgb(239, 219, 170), true
        ).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
        })

        root.addView(header, fullWidth().apply {
            bottomMargin = dp(12)
        })

        addButton = makeButton("＋  Add Food Item", forest)
        addButton.setOnClickListener { editItem(-1) }
        root.addView(addButton, fullWidth().apply {
            bottomMargin = dp(8)
        })

        modeButton = makeButton("🛒  Switch to Customer Ordering", gold, forest)
        modeButton.setOnClickListener {
            customerMode = !customerMode
            selectedCategory = null
            if (!customerMode) quantities.clear()
            updateMode()
        }
        root.addView(modeButton, fullWidth().apply {
            bottomMargin = dp(8)
        })

        backButton = makeButton("←  All Categories", gold, forest)
        backButton.visibility = View.GONE
        backButton.setOnClickListener {
            selectedCategory = null
            renderItems()
        }
        root.addView(backButton, fullWidth().apply {
            bottomMargin = dp(8)
        })

        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = false
        }

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(root)
        updateMode()
    }

    private fun fullWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun updateMode() {
        addButton.visibility =
            if (customerMode) View.GONE else View.VISIBLE

        modeButton.text = if (customerMode) {
            "✎  Switch to Menu Management"
        } else {
            "🛒  Switch to Customer Ordering"
        }

        modeButton.setBackgroundColor(if (customerMode) forest else gold)
        modeButton.setTextColor(if (customerMode) white else forest)

        renderItems()
    }

    private fun renderItems() {
        list.removeAllViews()

        backButton.visibility =
            if (selectedCategory == null) View.GONE else View.VISIBLE

        if (customerMode) {
            val checkout = makeButton(
                "View Order  •  ${cartCount()} items  •  ${formatMoney(cartTotal())}",
                forest, white
            )
            checkout.setOnClickListener { showOrderSummary() }
            list.addView(checkout, fullWidth().apply {
                topMargin = dp(4)
                bottomMargin = dp(12)
            })
        }

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
                    selectedCategory =
                        if (selectedCategory == category) null else category
                    renderItems()
                }
            }

            categoryHeader.addView(makeText("✦", 17f, gold, true).apply {
                setPadding(0, 0, dp(10), 0)
            })

            categoryHeader.addView(
                makeText(category, 18f, white, true),
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            categoryHeader.addView(makeText(
                "${sectionItems.size} items", 11f, gold, true
            ))

            list.addView(categoryHeader, fullWidth().apply {
                topMargin = dp(8)
                bottomMargin = dp(10)
            })

            if (sectionItems.isEmpty()) {
                val empty = makeText(
                    "✧\nNo dishes added yet", 15f, muted
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(dp(12), dp(20), dp(12), dp(20))
                    background = roundedBackground(white, 16, gold)
                }
                list.addView(empty, fullWidth().apply {
                    bottomMargin = dp(12)
                })
            }

            sectionItems.forEach { indexed ->
                val index = indexed.index
                val item = indexed.value
                list.addView(
                    if (customerMode) {
                        customerCard(index, item)
                    } else {
                        adminCard(index, item)
                    },
                    fullWidth().apply {
                        bottomMargin = dp(14)
                    }
                )
            }
        }

        if (customerMode && items.none { it.available }) {
            list.addView(makeText(
                "Currently, all dishes are marked sold out.",
                14f, red, true
            ).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(16), dp(8), dp(16))
            })
        }
    }

    private fun adminCard(index: Int, item: FoodItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(14))
            background = roundedBackground(white, 20, gold)
            elevation = dp(2).toFloat()
        }

        card.addView(foodPhoto(item), fullWidth().apply {
            height = dp(220)
            bottomMargin = dp(6)
        })

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        heading.addView(makeText(item.name, 19f, forest, true),
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                rightMargin = dp(4)
            })

        heading.addView(Button(this).apply {
            text = "⋮"
            textSize = 24f
            setTextColor(forest)
            background = roundedBackground(cream, 12)
            minWidth = dp(44)
            minHeight = dp(44)
            setPadding(0, 0, 0, 0)
            contentDescription = "Options for ${item.name}"
            setOnClickListener { showDishMenu(index, this) }
        })

        card.addView(heading)
        card.addView(makeText(
            if (item.available) "● AVAILABLE" else "● SOLD OUT",
            11f, if (item.available) green else red, true
        ).apply { setPadding(dp(2), dp(4), 0, dp(6)) })

        card.addView(View(this).apply {
            setBackgroundColor(gold)
        }, LinearLayout.LayoutParams(-1, dp(1)))

        val priceRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(9), dp(2), dp(4))
        }

        priceRow.addView(makeText("PRICE", 11f, muted, true),
            LinearLayout.LayoutParams(0, -2, 1f))
        priceRow.addView(makeText(displayPrice(item.price), 19f, green, true))
        card.addView(priceRow)

        if (item.ingredients.isNotBlank()) {
            card.addView(makeText(
                "Ingredients", 12f, gold, true
            ).apply { setPadding(dp(2), dp(8), 0, dp(2)) })

            card.addView(makeText(item.ingredients, 14f, ink).apply {
                setPadding(dp(2), 0, dp(2), dp(4))
            })
        }

        return card
    }

    private fun customerCard(index: Int, item: FoodItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(12))
            background = roundedBackground(white, 20, gold)
        }

        card.addView(foodPhoto(item), fullWidth().apply {
            height = dp(190)
            bottomMargin = dp(8)
        })

        card.addView(makeText(item.name, 20f, forest, true))
        card.addView(makeText(
            displayPrice(item.price), 17f, green, true
        ).apply { setPadding(0, dp(5), 0, dp(5)) })

        if (item.ingredients.isNotBlank()) {
            card.addView(makeText(item.ingredients, 13f, muted).apply {
                setPadding(0, 0, 0, dp(6))
            })
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        if (!item.available) {
            controls.addView(makeText(
                "SOLD OUT", 14f, red, true
            ))
        } else {
            val minus = makeButton("−", forest)
            minus.minWidth = dp(48)
            val count = makeText(
                "${quantities[index] ?: 0}", 18f, forest, true
            ).apply {
                gravity = Gravity.CENTER
            }
            val plus = makeButton("+", forest)
            plus.minWidth = dp(48)

            minus.setOnClickListener {
                val next = ((quantities[index] ?: 0) - 1).coerceAtLeast(0)
                if (next == 0) quantities.remove(index)
                else quantities[index] = next
                renderItems()
            }

            plus.setOnClickListener {
                quantities[index] = (quantities[index] ?: 0) + 1
                renderItems()
            }

            controls.addView(minus)
            controls.addView(count, LinearLayout.LayoutParams(
                dp(55), dp(48)
            ))
            controls.addView(plus)
        }

        card.addView(controls, fullWidth().apply {
            topMargin = dp(6)
        })

        return card
    }

    private fun foodPhoto(item: FoodItem): ImageView {
        val photo = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedBackground(cream, 16)
            contentDescription = item.name
            clipToOutline = true
        }

        if (item.image.isNotBlank()) {
            try {
                photo.setImageURI(Uri.parse(item.image))
            } catch (_: Exception) {
                photo.setImageDrawable(null)
            }
        }

        return photo
    }

    private fun showDishMenu(index: Int, anchor: View) {
        if (index !in items.indices) return

        val item = items[index]
        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, "Edit dish")
        popup.menu.add(0, 2, 1, "Edit price")
        popup.menu.add(0, 3, 2, "Change food picture")
        popup.menu.add(0, 4, 3, "Ingredients list")
        popup.menu.add(0, 5, 4, "Mark as ${if (item.available) "sold out" else "available"}")
        popup.menu.add(0, 6, 5, "Delete dish")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> editItem(index)
                2 -> editPrice(index)
                3 -> choosePicture(index)
                4 -> editIngredients(index)
                5 -> {
                    item.available = !item.available
                    if (!item.available) quantities.remove(index)
                    saveItems()
                    renderItems()
                    Toast.makeText(
                        this,
                        if (item.available) "Dish available" else "Dish sold out",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                6 -> confirmDelete(index)
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
                val remapped = quantities.toMap()
                quantities.clear()
                remapped.forEach { (oldIndex, quantity) ->
                    when {
                        oldIndex < index -> quantities[oldIndex] = quantity
                        oldIndex > index -> quantities[oldIndex - 1] = quantity
                    }
                }
                saveItems()
                renderItems()
                Toast.makeText(this, "Food item deleted", Toast.LENGTH_SHORT).show()
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

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val selected = categories.indexOf(old?.category)
        spinner.setSelection(if (selected >= 0) selected else 0)

        form.addView(makeText("Food name", 14f, forest, true))
        form.addView(name)
        form.addView(makeText("Menu section", 14f, forest, true).apply {
            setPadding(0, dp(12), 0, dp(4))
        })
        form.addView(spinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (old == null) "Add Food Item" else "Edit Dish")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val foodName = name.text.toString().trim()
                if (foodName.isEmpty()) {
                    name.error = "Enter food name"
                    return@setOnClickListener
                }

                val category = spinner.selectedItem.toString()
                dialog.dismiss()

                if (old == null) {
                    addNewDishWithPrice(foodName, category)
                } else {
                    old.name = foodName
                    old.category = category
                    saveItems()
                    renderItems()
                }
            }
        }
        dialog.show()
    }

    private fun addNewDishWithPrice(foodName: String, category: String) {
        val input = EditText(this).apply {
            hint = "Price in OMR (e.g. 2.500)"
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(this)
            .setTitle("Enter dish price")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val price = input.text.toString().trim()
                if (!validPrice(price)) {
                    Toast.makeText(
                        this, "Enter a valid price, e.g. 2.500",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                items.add(FoodItem(
                    name = foodName,
                    price = price,
                    category = category
                ))
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
            inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(this)
            .setTitle("Edit price")
            .setMessage(item.name)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val price = input.text.toString().trim()
                if (validPrice(price)) {
                    item.price = price
                    saveItems()
                    renderItems()
                } else {
                    Toast.makeText(
                        this, "Enter a valid price",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun validPrice(value: String): Boolean {
        val amount = value.trim().toBigDecimalOrNull() ?: return false
        return amount >= java.math.BigDecimal.ZERO
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
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Provider may not support persisted permission.
            }

            if (pictureIndex in items.indices) {
                items[pictureIndex].image = uri.toString()
                saveItems()
                renderItems()
            }
        }
    }

    private fun cartCount(): Int =
        quantities.values.sum()

    private fun itemAmount(item: FoodItem): java.math.BigDecimal =
        item.price.trim()
            .replace(Regex("(?i)OMR\\s*"), "")
            .toBigDecimalOrNull()
            ?: java.math.BigDecimal.ZERO

    private fun cartTotal(): java.math.BigDecimal {
        var total = java.math.BigDecimal.ZERO
        quantities.forEach { (index, quantity) ->
            if (index in items.indices && items[index].available) {
                total = total.add(
                    itemAmount(items[index]).multiply(
                        java.math.BigDecimal.valueOf(quantity.toLong())
                    )
                )
            }
        }
        return total
    }

    private fun formatMoney(amount: java.math.BigDecimal): String =
        "OMR " + amount.setScale(
            3, java.math.RoundingMode.HALF_UP
        ).toPlainString()

    private fun displayPrice(price: String): String {
        val clean = price.trim()
        return if (clean.contains("OMR", ignoreCase = true)) {
            clean
        } else {
            "OMR $clean"
        }
    }

    private fun showOrderSummary() {
        val selected = quantities.filter { (index, quantity) ->
            index in items.indices && quantity > 0 && items[index].available
        }

        if (selected.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Your order")
                .setMessage("Your order is empty. Add some dishes first.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val summary = StringBuilder()
        selected.forEach { (index, quantity) ->
            val item = items[index]
            val lineTotal = itemAmount(item).multiply(
                java.math.BigDecimal.valueOf(quantity.toLong())
            )
            summary.append(item.name)
                .append("  × ")
                .append(quantity)
                .append("\n")
                .append("   ")
                .append(formatMoney(lineTotal))
                .append("\n\n")
        }

        summary.append("--------------------\n")
            .append("Total: ")
            .append(formatMoney(cartTotal()))

        AlertDialog.Builder(this)
            .setTitle("Order Summary")
            .setMessage(summary.toString())
            .setNegativeButton("Continue shopping", null)
            .setNeutralButton("Clear order") { _, _ ->
                quantities.clear()
                renderItems()
            }
            .setPositiveButton("Confirm order") { _, _ ->
                showCustomerDetails()
            }
            .show()
    }

    private fun showCustomerDetails() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }

        val customerName = EditText(this).apply {
            hint = "Customer name (optional)"
            setSingleLine(true)
        }
        val tableNumber = EditText(this).apply {
            hint = "Table number (optional)"
            setSingleLine(true)
        }

        form.addView(customerName)
        form.addView(tableNumber)

        AlertDialog.Builder(this)
            .setTitle("Confirm order")
            .setMessage("Total: ${formatMoney(cartTotal())}")
            .setView(form)
            .setNegativeButton("Back", null)
            .setPositiveButton("Create receipt") { _, _ ->
                val receipt = buildString {
                    appendLine("RESTAURANT ORDER")
                    appendLine("Customer: ${customerName.text.toString().trim().ifEmpty { "Guest" }}")
                    appendLine("Table: ${tableNumber.text.toString().trim().ifEmpty { "Not specified" }}")
                    appendLine("--------------------")

                    quantities.forEach { (index, quantity) ->
                        if (index in items.indices &&
                            quantity > 0 && items[index].available
                        ) {
                            val item = items[index]
                            val lineTotal = itemAmount(item).multiply(
                                java.math.BigDecimal.valueOf(quantity.toLong())
                            )
                            appendLine("${item.name} × $quantity")
                            appendLine(formatMoney(lineTotal))
                        }
                    }

                    appendLine("--------------------")
                    appendLine("TOTAL: ${formatMoney(cartTotal())}")
                    appendLine("Order stored on this device only.")
                }

                AlertDialog.Builder(this)
                    .setTitle("Order receipt")
                    .setMessage(receipt)
                    .setPositiveButton("Share") { _, _ ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, receipt)
                        }
                        startActivity(
                            Intent.createChooser(shareIntent, "Share order")
                        )
                    }
                    .setNegativeButton("Close", null)
                    .show()

                quantities.clear()
                renderItems()
            }
            .show()
    }

    private fun saveItems() {
        val prefs = getSharedPreferences(
            "restaurant_menu", MODE_PRIVATE
        )

        prefs.edit().apply {
            putInt("count", items.size)

            items.forEachIndexed { i, item ->
                putString("name_$i", item.name)
                putString("price_$i", item.price)
                putString("image_$i", item.image)
                putString("category_$i", item.category)
                putString("ingredients_$i", item.ingredients)
                putBoolean("available_$i", item.available)
            }

            apply()
        }
    }

    private fun loadItems() {
        val prefs = getSharedPreferences(
            "restaurant_menu", MODE_PRIVATE
        )

        val count = prefs.getInt("count", 0)

        for (i in 0 until count) {
            val savedCategory = prefs.getString(
                "category_$i", "Starters"
            ) ?: "Starters"

            items.add(FoodItem(
                name = prefs.getString("name_$i", "") ?: "",
                price = prefs.getString("price_$i", "") ?: "",
                image = prefs.getString("image_$i", "") ?: "",
                category = if (savedCategory in categories) {
                    savedCategory
                } else {
                    "Starters"
                },
                ingredients = prefs.getString(
                    "ingredients_$i", ""
                ) ?: "",
                available = prefs.getBoolean("available_$i", true)
            ))
        }
    }
}
