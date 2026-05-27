package dev.marzban.admin.feature.users

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserStatus
import dev.marzban.admin.data.repository.UserRepository

class UsersPagingSource(
    private val repository: UserRepository,
    private val search: String?,
    private val status: UserStatus?,
) : PagingSource<Int, UserResponse>() {

    override fun getRefreshKey(state: PagingState<Int, UserResponse>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(PAGE_SIZE) ?: page.nextKey?.minus(PAGE_SIZE)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserResponse> {
        val offset = params.key ?: 0
        val limit = params.loadSize.coerceAtMost(PAGE_SIZE)
        return when (val result = repository.list(offset = offset, limit = limit, search = search, status = status)) {
            is ApiResult.Failure -> LoadResult.Error(result.error)
            is ApiResult.Success -> LoadResult.Page(
                data = result.value.users,
                prevKey = if (offset == 0) null else (offset - limit).coerceAtLeast(0),
                nextKey = if (result.value.users.size < limit) null else offset + limit,
            )
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
