package com.fakedevelopers.bidderbidder.ui.util

import com.fakedevelopers.bidderbidder.ui.util.EventFlow.Companion.DEFAULT_REPLAY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/*  이벤트가 stop 일 때 들어와서 휘발되는 것을 막아야합니다.
    아직 소비되지 않은 이벤트를 캐싱하는 이벤트 플로우 입니다. */
interface EventFlow<out T> : Flow<T> {
    companion object {
        const val DEFAULT_REPLAY = 2
    }
}

interface MutableEventFlow<T> : EventFlow<T>, FlowCollector<T>

private class EventFlowSlot<T>(val value: T) {
    private val consumed = AtomicBoolean(false)
    fun markConsumed() = consumed.getAndSet(true)
}

private class ReadOnlyEventFlow<T>(flow: EventFlow<T>) : EventFlow<T> by flow

private class EventFlowImpl<T>(replay: Int) : MutableEventFlow<T> {

    private val flow: MutableSharedFlow<EventFlowSlot<T>> = MutableSharedFlow(replay)

    override suspend fun collect(collector: FlowCollector<T>) =
        flow.collect { slot ->
            if (!slot.markConsumed()) {
                collector.emit(slot.value)
            }
        }

    override suspend fun emit(value: T) {
        flow.emit(EventFlowSlot(value))
    }
}

@Suppress("FunctionName")
fun <T> MutableEventFlow(replay: Int = DEFAULT_REPLAY): MutableEventFlow<T> = EventFlowImpl(replay)

fun <T> MutableEventFlow<T>.asEventFlow(): EventFlow<T> = ReadOnlyEventFlow(this)
