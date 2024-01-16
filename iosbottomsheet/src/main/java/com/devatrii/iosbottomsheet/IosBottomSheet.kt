package com.devatrii.iosbottomsheet

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.math.MathUtils.lerp

open class IosBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private var MIN_SCALE_X = 0.94f
        private var MAX_SCALE_X = 1f

        private var MIN_SCALE_Y = 0.98f
        private var MAX_SCALE_Y = 1f

        private var MIN_CORNER_RADIUS = 0f
        private var MAX_CORNER_RADIUS = 100f

        private var DEFAULT_STATE = BottomSheetBehavior.STATE_EXPANDED
        private var EXPANDED_OFFSET = 80

        private var DIM_AMOUNT = 0.15f

        private var BACKGROUND_COLOR_ID: Int = Color.BLACK
        private var STATUS_BAR_COLOR_ID: Int = Color.BLACK

        private var BACKGROUND_COLOR_HEX: String? = null
        private var STATUS_BAR_COLOR_HEX: String? = null

        private var SHOULD_CHANGE_STATUS_BAR_COLOR = true

        private const val TAG = "IosBottomSheet"
        private const val STATIC_ANIMATION_DURATION = 200L
        /* TODO
        *  Animate Alpha in v2
        * */
    }

    private var originalStatusBarColor: Int? = null
    private var offset = 1f


    override fun onStart() {
        super.onStart()
        setScrimDimAmount()
        animateBottomSheet()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        originalStatusBarColor?.let {
            requireActivity().window.statusBarColor = it
        }

        val contentView = requireActivity().findViewById<FrameLayout>(android.R.id.content)
        staticAnimate(contentView, true)
    }


    // main functions
    private fun setScrimDimAmount() {
        val dialog = dialog
        val window = dialog?.window
        window?.setDimAmount(DIM_AMOUNT)
    }

    private fun animateBottomSheet() {
        val dialog = dialog
        // set enter anim
        dialog?.window?.setWindowAnimations(R.style.SlideAnimation)


        val activity = requireActivity()
        originalStatusBarColor = activity.window.statusBarColor
        val contentView = requireActivity().findViewById<FrameLayout>(android.R.id.content)
        val rootView = contentView.parent.parent.parent as LinearLayout
        activity.setStatusBarBackgroundColor()
        rootView.setRootBackgroundColor()
        contentView.setBackgroundColor(activity.getColorFromAttribute(R.attr.bgColor))


        val modalBottomSheetBehavior =
            (dialog as BottomSheetDialog).behavior
        modalBottomSheetBehavior.apply {
            isFitToContents = false
            expandedOffset = EXPANDED_OFFSET
            state = DEFAULT_STATE
        }


        staticAnimate(contentView)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                offset = slideOffset
                contentView.scaleX = calculateScaleX(slideOffset)
                contentView.scaleY = calculateScaleY(slideOffset)
                contentView.setCornerRadius(calculateCornerRadius(slideOffset))
            }
        }
        modalBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

    }

    private fun staticAnimate(contentView: View, reverse: Boolean = false) {
        if (DEFAULT_STATE != BottomSheetBehavior.STATE_EXPANDED || offset != 1f)
            return
        val startValue = if (reverse) 1f else 0f
        val endValue = if (reverse) 0f else 1f

        ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = STATIC_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()
            start()
        }.addUpdateListener { animation ->
            val slideOffset = animation.animatedValue as Float
            contentView.scaleX = calculateScaleX(slideOffset)
            contentView.scaleY = calculateScaleY(slideOffset)
            contentView.setCornerRadius(calculateCornerRadius(slideOffset))
        }

    }

    // getter & setters
    fun getMinScaleX() = MIN_SCALE_X
    fun getMinScaleY() = MIN_SCALE_Y
    fun getMaxScaleX() = MAX_SCALE_X
    fun getMaxScaleY() = MAX_SCALE_Y
    fun getDefaultState() = DEFAULT_STATE
    fun getExpandedOffset() = EXPANDED_OFFSET
    fun getDimAmount() = DIM_AMOUNT

    fun getBackgroundColorId(): Int {
        return BACKGROUND_COLOR_ID
    }

    fun setBackgroundColorId(colorId: Int) {
        BACKGROUND_COLOR_ID = colorId
    }

    fun getStatusBarColorId(): Int {
        return STATUS_BAR_COLOR_ID
    }

    fun setStatusBarColorId(colorId: Int) {
        STATUS_BAR_COLOR_ID = colorId
    }

    fun getBackgroundColorHex(): String? {
        return BACKGROUND_COLOR_HEX
    }

    fun shouldChangeStatusBarColor(): Boolean {
        return SHOULD_CHANGE_STATUS_BAR_COLOR
    }

    fun getStatusBarColorHex(): String? {
        return STATUS_BAR_COLOR_HEX
    }

    fun setShouldChangeStatusBarColor(shouldChange: Boolean): IosBottomSheet {
        SHOULD_CHANGE_STATUS_BAR_COLOR = shouldChange
        return this
    }

    fun setBackgroundColorHex(hexColor: String?): IosBottomSheet {
        BACKGROUND_COLOR_HEX = hexColor
        return this
    }


    fun setStatusBarColorHex(hexColor: String?): IosBottomSheet {
        STATUS_BAR_COLOR_HEX = hexColor
        return this
    }

    fun setMinScaleX(minScaleX: Float): IosBottomSheet {
        MIN_SCALE_X = minScaleX
        return this
    }

    fun setMinScaleY(minScaleY: Float): IosBottomSheet {
        MIN_SCALE_Y = minScaleY
        return this
    }

    fun setMaxScaleX(maxScaleX: Float): IosBottomSheet {
        MAX_SCALE_X = maxScaleX
        return this
    }

    fun setMaxScaleY(maxScaleY: Float): IosBottomSheet {
        MAX_SCALE_Y = maxScaleY
        return this
    }

    fun setDefaultState(@BottomSheetBehavior.StableState defaultState: Int): IosBottomSheet {
        DEFAULT_STATE = defaultState
        return this
    }

    fun setExpandedOffset(expandedOffset: Int): IosBottomSheet {
        EXPANDED_OFFSET = expandedOffset
        return this
    }

    fun setDimAmount(dimAmount: Float): IosBottomSheet {
        DIM_AMOUNT = dimAmount
        return this
    }


    private fun calculateScaleX(offset: Float): Float {
        return lerp(MAX_SCALE_X, MIN_SCALE_X, offset.coerceAtLeast(0f))
    }

    private fun calculateScaleY(offset: Float): Float {
        return lerp(MAX_SCALE_Y, MIN_SCALE_Y, offset.coerceAtLeast(0f))
    }

    private fun calculateCornerRadius(slideOffset: Float): Float {
        return lerp(MIN_CORNER_RADIUS, MAX_CORNER_RADIUS, slideOffset.coerceAtLeast(0f))
    }

    private fun View.setCornerRadius(cornerRadius: Float) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.apply {
                    setRoundRect(0, 0, view?.width ?: 0, view?.height ?: 0, cornerRadius)
                }
            }
        }

        if (!clipToOutline)
            clipToOutline = true

    }

    private fun View.setRootBackgroundColor() {

        BACKGROUND_COLOR_HEX?.let {
            setBackgroundColor(Color.parseColor(it))
            return
        }
        setBackgroundColor(BACKGROUND_COLOR_ID)
    }

    private fun Activity.setStatusBarBackgroundColor() {
        if (!SHOULD_CHANGE_STATUS_BAR_COLOR)
            return
        STATUS_BAR_COLOR_HEX?.let {
            window.statusBarColor = Color.parseColor(it)
            return
        }
        window.statusBarColor = STATUS_BAR_COLOR_ID

    }

    private fun Context.getColorFromAttribute(attr: Int): Int {
        val typedArray = obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.TRANSPARENT)
        typedArray.recycle()
        return color
    }

}