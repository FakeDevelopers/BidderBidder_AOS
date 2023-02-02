package com.fakedevelopers.presentation.ui.productSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.usecase.GetProductSearchRankUseCase
import com.fakedevelopers.domain.usecase.GetSearchHistoryUseCase
import com.fakedevelopers.domain.usecase.SetSearchHistoryUseCase
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.logging.helper.stringify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ProductSearchViewModel @Inject constructor(
    private val getProductSearchRankUseCase: GetProductSearchRankUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val setSearchHistoryUseCase: SetSearchHistoryUseCase
) : ViewModel() {

    private val _searchWord = MutableEventFlow<String>()
    val searchWord = _searchWord.asEventFlow()

    // api가 있어야 사용가능
    private val resultList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private var prevSearchBar = ""

    val searchHistoryAdapter = SearchHistoryAdapter(
        eraseHistory = { eraseHistoryEvent(it) }
    ) { searchEvent(it) }
    val searchPopularAdapter = SearchPopularAdapter { searchEvent(it) }
    val searchResultAdapter = SearchResultAdapter { searchEvent(it) }
    val searchBar = MutableStateFlow("")

    init {
        requestSearchRank()
        viewModelScope.launch {
            searchHistoryAdapter.submitList(getSearchHistoryUseCase())
        }
    }

    fun searchEvent(word: String) {
        val list = mutableListOf<String>()
        list.addAll(searchHistoryAdapter.currentList)
        viewModelScope.launch {
            // 중복 단어 선택시 set이 순서 변경을 인식 못함
            // 그래서 set을 비운 다음 다시 채워줌
            if (list.contains(word)) {
                list.remove(word)
                setSearchHistoryUseCase(emptyList())
            }
            list.add(0, word)
            setSearchHistoryUseCase(list)
            // 작업이 다 끝나면 검색을 수행
            _searchWord.emit(word)
        }
    }

    fun setSearchBar(word: String) {
        viewModelScope.launch {
            searchBar.emit(word)
        }
    }

    fun requestSearchResult() {
        // prevSearchBar = searchBar 이면 검색 요청을 하지 않음
        // 화면을 나갔다 들어왔을 때 재요청 방지
        if (prevSearchBar == searchBar.value) {
            return
        }

        // 여기서 요청을 하든 해야겠죠
        searchResultAdapter.run {
            setSearchWord(searchBar.value)
            submitList(
                listOf(
                    searchBar.value,
                    "${searchBar.value}${Random.nextInt(100)}",
                    "${searchBar.value}${Random.nextInt(100)}",
                    "${searchBar.value}${Random.nextInt(100)}",
                    "${searchBar.value}${Random.nextInt(100)}",
                    "${searchBar.value}${Random.nextInt(100)}"
                )
            )
        }
        prevSearchBar = searchBar.value
    }

    private fun requestSearchRank() {
        viewModelScope.launch {
            val result = getProductSearchRankUseCase(LIST_COUNT)
            if (result.isSuccess) {
                searchPopularAdapter.submitList(result.getOrDefault(listOf("")))
            } else {
                ApiErrorHandler.printMessage(result.exceptionOrNull()?.stringify().toString())
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryAdapter.submitList(emptyList())
            setSearchHistoryUseCase(emptyList())
        }
    }

    fun clearResult() {
        prevSearchBar = ""
        viewModelScope.launch {
            searchResultAdapter.submitList(emptyList())
        }
    }

    private fun eraseHistoryEvent(word: String) {
        val list = searchHistoryAdapter.currentList.filter { it != word }
        searchHistoryAdapter.submitList(list)
        viewModelScope.launch {
            setSearchHistoryUseCase(list)
        }
    }

    companion object {
        private const val LIST_COUNT = 10
    }
}
