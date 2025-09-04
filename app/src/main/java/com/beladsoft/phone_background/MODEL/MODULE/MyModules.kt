package com.beladsoft.phone_background.MODEL.MODULE

//public String itemID,itemImageUrl,itemTitle;
//    public int viewType;

data class HomeModule(
    var itemID: String? = null,
    var itemImageUrl: String? = null,
    var itemTitle: String? = null,
    var viewType: Int? = null
)

data class WallpaperModule(
    var img: String? = null,
    var folder: String? = null,
    var viewType: Int? = null,
    var fav: Boolean? = null
)

data class SettingSources(
    val imgUri: String,
    val text: String,
    val id: String,
    val viewType: Int,
    var boolean: Boolean
)