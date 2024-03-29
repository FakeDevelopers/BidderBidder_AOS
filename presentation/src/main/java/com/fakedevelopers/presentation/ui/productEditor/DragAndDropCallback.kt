package com.fakedevelopers.presentation.ui.productEditor

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragAndDropCallback(
    private val adapter: SelectedPictureListAdapter
) : ItemTouchHelper.Callback() {

    private var isMoved = false

    // 좌우 드래그
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
        makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemDragMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        if (fromPos == 0) {
            (viewHolder as SelectedPictureListAdapter.ViewHolder).setRepresentImage(false)
            (target as SelectedPictureListAdapter.ViewHolder).setRepresentImage(true)
        } else if (toPos == 0) {
            (viewHolder as SelectedPictureListAdapter.ViewHolder).setRepresentImage(true)
            (target as SelectedPictureListAdapter.ViewHolder).setRepresentImage(false)
        }
        isMoved = true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (isMoved) {
            isMoved = false
            adapter.changeMoveEvent()
        }
    }

    override fun isLongPressDragEnabled() = true

    override fun isItemViewSwipeEnabled() = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val newDX =
            if (
                (viewHolder.absoluteAdapterPosition == 0 && dX < 0) ||
                (viewHolder.absoluteAdapterPosition == recyclerView.adapter!!.itemCount - 1 && dX > 0)
            ) {
                0.0f
            } else {
                dX
            }
        super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 안쓸거야!!
    }
}
