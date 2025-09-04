package com.beladsoft.phone_background

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.GONE
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.beladsoft.phone_background.FRAMWORK.Provider
import com.beladsoft.phone_background.MODEL.Constants
import com.beladsoft.phone_background.Objects.SettingSources
import com.beladsoft.phone_background.WORKMANAGER.SetAlarmWorker
import java.io.File
import java.util.concurrent.TimeUnit

class SettingActivity : AppCompatActivity() {

    private lateinit var swWEDay: SwitchCompat
    private lateinit var spSources: Spinner
    private lateinit var spSetAs: Spinner
    private lateinit var recycleView: RecyclerView
    private lateinit var chbMobileData: CheckBox
    private lateinit var llBatOpt: LinearLayout
    private lateinit var perf: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sourcesList: MutableList<SettingSources>
    private lateinit var adapter: SettingSourcesAdapter
    private lateinit var toolbarI: androidx.appcompat.widget.Toolbar
    private lateinit var tvSelectedFolder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //late_init
        toolbarI = findViewById(R.id.toolbar)
        swWEDay = findViewById(R.id.swWEDay)
        spSources = findViewById(R.id.spinnerSources)
        spSetAs = findViewById(R.id.spinnerSetAs)
        recycleView = findViewById(R.id.rvSetting)
        chbMobileData = findViewById(R.id.chbMobileData)
        llBatOpt = findViewById(R.id.llOutBatteryOpt)
        tvSelectedFolder = findViewById(R.id.tvFolderPath)
        sourcesList = mutableListOf()
        adapter = SettingSourcesAdapter(this, sourcesList)
        perf = getSharedPreferences(Constants.SETTINGS_DATA, MODE_PRIVATE)
        editor = perf.edit()

        toolbarI.setNavigationOnClickListener { this.finish() }
        // checked if WEDay = On
        val wEDay: Boolean = perf.getBoolean(Constants.WallpaperEveryDay_PERF, false)
        swWEDay.isChecked = wEDay
        swWEDay.setOnClickListener { onSetAutoChanger(swWEDay) }

        //Set RecyclerView
        setupRecyclerView()
        //Set Wallpaper Source
        setupSourcesSpinner()
        //using mobile data
        chbMobileData.isChecked = perf.getBoolean(Constants.MOBILE_DATA, false)
        chbMobileData.setOnClickListener { onMobileData(chbMobileData) }
        //Wallpaper for...
        setupSetForSpinner()
        //Battery optimization
        if (isBatteryIgnored())
            llBatOpt.visibility = GONE
    }

    private fun setupRecyclerView() {
        recycleView.layoutManager = GridLayoutManager(
            this,
            Provider.calculateNoOfColumns(this, 120f)
        )
        recycleView.hasFixedSize()
        adapter = SettingSourcesAdapter(this, sourcesList)
        recycleView.adapter = this.adapter
    }

    private fun loadDownloadedWallpapers() {
        if (!Provider().requestPermission(this, this))
            return
        //firstly check if there actually downloaded images
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/DCIM", Constants.IMG_FOLDER_NAME
        )
        if (file.isDirectory) {
            val images = file.list(Provider.imageFilter)
            if (images != null && images.isNotEmpty()) {
                // Apply changes
                editor.putInt(Constants.SOURCE, 1).apply()
                // Show Wallpapers On RV
                adapter.clear()
                for (img in images) {
                    try {
                        if (!img.contains('_')) continue
                        sourcesList.add(
                            SettingSources(
                                file.absolutePath + "/$img",
                                "", "", 1, false
                            )
                        )
                    } catch (e: Exception) {
                        // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, R.string.youHaventDownloadImages, Toast.LENGTH_SHORT).show()
                spSources.setSelection(perf.getInt(Constants.SOURCE, 0))
            }
        } else {
            spSources.setSelection(perf.getInt(Constants.SOURCE, 0))
            Toast.makeText(this, R.string.youHaventDownloadImages, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        adapter.clear()

        val perf = getSharedPreferences(Constants.SETTINGS_DATA, Context.MODE_PRIVATE)
        val allCategories: String? = perf.getString(Constants.SELECTED_CATEGORIES, "Towns")
        val listOfCategories: MutableList<String>? = allCategories?.split(",")?.toMutableList()
        for (c in listOfCategories!!) if (c.isEmpty() || c.isBlank()) listOfCategories.remove(c)

        val categ: String? = perf.getString(Constants.CLASSES, "TOWNS")
        val categories = categ?.split(",") ?: listOf()
        for (cate in categories) {
            if (cate.isEmpty()) continue
            sourcesList.add(
                SettingSources(
                    Constants.CLASSES, // img url
                    cate, // text
                    "id", // id
                    0, // view type
                    listOfCategories.contains(cate) // is selected
                )
            )
        }
        editor.putInt(Constants.SOURCE, 0).apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.PERMISSION_GRANTED, Toast.LENGTH_SHORT).show()
        } else Toast.makeText(this, R.string.PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
    }

    private fun setupSetForSpinner() {
        val arrSetFor = resources.getStringArray(R.array.set_as)
        val adapter = ArrayAdapter(this, R.layout.item_spinner_items, arrSetFor)
        spSetAs.adapter = adapter
        spSetAs.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                editor.putInt(Constants.SET_AS, position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        spSetAs.setSelection(perf.getInt(Constants.SET_AS, 2))
    }

    private fun setupSourcesSpinner() {
        val arrSources = resources.getStringArray(R.array.sources)
        val adapter = ArrayAdapter(this, R.layout.item_spinner_items, arrSources)
        spSources.adapter = adapter
        spSources.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        // from this app - show folders in recycle view to select
                        loadCategories()
                    }

                    1 -> {
                        //select downloads images - show images
                        loadDownloadedWallpapers()
                    }

                    2 -> {
                        // select from fav images - load fav images
                        loadFavImages()
                    }

                    3 -> {
                        // select a custom folder - select folder
                        loadCustomFolderImages()
                    }
                }
                // remove folder path in other cases
                if (position == 3) tvSelectedFolder.visibility = View.VISIBLE
                else tvSelectedFolder.visibility = GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // from this app - show folders categories
            }
        }
        spSources.setSelection(perf.getInt(Constants.SOURCE, 0), true)
    }

    private fun loadCustomFolderImages() {
        loadFolderImagesToRV()
        // load from perevios chosen folder
    }

    private fun loadFavImages() {
        val favs: String? =
            getSharedPreferences(Provider.FAV_PREF, MODE_PRIVATE).getString(Provider.FAV_PREF, "")
        val favImages = favs?.split(",") ?: listOf()
        if (favImages.isNotEmpty()) {
            adapter.clear()
            //Toast.makeText(this, "favImages[1]= " + favImages[1], Toast.LENGTH_SHORT).show()
            var setEditor = false
            for (fav in favImages) {
                if (fav.isEmpty() || !fav.contains("_")) continue
                if (!setEditor) {
                    editor.putInt(Constants.SOURCE, 2).apply()
                    setEditor = true
                }
                sourcesList.add(SettingSources(fav, "", fav, 2, false))
            }
            if (!setEditor) {
                Toast.makeText(this, R.string.noFavImages, Toast.LENGTH_SHORT).show()
                spSources.setSelection(perf.getInt(Constants.SOURCE, 0))
            }
        } else {
            Toast.makeText(this, R.string.noFavImages, Toast.LENGTH_SHORT).show()
            spSources.setSelection(perf.getInt(Constants.SOURCE, 0))
        }
    }

    fun onSetAutoChanger(view: View) {
        swWEDay.isChecked = !swWEDay.isChecked
        editor.putBoolean(Constants.WallpaperEveryDay_PERF, swWEDay.isChecked).apply()
        setupWorkManager()
    }

    fun setupWorkManager() {
        Log.w("WorkManager", "start func setupWorkManager")
        if (perf.getBoolean(Constants.WallpaperEveryDay_PERF, false)) {
            Log.w("WorkManager", "add work manager")
            // todo if network was not reached register an intent
            // enableBootReceiver(0)

            val tag = Constants.WED_WORK_NAME_ID
            val sourceID = perf.getInt(Constants.SOURCE, 0)
            val builder: Constraints = if (sourceID == 0 || // from app
                sourceID == 2 // from fav
            )
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            else Constraints.Builder().build()

            val periodicWorkRequest = PeriodicWorkRequest
                .Builder(SetAlarmWorker::class.java, 1, TimeUnit.DAYS)
                .addTag(tag)
                .setConstraints(builder)
                .setInitialDelay(1000, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            val instance = WorkManager.getInstance(this)
            instance.enqueueUniquePeriodicWork(
                Constants.WED_WORK_NAME_ID,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )

        } else {
            Log.w("WorkManager", "delete WorkManager")
            WorkManager.getInstance(this)
                .cancelAllWork()
            //enableBootReceiver(3)
        }
        /*
           val list: ListenableFuture<List<WorkInfo>> =
               WorkManager.getInstance(this).getWorkInfosByTag(Constants.WED_WORK_NAME_ID)

           val state: List<WorkInfo> = list.get()
           for (x in state) {
               Toast.makeText(this, x.state.name, Toast.LENGTH_SHORT).show()
           }

         */
    }


    fun onMobileData(view: View) {
        editor.putBoolean(Constants.MOBILE_DATA, !perf.getBoolean(Constants.MOBILE_DATA, false))
            .apply()
        chbMobileData.isChecked = perf.getBoolean(Constants.MOBILE_DATA, false)
    }

    fun onExcludeBOpt(view: View) {
        if (!isBatteryIgnored()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.outOfBatOpt)
                .setMessage(
                    getString(R.string.batOptDet) + getString(R.string.app_name) + getString(
                        R.string.disableIt
                    )
                )
                .setPositiveButton(R.string.OK) { dialogInterface, i ->
                    startActivity(
                        Intent(
                            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                        )
                    )
                }
                .show()
        } else llBatOpt.visibility = View.GONE
    }

    private fun isBatteryIgnored(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.M ||
                getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(
                    packageName
                )
    }

    fun onShareAdvice(view: View) {}
    fun OnTermOfUse(view: View) {}
    fun OnPrivacyPolicy(view: View) {}

    fun OnOutBatteryOpt(view: View) {
        if (!isBatteryIgnored()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.outOfBatOpt)
                .setMessage(
                    getString(R.string.batOptDet) + getString(R.string.app_name) + getString(
                        R.string.disableIt
                    )
                )
                .setPositiveButton(R.string.OK) { dialogInterface, i ->
                    startActivity(
                        Intent(
                            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                        )
                    )
                }
                .show()
        } else llBatOpt.visibility = GONE
    }

    fun onSelectFolderPath(view: View) {
        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        } else {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            Toast.makeText(this, R.string.selectAnyImage, Toast.LENGTH_LONG).show()
        }
        launcher.launch(intent)
    }

    private var launcher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val path = Provider.getPath(
                result.data!!.data!!.path
            )
            // check if the folder contains of images
            val images = File(path).list(Provider.imageFilter)
            if (images != null && images.isNotEmpty()) {
                editor.putString(Constants.FOLDER_PATH, path).apply()
                editor.putInt(Constants.SOURCE, 3).apply()
                tvSelectedFolder.text = path
                tvSelectedFolder.setOnClickListener { view: View? ->
                    if (view != null) {
                        this.onSelectFolderPath(
                            view
                        )
                    }
                }
                loadFolderImagesToRV()
            } else {
                spSources.setSelection(perf.getInt(Constants.SOURCE, 0), true)
                Toast.makeText(this, R.string.noImagesInFolder, Toast.LENGTH_SHORT).show()
            }
        } else {
            spSources.setSelection(perf.getInt(Constants.SOURCE, 0), true)
            Toast.makeText(this, R.string.selectValidFolder, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFolderImagesToRV() {
        // show wallpapers on RV
        adapter.clear()
        tvSelectedFolder.text =
            perf.getString(Constants.FOLDER_PATH, getString(R.string.folder_path))
        val file = File(perf.getString(Constants.FOLDER_PATH, "")!!)

        val images = file.list(Provider.imageFilter)
        if (images != null) {
            if (images.isNotEmpty()) editor.putInt(Constants.SOURCE, 3).apply()
            for (img in images!!) {
                try {
                    sourcesList.add(
                        SettingSources(
                            file.absolutePath + "/$img",
                            "", "", 3, false
                        )
                    )
                } catch (e: Exception) {
                    // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    fun onShowAboutApp(view: View) {
        showAboutApp()
    }

    private fun showAboutApp() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val layout =
            LayoutInflater.from(this).inflate(R.layout.alert_about_app, null, false)
        val dialog = builder.create()
        dialog.setView(layout)
        dialog.setCancelable(false)
        val btnOK = layout.findViewById<Button>(R.id.btnOK)
        val tvWebsite = layout.findViewById<TextView>(R.id.tvWebsite)
        val tvVersion = layout.findViewById<TextView>(R.id.tvVersion)
        dialog.show()
        btnOK.setOnClickListener { view: View? -> dialog.dismiss() }
        tvWebsite.setOnClickListener { view: View? ->
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.beladsoft.com"))
            )
        }
        tvVersion.text = " " + BuildConfig.VERSION_NAME + " ( " + BuildConfig.VERSION_CODE + " ) "
        val ibCall = layout.findViewById<ImageButton>(R.id.ibCall)
        val ibEmail = layout.findViewById<ImageButton>(R.id.ibEmail)
        val ibChat = layout.findViewById<ImageButton>(R.id.ibChat)
        ibCall.setOnClickListener { c: View? ->
            startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:+967776046056"))
            )
        }
        ibEmail.setOnClickListener { e: View? ->
            startActivity(
                Intent(Intent.ACTION_VIEW).setData(Uri.parse("mailto:beladsoft@gmail.com"))
            )
        }
        ibChat.setOnClickListener { c: View? ->
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/+967776046056?text=Hi " + getString(R.string.app_name) + " Developer\n")
                )
            )
        }
    }

    fun onShareApp(view: View) {
        startActivity(
            Intent().setAction(Intent.ACTION_SEND)
                .putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.share_msg) +
                            "https://play.google.com/store/apps/details?id=" + packageName
                )
                .setType("text/plain")
        )
    }


}