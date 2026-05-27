package dev.marzban.admin.feature.users

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserStatus
import dev.marzban.admin.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UsersFilter(
    val search: String = "",
    val status: UserStatus? = null,
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {
    private val _filter = MutableStateFlow(UsersFilter())
    val filter: StateFlow<UsersFilter> = _filter.asStateFlow()

    val pagingFlow: Flow<PagingData<UserResponse>> = _filter.flatMapLatest { f ->
        Pager(
            config = PagingConfig(
                pageSize = UsersPagingSource.PAGE_SIZE,
                prefetchDistance = UsersPagingSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                UsersPagingSource(
                    repository = repository,
                    search = f.search.ifBlank { null },
                    status = f.status,
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun setSearch(value: String) = _filter.update { it.copy(search = value) }
    fun setStatus(value: UserStatus?) = _filter.update { it.copy(status = value) }
}
