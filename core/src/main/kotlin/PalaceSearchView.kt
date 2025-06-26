package org.thepalaceproject.theme.core

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView

/**
 * The appcompat search view ignores any and all style attributes set in the theme. Evidently
 * the only way to style it is to subclass and then forcefully set colors programmatically.
 */

class PalaceSearchView(
  context: Context,
  attrs: AttributeSet?,
  ignored: Int
) : SearchView(context, attrs, R.style.Palace_Legacy_SearchView) {

  private val searchIconCollapsed: ImageView =
    this.findViewById(androidx.appcompat.R.id.search_button)
  private val searchIconOpen: ImageView =
    this.findViewById(androidx.appcompat.R.id.search_mag_icon)
  private val searchIconClose: ImageView =
    this.findViewById(androidx.appcompat.R.id.search_close_btn)
  private val searchEditText: EditText =
    this.findViewById(androidx.appcompat.R.id.search_src_text)

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(
    context,
    attrs,
    R.style.Palace_Legacy_SearchView
  )

  init {
    val textColor = context.getColor(R.color.PalaceTextColor)
    
    this.searchIconCollapsed.contentDescription = context.getString(R.string.contentDescriptionSearch)
    this.searchIconCollapsed.setImageDrawable(
      AppCompatResources.getDrawable(context, R.drawable.palace_search)
    )
    this.searchIconOpen.setImageDrawable(
      AppCompatResources.getDrawable(context, R.drawable.palace_search)
    )
    this.searchIconClose.contentDescription = context.getString(R.string.contentDescriptionClose)
    this.searchIconClose.setImageDrawable(
      AppCompatResources.getDrawable(context, R.drawable.palace_cancel_24)
    )

    this.searchEditText.setHint(R.string.search)
    this.searchEditText.setHintTextColor(context.getColor(R.color.PalaceTextHintColor))
    this.searchEditText.setTextColor(context.getColor(R.color.PalaceTextColor))

    this.searchIconCollapsed.imageTintMode = PorterDuff.Mode.MULTIPLY
    this.searchIconCollapsed.imageTintList = ColorStateList.valueOf(textColor)
    this.searchIconCollapsed.imageAlpha = 0xff

    this.searchIconOpen.imageTintMode = PorterDuff.Mode.MULTIPLY
    this.searchIconOpen.imageTintList = ColorStateList.valueOf(textColor)
    this.searchIconOpen.imageAlpha = 0xff

    this.searchIconClose.imageTintMode = PorterDuff.Mode.MULTIPLY
    this.searchIconClose.imageTintList = ColorStateList.valueOf(textColor)
    this.searchIconClose.imageAlpha = 0xff
  }
}
