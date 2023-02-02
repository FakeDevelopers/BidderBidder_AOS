package com.fakedevelopers.presentation.ui.chat.channel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.fakedevelopers.domain.secret.Constants.Companion.STREAM_KEY
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentChannelListBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.logging.helper.stringify
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelListFragment : BaseFragment<FragmentChannelListBinding>(
    R.layout.fragment_channel_list
) {

    private val viewModel: CLViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        initCollector()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.token.isNotEmpty()) {
            initUser(viewModel.token)
        }
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.streamUserTokenEvent.collectLatest { result ->
                    if (result.isSuccess) {
                        val token = result.getOrDefault("")
                        initUser(token)
                        viewModel.setToken(token)
                    } else {
                        ApiErrorHandler.printMessage(result.exceptionOrNull()?.stringify().toString())
                    }
                }
            }
        }
    }

    private fun initListener() {
        binding.channelListChat.setChannelItemClickListener { channel ->
            findNavController().navigate(
                ChannelListFragmentDirections.actionChannelListFragmentToChattingFragment(channel.cid)
            )
        }
    }

    private fun initUser(token: String) {
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = io.getstream.chat.android.offline.plugin.configuration.Config(),
            appContext = requireActivity().applicationContext
        )
        val client = ChatClient.Builder(STREAM_KEY, requireActivity().applicationContext)
            .withPlugin(offlinePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
        val user = User(
            id = viewModel.streamUserId.value,
            name = "Ryan Kim${viewModel.streamUserId.value}",
            image = "https://ibb.co/7yvHkpX"
        )
        client.connectUser(
            user = user,
            token = token
        ).enqueue {
            if (it.isSuccess) {
                // createChannel(client, user.id)
            } else {
                ApiErrorHandler.printMessage(it.error().message)
            }
        }
        val channelListViewModel: ChannelListViewModel by viewModels { ChannelListViewModelFactory() }
        channelListViewModel.bindView(binding.channelListChat, this)
    }

    private fun createChannel(client: ChatClient, id: String) {
        client.createChannel(
            channelType = "messaging",
            channelId = "channel_2708",
            memberIds = listOf(id, "2"),
            extraData = mapOf(
                "name" to "카카오 종목토론방",
                "image" to "https://ibb.co/7yvHkpX"
            )
        ).enqueue() {
            if (it.isSuccess) {
                val channel = it.data()
                collectChannelState(client, channel.cid)
            } else {
                ApiErrorHandler.printMessage(it.error().message)
            }
        }
    }

    private fun collectChannelState(client: ChatClient, cid: String) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                client.watchChannelAsState(cid = cid, messageLimit = 0).collectLatest {
                    // 여기는 특정 채널 상태가 변경되면 호출 됩니다.
                    // 상태 : channelState.messages channelState.reads channelState.typing
                }
            }
        }
    }
}
