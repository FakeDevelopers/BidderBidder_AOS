package com.fakedevelopers.bidderbidder.ui.chat.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.STREAM_KEY
import com.fakedevelopers.bidderbidder.databinding.FragmentChannelListBinding
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelListFragment : Fragment() {

    private var _binding: FragmentChannelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CLViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_channel_list,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                viewModel.streamUserTokenEvent.collectLatest {
                    if (it.isSuccessful) {
                        initUser(it.body().toString())
                        viewModel.setToken(it.body().toString())
                    } else {
                        ApiErrorHandler.printErrorMessage(it.errorBody())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
