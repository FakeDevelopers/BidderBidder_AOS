package com.fakedevelopers.bidderbidder.ui.product_search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProductSearchViewModel(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _searchWord = MutableSharedFlow<String>()
    private val _historySet = MutableSharedFlow<Set<String>>()

    // api가 있어야 사용가능
    private val resultList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private val popularList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private var prevSearchBar = ""

    val searchHistoryAdapter = SearchHistoryAdapter(
        eraseHistory = { eraseHistoryEvent(it) }
    ) { searchEvent(it) }
    val searchPopularAdapter = SearchPopularAdapter { searchEvent(it) }.apply {
        submitList(listOf("test", "테스트", "아반떼", "야옹이", "인조인간"))
    }
    val searchResultAdapter = SearchResultAdapter { searchEvent(it) }
    val searchBar = MutableStateFlow("")
    val searchWord: SharedFlow<String> get() = _searchWord
    val historySet: SharedFlow<Set<String>> get() = _historySet

    fun searchEvent(word: String) {
        val list = mutableListOf<String>()
        list.addAll(searchHistoryAdapter.currentList)
        viewModelScope.launch(defaultDispatcher) {
            // 중복 단어 선택시 set이 순서 변경을 인식 못함
            // 그래서 set을 비운 다음 다시 채워줌
            if (list.contains(word)) {
                list.remove(word)
                _historySet.emit(emptySet())
            }
            list.add(0, word)
            _historySet.emit(list.toSet())
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
        searchResultAdapter.submitList(
            listOf(
                searchBar.value,
                "${searchBar.value}${Random.nextInt(100)}",
                "${searchBar.value}${Random.nextInt(100)}",
                "${searchBar.value}${Random.nextInt(100)}",
                "${searchBar.value}${Random.nextInt(100)}",
                "${searchBar.value}${Random.nextInt(100)}"
            )
        )
        prevSearchBar = searchBar.value
    }

    fun setHistoryList(list: List<String>) {
        searchHistoryAdapter.submitList(list.toMutableList())
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryAdapter.submitList(emptyList())
            _historySet.emit(emptySet())
        }
    }

    fun clearResult() {
        prevSearchBar = ""
        viewModelScope.launch {
            searchResultAdapter.submitList(emptyList())
        }
    }

    private fun eraseHistoryEvent(word: String) {
        viewModelScope.launch {
            val list = mutableListOf<String>().apply {
                addAll(searchHistoryAdapter.currentList)
                remove(word)
            }
            searchHistoryAdapter.submitList(list)
            _historySet.emit(list.toSet())
        }
    }
}
