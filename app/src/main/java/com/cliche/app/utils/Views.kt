package com.cliche.app.utils

import android.view.View

/**
 * Sets the visibility of the view based on the [visible] parameter.
 * If [visible] is true, the view is set to VISIBLE; otherwise, it is set to GONE.
 *
 * @param visible Boolean indicating whether the view should be visible or not.
 *
 * @return Unit
 */
fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

