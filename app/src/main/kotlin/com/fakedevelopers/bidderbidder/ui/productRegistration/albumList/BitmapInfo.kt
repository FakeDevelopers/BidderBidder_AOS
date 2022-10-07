package com.fakedevelopers.bidderbidder.ui.productRegistration.albumList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 지금은 회전 각도 밖에 없지만 나중엔 이미지 자르기 영역에 대한 정보가 추가 됩니다.
@Parcelize
data class BitmapInfo(
    var degree: Float
) : Parcelable
