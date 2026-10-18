package com.example.restaurantmenu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.math.BigDecimal
import java.math.RoundingMode

data class DishAddon(var name: String, var price: String, var required: Boolean = false)
data class FoodItem(
    var name: String,
    var price: String,
    var image: String = "",
    var category: String = "Starters",
    var ingredients: String = "",
    var available: Boolean = true,
    val addons: MutableList<DishAddon> = mutableListOf()
)

class MainActivity : Activity() {
    private val categories = listOf("Salads", "Soups", "Starters", "Dynamites", "Baked Potatoes", "Pasta & Special", "Burgers", "Pizzas", "Desserts", "Fresh Juices", "Mojitos", "Milkshakes", "Smoothies", "Hot Coffees", "Cold Coffee", "Other Drinks", "L.T.O")
    private val items = mutableListOf<FoodItem>()
    private val quantities = mutableMapOf<Int, Int>()
    private val chosenAddons = mutableMapOf<Int, MutableSet<Int>>()
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
        showSplashScreen()
    }

    private fun showSplashScreen() {
        val splash = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(24), dp(28), dp(24))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(forest, green))
        }
        splash.addView(makeText("✦", 34f, gold, true).apply { gravity = Gravity.CENTER })
        splash.addView(makeText("IBC APP", 28f, white, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
            setPadding(0, dp(12), 0, dp(6))
        })
        splash.addView(View(this).apply { setBackgroundColor(gold) }, LinearLayout.LayoutParams(dp(110), dp(2)).apply {
            gravity = Gravity.CENTER; topMargin = dp(8); bottomMargin = dp(18)
        })
        splash.addView(makeText("by Anmol Rai", 18f, gold, true).apply { gravity = Gravity.CENTER })
        splash.addView(makeText("SURRENDER TO THE TASTE", 11f, white, true).apply {
            gravity = Gravity.CENTER; setPadding(0, dp(18), 0, 0); letterSpacing = 0.1f
        })
        setContentView(splash)
        Handler(Looper.getMainLooper()).postDelayed({ showScreen() }, 3000L)
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()
    private fun roundedBackground(color: Int, radius: Int = 16, strokeColor: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; cornerRadius = dp(radius).toFloat(); setColor(color); if (strokeColor != Color.TRANSPARENT) setStroke(dp(1), strokeColor) }

    private fun makeButton(label: String, backgroundColor: Int = green, textColor: Int = white): Button = Button(this).apply {
        text = label; setTextColor(textColor); textSize = 14f; setTypeface(null, Typeface.BOLD)
        background = roundedBackground(backgroundColor, 14); isAllCaps = false; minHeight = dp(48); setPadding(dp(12), dp(8), dp(12), dp(8))
    }
    private fun makeText(value: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun fullWidth(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun showScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(10)); setBackgroundColor(cream) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(16), dp(20), dp(16), dp(20))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(forest, green)).apply { cornerRadius = dp(22).toFloat() }
        }
        header.addView(makeText("✦  IBC  ✦", 14f, gold, true).apply { gravity = Gravity.CENTER; letterSpacing = 0.18f })
        header.addView(makeText("Restaurant Menu", 25f, white, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(4)) })
        header.addView(makeText("SURRENDER TO THE TASTE", 11f, Color.rgb(239, 219, 170), true).apply { gravity = Gravity.CENTER; letterSpacing = 0.12f })
        root.addView(header, fullWidth().apply { bottomMargin = dp(12) })
        addButton = makeButton("＋  Add Food Item", forest).apply { setOnClickListener { editItem(-1) } }
        root.addView(addButton, fullWidth().apply { bottomMargin = dp(8) })
        modeButton = makeButton("🛒  Switch to Customer Ordering", gold, forest).apply {
            setOnClickListener { customerMode = !customerMode; selectedCategory = null; if (!customerMode) { quantities.clear(); chosenAddons.clear() }; updateMode() }
        }
        root.addView(modeButton, fullWidth().apply { bottomMargin = dp(8) })
        backButton = makeButton("←  All Categories", gold, forest).apply {
            visibility = View.GONE; setOnClickListener { selectedCategory = null; renderItems() }
        }
        root.addView(backButton, fullWidth().apply { bottomMargin = dp(8) })
        val scroll = ScrollView(this).apply { clipToPadding = false }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        updateMode()
    }

    private fun updateMode() {
        addButton.visibility = if (customerMode) View.GONE else View.VISIBLE
        modeButton.text = if (customerMode) "✎  Switch to Menu Management" else "🛒  Switch to Customer Ordering"
        modeButton.setBackgroundColor(if (customerMode) forest else gold)
        modeButton.setTextColor(if (customerMode) white else forest)
        renderItems()
    }

    private fun renderItems() {
        list.removeAllViews()
        backButton.visibility = if (selectedCategory == null) View.GONE else View.VISIBLE
        if (customerMode) {
            list.addView(makeButton("View Order  •  ${cartCount()} items  •  ${formatMoney(cartTotal())}", forest).apply { setOnClickListener { showOrderSummary() } }, fullWidth().apply { bottomMargin = dp(12) })
        }
        (selectedCategory?.let { listOf(it) } ?: categories).forEach { category ->
            val sectionItems = items.withIndex().filter { it.value.category == category }
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(13), dp(14), dp(13)); background = roundedBackground(forest, 15, gold)
                isClickable = true; isFocusable = true
                setOnClickListener { selectedCategory = if (selectedCategory == category) null else category; renderItems() }
            }
            header.addView(makeText("✦", 17f, gold, true).apply { setPadding(0, 0, dp(10), 0) })
            header.addView(makeText(category, 18f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
            header.addView(makeText("${sectionItems.size} items", 11f, gold, true))
            list.addView(header, fullWidth().apply { topMargin = dp(8); bottomMargin = dp(10) })
            if (sectionItems.isEmpty()) list.addView(makeText("✧\nNo dishes added yet", 15f, muted).apply {
                gravity = Gravity.CENTER; setPadding(dp(12), dp(20), dp(12), dp(20)); background = roundedBackground(white, 16, gold)
            }, fullWidth().apply { bottomMargin = dp(12) })
            sectionItems.forEach { indexed -> list.addView(if (customerMode) customerCard(indexed.index, indexed.value) else adminCard(indexed.index, indexed.value), fullWidth().apply { bottomMargin = dp(14) }) }
        }
    }

    private fun foodPhoto(item: FoodItem): ImageView = ImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP; background = roundedBackground(cream, 16); contentDescription = item.name; clipToOutline = true
        if (item.image.isNotBlank()) try { setImageURI(Uri.parse(item.image)) } catch (_: Exception) { setImageDrawable(null) }
    }

    private fun adminCard(index: Int, item: FoodItem): View {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(14)); background = roundedBackground(white, 20, gold); elevation = dp(2).toFloat() }
        card.addView(foodPhoto(item), fullWidth().apply { height = dp(210); bottomMargin = dp(6) })
        val heading = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        heading.addView(makeText(item.name, 19f, forest, true), LinearLayout.LayoutParams(0, -2, 1f))
        heading.addView(Button(this).apply { text = "⋮"; textSize = 24f; setTextColor(forest); background = roundedBackground(cream, 12); minWidth = dp(44); minHeight = dp(44); setPadding(0, 0, 0, 0); setOnClickListener { showDishMenu(index, this) } })
        card.addView(heading)
        card.addView(makeText(if (item.available) "● AVAILABLE" else "● SOLD OUT", 11f, if (item.available) green else red, true).apply { setPadding(dp(2), dp(4), 0, dp(6)) })
        card.addView(View(this).apply { setBackgroundColor(gold) }, LinearLayout.LayoutParams(-1, dp(1)))
        val priceRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(2), dp(9), dp(2), dp(4)) }
        priceRow.addView(makeText("PRICE", 11f, muted, true), LinearLayout.LayoutParams(0, -2, 1f)); priceRow.addView(makeText(displayPrice(item.price), 19f, green, true)); card.addView(priceRow)
        if (item.ingredients.isNotBlank()) card.addView(makeText("Ingredients: ${item.ingredients}", 14f, ink).apply { setPadding(dp(2), dp(8), dp(2), dp(4)) })
        card.addView(makeButton("Manage add-ons (${item.addons.size})", forest).apply { setOnClickListener { manageAddons(index) } }, fullWidth().apply { topMargin = dp(8) })
        item.addons.forEach { addon -> card.addView(makeText("• ${addon.name} — ${displayPrice(addon.price)}${if (addon.required) " (required)" else ""}", 12f, muted).apply { setPadding(dp(4), dp(4), 0, 0) }) }
        return card
    }

    private fun customerCard(index: Int, item: FoodItem): View {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(12)); background = roundedBackground(white, 20, gold) }
        card.addView(foodPhoto(item), fullWidth().apply { height = dp(185); bottomMargin = dp(8) })
        card.addView(makeText(item.name, 20f, forest, true))
        card.addView(makeText(displayPrice(item.price), 17f, green, true).apply { setPadding(0, dp(5), 0, dp(5)) })
        if (item.ingredients.isNotBlank()) card.addView(makeText(item.ingredients, 13f, muted).apply { setPadding(0, 0, 0, dp(6)) })
        if (item.addons.isNotEmpty()) {
            val picked = chosenAddons[index].orEmpty().mapNotNull { item.addons.getOrNull(it)?.name }
            card.addView(makeText("Add-ons: ${if (picked.isEmpty()) "none selected" else picked.joinToString()}", 12f, muted).apply { setPadding(0, dp(4), 0, dp(4)) })
            card.addView(makeButton("Choose add-ons", gold, forest).apply { setOnClickListener { chooseAddons(index) } }, fullWidth().apply { bottomMargin = dp(6) })
        }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        if (!item.available) controls.addView(makeText("SOLD OUT", 14f, red, true)) else {
            val minus = makeButton("−", forest).apply { minWidth = dp(48) }
            val count = makeText("${quantities[index] ?: 0}", 18f, forest, true).apply { gravity = Gravity.CENTER }
            val plus = makeButton("+", forest).apply { minWidth = dp(48) }
            minus.setOnClickListener { val next = ((quantities[index] ?: 0) - 1).coerceAtLeast(0); if (next == 0) quantities.remove(index) else quantities[index] = next; renderItems() }
            plus.setOnClickListener {
                val chosen = chosenAddons[index].orEmpty()
                val missing = item.addons.indices.any { item.addons[it].required && it !in chosen }
                if (missing) { Toast.makeText(this, "Please choose required add-ons first", Toast.LENGTH_SHORT).show(); chooseAddons(index) }
                else { quantities[index] = (quantities[index] ?: 0) + 1; renderItems() }
            }
            controls.addView(minus); controls.addView(count, LinearLayout.LayoutParams(dp(55), dp(48))); controls.addView(plus)
        }
        card.addView(controls, fullWidth().apply { topMargin = dp(6) })
        return card
    }

    private fun showDishMenu(index: Int, anchor: View) {
        if (index !in items.indices) return
        val item = items[index]; val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Edit dish")
        popup.menu.add(0, 2, 1, "Edit price")
        popup.menu.add(0, 3, 2, "Change food picture")
        popup.menu.add(0, 4, 3, "Ingredients list")
        popup.menu.add(0, 7, 4, "Manage add-ons")
        popup.menu.add(0, 5, 5, "Mark as ${if (item.available) "sold out" else "available"}")
        popup.menu.add(0, 6, 6, "Delete dish")
        popup.setOnMenuItemClickListener { m ->
            when (m.itemId) {
                1 -> editItem(index); 2 -> editPrice(index); 3 -> choosePicture(index); 4 -> editIngredients(index)
                5 -> { item.available = !item.available; if (!item.available) { quantities.remove(index); chosenAddons.remove(index) }; saveItems(); renderItems() }
                6 -> confirmDelete(index); 7 -> manageAddons(index)
            }; true
        }
        popup.show()
    }

    private fun manageAddons(index: Int) {
        if (index !in items.indices) return
        val item = items[index]
        val options = mutableListOf("＋ Add new add-on")
        options.addAll(item.addons.map { "${it.name} — ${displayPrice(it.price)}${if (it.required) " • Required" else ""}" })
        options.add("Done")
        AlertDialog.Builder(this).setTitle("Add-ons for ${item.name}").setItems(options.toTypedArray()) { _, which ->
            when {
                which == 0 -> editAddon(index, -1)
                which in 1..item.addons.size -> AlertDialog.Builder(this).setTitle(item.addons[which - 1].name)
                    .setItems(arrayOf("Edit add-on", "Delete add-on")) { _, action ->
                        if (action == 0) editAddon(index, which - 1) else {
                            item.addons.removeAt(which - 1)
                            chosenAddons[index]?.remove(which - 1)
                            chosenAddons[index] = chosenAddons[index].orEmpty().map { if (it > which - 1) it - 1 else it }.toMutableSet()
                            saveItems(); renderItems(); manageAddons(index)
                        }
                    }.show()
                else -> Unit
            }
        }.setNegativeButton("Close", null).show()
    }

    private fun editAddon(dishIndex: Int, addonIndex: Int) {
        if (dishIndex !in items.indices) return
        val item = items[dishIndex]; val old = item.addons.getOrNull(addonIndex)
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val name = EditText(this).apply { hint = "Add-on name (e.g. Extra cheese)"; setSingleLine(true); setText(old?.name ?: "") }
        val price = EditText(this).apply { hint = "Price in OMR (e.g. 0.500)"; setSingleLine(true); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setText(old?.price ?: "") }
        val required = CheckBox(this).apply { text = "Customer must select this add-on"; setTextColor(ink); isChecked = old?.required ?: false }
        form.addView(name); form.addView(price); form.addView(required)
        val dialog = AlertDialog.Builder(this).setTitle(if (old == null) "Add add-on" else "Edit add-on").setView(form).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val n = name.text.toString().trim(); val p = price.text.toString().trim()
            if (n.isBlank()) { name.error = "Enter add-on name"; return@setOnClickListener }
            if (!validPrice(p)) { price.error = "Enter a valid OMR price"; return@setOnClickListener }
            val addon = DishAddon(n, p, required.isChecked)
            if (old == null) item.addons.add(addon) else item.addons[addonIndex] = addon
            saveItems(); dialog.dismiss(); renderItems(); manageAddons(dishIndex)
        } }
        dialog.show()
    }

    private fun chooseAddons(index: Int) {
        if (index !in items.indices) return
        val item = items[index]; if (item.addons.isEmpty()) return
        val checked = BooleanArray(item.addons.size) { i -> i in chosenAddons[index].orEmpty() }
        val labels = item.addons.map { "${it.name} (+${displayPrice(it.price)})${if (it.required) " • Required" else ""}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choose add-ons for ${item.name}")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save choices") { _, _ ->
                val missing = item.addons.indices.any { item.addons[it].required && !checked[it] }
                if (missing) Toast.makeText(this, "Select all required add-ons", Toast.LENGTH_LONG).show()
                else { chosenAddons[index] = checked.indices.filter { checked[it] }.toMutableSet(); renderItems() }
            }.show()
    }

    private fun confirmDelete(index: Int) {
        if (index !in items.indices) return
        AlertDialog.Builder(this).setTitle("Delete food item?").setMessage("Delete ${items[index].name}?").setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ ->
            items.removeAt(index)
            val q = quantities.toMap(); quantities.clear(); q.forEach { (i, v) -> if (i < index) quantities[i] = v else if (i > index) quantities[i - 1] = v }
            val a = chosenAddons.toMap(); chosenAddons.clear(); a.forEach { (i, v) -> if (i < index) chosenAddons[i] = v else if (i > index) chosenAddons[i - 1] = v }
            saveItems(); renderItems()
        }.show()
    }

    private fun editItem(index: Int) {
        val old = items.getOrNull(index)
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val name = EditText(this).apply { hint = "Food name"; setSingleLine(true); setText(old?.name ?: "") }
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categories); setSelection((categories.indexOf(old?.category)).takeIf { it >= 0 } ?: 0) }
        form.addView(makeText("Food name", 14f, forest, true)); form.addView(name); form.addView(makeText("Menu section", 14f, forest, true).apply { setPadding(0, dp(12), 0, dp(4)) }); form.addView(spinner)
        val dialog = AlertDialog.Builder(this).setTitle(if (old == null) "Add Food Item" else "Edit Dish").setView(form).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val n = name.text.toString().trim(); if (n.isEmpty()) { name.error = "Enter food name"; return@setOnClickListener }
            val cat = spinner.selectedItem.toString(); dialog.dismiss()
            if (old == null) addNewDishWithPrice(n, cat) else { old.name = n; old.category = cat; saveItems(); renderItems() }
        } }; dialog.show()
    }

    private fun addNewDishWithPrice(name: String, category: String) {
        val input = EditText(this).apply { hint = "Price in OMR (e.g. 2.500)"; setSingleLine(true); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this).setTitle("Enter dish price").setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
            val p = input.text.toString().trim(); if (!validPrice(p)) { Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
            items.add(FoodItem(name, p, category = category)); saveItems(); renderItems()
        }.show()
    }

    private fun editPrice(index: Int) {
        val item = items.getOrNull(index) ?: return
        val input = EditText(this).apply { hint = "Price in OMR"; setSingleLine(true); setText(item.price); inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this).setTitle("Edit price").setMessage(item.name).setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
            val p = input.text.toString().trim(); if (validPrice(p)) { item.price = p; saveItems(); renderItems() } else Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show()
        }.show()
    }
    private fun validPrice(value: String): Boolean {
        val amount = value.trim().replace(Regex("(?i)OMR\\s*"), "").toBigDecimalOrNull() ?: return false
        return amount >= BigDecimal.ZERO
    }

    private fun editIngredients(index: Int) {
        val item = items.getOrNull(index) ?: return
        val input = EditText(this).apply { hint = "Ingredients, separated by commas"; setText(item.ingredients); minLines = 4; gravity = Gravity.TOP }
        AlertDialog.Builder(this).setTitle("Ingredients: ${item.name}").setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ -> item.ingredients = input.text.toString().trim(); saveItems(); renderItems() }.show()
    }

    private fun choosePicture(index: Int) {
        if (index !in items.indices) return
        pictureIndex = index
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "image/*"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, 100)
    }
    @Deprecated("Uses Android activity result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            if (pictureIndex in items.indices) { items[pictureIndex].image = uri.toString(); saveItems(); renderItems() }
        }
    }

    private fun cartCount(): Int = quantities.values.sum()
    private fun amount(value: String): BigDecimal = value.trim().replace(Regex("(?i)OMR\\s*"), "").toBigDecimalOrNull() ?: BigDecimal.ZERO
    private fun addonUnitTotal(index: Int): BigDecimal {
        val item = items.getOrNull(index) ?: return BigDecimal.ZERO
        return chosenAddons[index].orEmpty().fold(BigDecimal.ZERO) { total, ai -> total.add(item.addons.getOrNull(ai)?.let { amount(it.price) } ?: BigDecimal.ZERO) }
    }
    private fun lineTotal(index: Int, quantity: Int): BigDecimal {
        val item = items.getOrNull(index) ?: return BigDecimal.ZERO
        return amount(item.price).add(addonUnitTotal(index)).multiply(BigDecimal.valueOf(quantity.toLong()))
    }
    private fun cartTotal(): BigDecimal = quantities.entries.fold(BigDecimal.ZERO) { total, entry ->
        if (entry.key in items.indices && items[entry.key].available) total.add(lineTotal(entry.key, entry.value)) else total
    }
    private fun formatMoney(value: BigDecimal): String = "OMR " + value.setScale(3, RoundingMode.HALF_UP).toPlainString()
    private fun displayPrice(price: String): String = if (price.contains("OMR", true)) price else "OMR $price"

    private fun showOrderSummary() {
        val selected = quantities.filter { it.key in items.indices && it.value > 0 && items[it.key].available }
        if (selected.isEmpty()) { AlertDialog.Builder(this).setTitle("Your order").setMessage("Your order is empty. Add some dishes first.").setPositiveButton("OK", null).show(); return }
        val summary = StringBuilder()
        selected.forEach { (i, q) ->
            val item = items[i]; summary.append(item.name).append(" × ").append(q).append('\n')
            chosenAddons[i].orEmpty().forEach { ai -> item.addons.getOrNull(ai)?.let { summary.append("   + ${it.name} (${displayPrice(it.price)})\n") } }
            summary.append("   ${formatMoney(lineTotal(i, q))}\n\n")
        }
        summary.append("--------------------\nTotal: ").append(formatMoney(cartTotal()))
        AlertDialog.Builder(this).setTitle("Order Summary").setMessage(summary.toString()).setNegativeButton("Continue shopping", null).setNeutralButton("Clear order") { _, _ -> quantities.clear(); chosenAddons.clear(); renderItems() }.setPositiveButton("Confirm order") { _, _ -> showCustomerDetails() }.show()
    }

    private fun showCustomerDetails() {
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val customer = EditText(this).apply { hint = "Customer name (optional)"; setSingleLine(true) }
        val table = EditText(this).apply { hint = "Table number (optional)"; setSingleLine(true) }
        form.addView(customer); form.addView(table)
        AlertDialog.Builder(this).setTitle("Confirm order").setMessage("Total: ${formatMoney(cartTotal())}").setView(form).setNegativeButton("Back", null).setPositiveButton("Create receipt") { _, _ ->
            val receipt = buildString {
                appendLine("IBC RESTAURANT ORDER"); appendLine("Customer: ${customer.text.toString().trim().ifEmpty { "Guest" }}"); appendLine("Table: ${table.text.toString().trim().ifEmpty { "Not specified" }}"); appendLine("--------------------")
                quantities.forEach { (i, q) -> if (i in items.indices && q > 0 && items[i].available) {
                    val item = items[i]; appendLine("${item.name} × $q")
                    chosenAddons[i].orEmpty().forEach { ai -> item.addons.getOrNull(ai)?.let { appendLine("  + ${it.name} (${displayPrice(it.price)})") } }
                    appendLine(formatMoney(lineTotal(i, q)))
                } }
                appendLine("--------------------"); appendLine("TOTAL: ${formatMoney(cartTotal())}")
            }
            AlertDialog.Builder(this).setTitle("Order receipt").setMessage(receipt).setPositiveButton("Share") { _, _ -> startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, receipt) }, "Share order")) }.setNegativeButton("Close", null).show()
            quantities.clear(); chosenAddons.clear(); renderItems()
        }.show()
    }

    private fun saveItems() {
        val prefs = getSharedPreferences("restaurant_menu", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("count", items.size)
            items.forEachIndexed { i, item ->
                putString("name_$i", item.name); putString("price_$i", item.price); putString("image_$i", item.image); putString("category_$i", item.category); putString("ingredients_$i", item.ingredients); putBoolean("available_$i", item.available)
                putInt("addon_count_$i", item.addons.size)
                item.addons.forEachIndexed { ai, addon -> putString("addon_name_${i}_$ai", addon.name); putString("addon_price_${i}_$ai", addon.price); putBoolean("addon_required_${i}_$ai", addon.required) }
            }
            apply()
        }
    }

    private fun loadItems() {
        val prefs = getSharedPreferences("restaurant_menu", MODE_PRIVATE)
        for (i in 0 until prefs.getInt("count", 0)) {
            val savedCategory = prefs.getString("category_$i", "Starters") ?: "Starters"
            val item = FoodItem(
                name = prefs.getString("name_$i", "") ?: "", price = prefs.getString("price_$i", "") ?: "", image = prefs.getString("image_$i", "") ?: "",
                category = if (savedCategory in categories) savedCategory else "Starters", ingredients = prefs.getString("ingredients_$i", "") ?: "", available = prefs.getBoolean("available_$i", true)
            )
            for (ai in 0 until prefs.getInt("addon_count_$i", 0)) item.addons.add(DishAddon(
                prefs.getString("addon_name_${i}_$ai", "") ?: "", prefs.getString("addon_price_${i}_$ai", "0") ?: "0", prefs.getBoolean("addon_required_${i}_$ai", false)
            ))
            items.add(item)
        }
    }
}
