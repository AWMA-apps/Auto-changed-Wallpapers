package com.beladsoft.phone_background

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.*
import com.beladsoft.phone_background.FRAMWORK.Provider
import com.beladsoft.phone_background.MODEL.Constants
import com.beladsoft.phone_background.Objects.SettingSources
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class SettingSourcesAdapter(val context: Context, val list: MutableList<SettingSources>) :
    Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return Items(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting_sources, parent, false)
        )
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hold: Items = holder as Items
        val obj: SettingSources = list[position]
        when (obj.viewType) {
            0 -> { // wallpapers on this app
                val storage = FirebaseStorage.getInstance().reference
                // show background... only
                storage.child(obj.text).child("background.jpg").downloadUrl
                    .addOnSuccessListener {
                        Glide.with(context).load(it.toString()).into(hold.ivImage)
                    }
                if (obj.text.isNotEmpty()) {
                    hold.tvTextView.visibility = VISIBLE
                    hold.tvTextView.text = Provider.getTheTranslation(obj.text,
                        Locale.getDefault().language, context)
                }
                onCategoriesClick(hold, obj)
                //hold.ivImage.setOnClickListener({  })
            }

            1 -> {        // downloaded images
                Glide.with(context).load(obj.imgUri).into(hold.ivImage)
            }

            2 -> {    // fav images
                Log.d("WorkManager",obj.imgUri)
                val folderImg = obj.imgUri.split("_")
                val savedImgUrl: String? = Provider().getUrl(context, folderImg[0], obj.imgUri)
                if (savedImgUrl != null && savedImgUrl.isNotEmpty()) {
                    Glide.with(context).load(savedImgUrl).into(hold.ivImage)
                } else {
                    val storage = FirebaseStorage.getInstance().reference
                    storage.child(obj.imgUri.split("_")[0])
                        .child(obj.imgUri).downloadUrl
                        .addOnSuccessListener {
                            Glide.with(context).load(it).into(hold.ivImage)
                        }
                }
            }

            3 -> {
                Glide.with(context).load(obj.imgUri).into(hold.ivImage)
            }
        }
    }

    private fun onCategoriesClick(hold: Items, var_: SettingSources) {
        if (var_.boolean) {
            hold.civSelectedDone.visibility = VISIBLE
        }

        hold.ivImage.setOnClickListener {
            val perf = context.getSharedPreferences(Constants.SETTINGS_DATA, Context.MODE_PRIVATE)
            val editor = perf.edit()
            var stringAllSelectedCategories: String? =
                perf.getString(Constants.SELECTED_CATEGORIES, "Towns")
            val listOfSelectedCategories: MutableList<String>? =
                stringAllSelectedCategories?.split(",")?.toMutableList()
            for (c in listOfSelectedCategories!!) if (c.isEmpty() || c.isBlank())
                listOfSelectedCategories.remove(c)

            if (var_.boolean) {
                // remove!, its selected
                // if it is the only selected categories don't perform it
                if (listOfSelectedCategories.size == 1) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.oneCategoryMustBeSelected),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // REMOVE CLICKED ITEM
                    listOfSelectedCategories.remove(var_.text)
                    hold.civSelectedDone.visibility = GONE
                    //REBUILD THE SELECTED CATEGORIES
                    val sb = StringBuilder()
                    for (categ in listOfSelectedCategories) if (categ.isNotEmpty())
                        sb.append(categ).append(",")
                    editor.putString(Constants.SELECTED_CATEGORIES, sb.toString()).apply()
                    var_.boolean = false
                }
            } else {
                // add THE SELECTED ITEM
                hold.progressBar.visibility = VISIBLE
                hold.ivImage.isEnabled = false

                // GET FOLDER ITEM COUNT
                val sr = FirebaseStorage.getInstance().reference
                sr.child(var_.text).listAll().addOnSuccessListener {
                    //            FOLDER:12
                    editor.putInt(var_.text + Constants.SIZE, it.items.size - 1).apply()
                    listOfSelectedCategories.add(var_.text)
                    hold.progressBar.visibility = GONE
                    hold.civSelectedDone.visibility = VISIBLE

                    // REBUILD THE SELECTED CATEGORIES
                    val sb = StringBuilder()
                    for (categ in listOfSelectedCategories) if (categ.isNotEmpty())
                        sb.append(categ).append(",")
                    editor.putString(Constants.SELECTED_CATEGORIES, sb.toString()).apply()
                    hold.ivImage.isEnabled = true
                    var_.boolean = true
                }.addOnFailureListener {
                    Toast.makeText(
                        /* context = */ context,
                        /* text = */ context.getString(R.string.errorSelectingCategory),
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                    hold.progressBar.visibility = GONE
                    hold.ivImage.isEnabled = true
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    class Items(itemView: View) : ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        val tvTextView: TextView = itemView.findViewById(R.id.tvTextView)
        val civSelectedDone: CircleImageView = itemView.findViewById(R.id.civSelectedDone)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pbLoadingImagesNumber);
    }


}