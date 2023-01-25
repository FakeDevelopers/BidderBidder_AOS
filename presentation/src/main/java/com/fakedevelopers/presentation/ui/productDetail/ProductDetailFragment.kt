package com.fakedevelopers.presentation.ui.productDetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentProductDetailBinding
import com.fakedevelopers.presentation.model.RemainTime
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.DATE_PATTERN
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

@AndroidEntryPoint
class ProductDetailFragment : BaseFragment<FragmentProductDetailBinding>(
    R.layout.fragment_product_detail
) {

    private val viewModel: ProductDetailViewModel by viewModels()

    private val productDetailAdapter by lazy { ProductDetailAdapter() }

    private val onPageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            setPagerCount(position)
        }
    }

    private val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        val args: ProductDetailFragmentArgs by navArgs()
        if (args.productId != -1L) {
            viewModel.productDetailRequest(args.productId)
        }
        binding.viewpagerProductDetailPictures.run {
            adapter = productDetailAdapter
            registerOnPageChangeCallback(onPageChanged)
        }
        initCollector()
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.eventFlow.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun setPagerCount(position: Int, totalCount: Int = productDetailAdapter.itemCount) {
        Logger.i("$position $totalCount")
        binding.textviewProductDetailPictureCount.text =
            getString(R.string.product_detail_picture_count, position + 1, totalCount)
    }

    private fun handleEvent(event: ProductDetailViewModel.Event) {
        when (event) {
            is ProductDetailViewModel.Event.CreatedDate -> handleCreateTime(event.date)
            is ProductDetailViewModel.Event.Expired -> handleExpired(event.state)
            is ProductDetailViewModel.Event.Timer -> handleRemainTime(event.remainTime)
            is ProductDetailViewModel.Event.ProductImages -> handleProductImages(event.images)
        }
    }

    private fun handleCreateTime(date: String) {
        val createdDate = LocalDateTime.parse(date, formatter)
        val now = LocalDateTime.now()
        val dateDiff = Period.between(createdDate.toLocalDate(), now.toLocalDate())
        val createdDiff = when {
            dateDiff.years > 0 -> getString(R.string.product_detail_before_years, dateDiff.years)
            dateDiff.months > 0 -> getString(R.string.product_detail_before_months, dateDiff.months)
            dateDiff.days > 1 -> getString(R.string.product_detail_before_days, dateDiff.days)
            else -> getHourDiff(createdDate, now)
        }
        binding.textviewProductDetailCategoryAndTime.text =
            getString(R.string.product_detail_category_and_time, "음반", createdDiff)
    }

    private fun getHourDiff(start: LocalDateTime, end: LocalDateTime): String {
        val hourDiff = (end.toEpochSecond(ZoneOffset.MIN) - start.toEpochSecond(ZoneOffset.MIN)) / 3600
        return when {
            hourDiff >= 24 -> getString(R.string.product_detail_before_days, hourDiff / 24)
            hourDiff > 0 -> getString(R.string.product_detail_before_hours, hourDiff)
            else -> getString(R.string.product_detail_before_now)
        }
    }

    private fun handleExpired(state: Boolean) {
        binding.textviewProductListRemainTimeDividerStart.isVisible = state.not()
        binding.textviewProductListRemainTimeDividerEnd.isVisible = state.not()
        binding.textviewProductListRemainTimeStart.isVisible = state.not()
        binding.textviewProductListRemainTimeMiddle.isVisible = state.not()
        binding.textviewProductListRemainTimeEnd.isVisible = state.not()
        binding.textViewProductDetailExpired.isVisible = state
        binding.buttonProductDetailBidding.isEnabled = state
        binding.buttonProductDetailBuy.isEnabled = state
    }

    private fun handleRemainTime(remainTime: RemainTime) {
        if (remainTime.day > 0) {
            binding.textviewProductListRemainTimeStart.text = getString(R.string.time_days, remainTime.day)
            binding.textviewProductListRemainTimeMiddle.text = getString(R.string.time_hours, remainTime.hour)
            binding.textviewProductListRemainTimeEnd.text = getString(R.string.time_minutes, remainTime.minute)
        } else {
            binding.textviewProductListRemainTimeStart.run {
                text = getString(R.string.time_hours, remainTime.hour)
                isVisible = remainTime.hour > 0
            }
            binding.textviewProductListRemainTimeMiddle.run {
                text = getString(R.string.time_minutes, remainTime.minute)
                isVisible = remainTime.hour > 0 || remainTime.minute > 0
            }
            binding.textviewProductListRemainTimeDividerStart.isVisible = remainTime.hour > 0
            binding.textviewProductListRemainTimeDividerEnd.isVisible = remainTime.hour > 0 || remainTime.minute > 0
            binding.textviewProductListRemainTimeEnd.text = getString(R.string.time_seconds, remainTime.second)
        }
        if (binding.textviewProductListRemainTimeStart.isVisible) {
            setPartialText(binding.textviewProductListRemainTimeStart)
        }
        if (binding.textviewProductListRemainTimeMiddle.isVisible) {
            setPartialText(binding.textviewProductListRemainTimeMiddle)
        }
        setPartialText(binding.textviewProductListRemainTimeEnd)
    }

    private fun handleProductImages(images: List<String>) {
        productDetailAdapter.submitList(images)
        setPagerCount(binding.viewpagerProductDetailPictures.currentItem, images.size)
    }

    private fun setPartialText(textView: TextView) {
        val lastPriceIndex = textView.text.indexOfLast { c -> c in '0'..'9' } + 1
        textView.text = SpannableStringBuilder(textView.text).apply {
            setSpan(
                RelativeSizeSpan(REMAIN_TIME_RELATIVE_SIZE),
                lastPriceIndex,
                textView.text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                lastPriceIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onDestroyView() {
        binding.viewpagerProductDetailPictures.unregisterOnPageChangeCallback(onPageChanged)
        super.onDestroyView()
    }

    companion object {
        private const val REMAIN_TIME_RELATIVE_SIZE = 0.75f
    }
}
