/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sms.moLotus.feature.compose.part

import android.content.Context
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.setVisible
import com.sms.moLotus.common.widget.BubbleImageView
import com.sms.moLotus.extensions.isAudio
import com.sms.moLotus.extensions.isImage
import com.sms.moLotus.extensions.isVideo
import com.sms.moLotus.model.Message
import com.sms.moLotus.model.MmsPart
import com.sms.moLotus.util.GlideApp
import kotlinx.android.synthetic.main.mms_preview_list_item.*
import javax.inject.Inject

class MediaBinder @Inject constructor(colors: Colors, private val context: Context) : PartBinder() {

    override val partLayout = R.layout.mms_preview_list_item
    override var theme = colors.theme()

    override fun canBindPart(part: MmsPart) = part.isImage() || part.isVideo()

    override fun bindPart(
        holder: QkViewHolder,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        holder.video.setVisible(part.isVideo())
        holder.containerView.setOnClickListener { clicks.onNext(part.id) }

        holder.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        GlideApp.with(context).load(part.getUri()).fitCenter().into(holder.thumbnail)
    }

}