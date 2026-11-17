package com.lcn.fbfilter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcn.fbfilter.api.FacebookGraphService
import com.lcn.fbfilter.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FbFilterUiState(
    val currentScreen: AppScreen = AppScreen.LOGIN,
    val session: FbUserSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCriteria: Set<FilterCriterion> = setOf(FilterCriterion.NO_INTERACTION),
    val filteredFriends: List<FriendAccount> = emptyList(),
    val currentPage: Int = 1,
    val maxPerPage: Int = FacebookGraphService.MAX_FRIENDS_PER_PAGE,
    val executionProgress: ExecutionProgress? = null,
    val lastSuccessNotice: String? = null
)

class FbFilterViewModel(
    private val graphService: FacebookGraphService = FacebookGraphService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FbFilterUiState())
    val uiState: StateFlow<FbFilterUiState> = _uiState.asStateFlow()

    /**
     * Màn hình 1: Bắt buộc đăng nhập tài khoản Facebook trước khi mở khóa tính năng
     */
    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = graphService.authenticateWithGraphApi(accessToken)
            result.onSuccess { userSession ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        session = userSession,
                        currentScreen = AppScreen.FILTER_SELECT,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Đăng nhập thất bại. Vui lòng kiểm tra lại kết nối."
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.value = FbFilterUiState(currentScreen = AppScreen.LOGIN)
    }

    /**
     * Màn hình 2: Chọn tiêu chí lọc (Không tương tác, Bỏ theo dõi)
     */
    fun toggleCriterion(criterion: FilterCriterion) {
        _uiState.update { state ->
            val updated = state.selectedCriteria.toMutableSet()
            if (criterion in updated) {
                if (updated.size > 1) updated.remove(criterion)
            } else {
                updated.add(criterion)
            }
            state.copy(selectedCriteria = updated, lastSuccessNotice = null)
        }
    }

    /**
     * Nhấn nút "Quét" để chuyển sang màn hình Danh sách bạn bè (Giới hạn 5000 tài khoản/trang)
     */
    fun startScanFriends() {
        val currentSession = _uiState.value.session ?: return
        val criteria = _uiState.value.selectedCriteria
        if (criteria.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ít nhất 1 tiêu chí lọc!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, lastSuccessNotice = null) }
            val result = graphService.scanFilteredFriends(currentSession, criteria)
            result.onSuccess { friends ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        filteredFriends = friends.take(FacebookGraphService.MAX_FRIENDS_PER_PAGE),
                        currentScreen = AppScreen.FRIEND_LIST
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Lỗi khi quét danh sách bạn bè."
                    )
                }
            }
        }
    }

    /**
     * Màn hình 3: Chọn 1 hoặc nhiều tài khoản (hoặc Chọn tất cả)
     */
    fun toggleFriendSelection(friendId: String) {
        _uiState.update { state ->
            val updatedList = state.filteredFriends.map { friend ->
                if (friend.id == friendId) friend.copy(isSelected = !friend.isSelected) else friend
            }
            state.copy(filteredFriends = updatedList)
        }
    }

    fun toggleSelectAll() {
        _uiState.update { state ->
            val allSelected = state.filteredFriends.isNotEmpty() && state.filteredFriends.all { it.isSelected }
            val updatedList = state.filteredFriends.map { it.copy(isSelected = !allSelected) }
            state.copy(filteredFriends = updatedList)
        }
    }

    /**
     * Thực thi hàng loạt: Bỏ theo dõi, Huỷ kết bạn, hoặc Chặn
     * Hiển thị tiến trình thực thi -> Khi hoàn tất thì báo thành công -> Quay về màn hình Lựa chọn
     */
    fun executeBatchAction(actionType: ActionType) {
        val currentSession = _uiState.value.session ?: return
        val selectedAccounts = _uiState.value.filteredFriends.filter { it.isSelected }
        if (selectedAccounts.isEmpty()) return

        viewModelScope.launch {
            var successCount = 0
            var failedCount = 0
            val total = selectedAccounts.size

            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.EXECUTING,
                    executionProgress = ExecutionProgress(
                        actionType = actionType,
                        totalSelected = total,
                        completedCount = 0,
                        currentFriendName = selectedAccounts.first().name,
                        successCount = 0,
                        failedCount = 0,
                        isFinished = false
                    )
                )
            }

            selectedAccounts.forEachIndexed { index, friend ->
                _uiState.update { state ->
                    state.copy(
                        executionProgress = state.executionProgress?.copy(
                            currentFriendName = friend.name,
                            completedCount = index
                        )
                    )
                }

                val ok = graphService.executeFriendAction(currentSession, friend, actionType)
                if (ok) successCount++ else failedCount++

                _uiState.update { state ->
                    state.copy(
                        executionProgress = state.executionProgress?.copy(
                            completedCount = index + 1,
                            successCount = successCount,
                            failedCount = failedCount
                        )
                    )
                }
            }

            // Đánh dấu hoàn tất để hiện Dialog Báo thành công
            _uiState.update { state ->
                state.copy(
                    executionProgress = state.executionProgress?.copy(isFinished = true)
                )
            }
        }
    }

    /**
     * Xác nhận thông báo thành công và quay về màn hình Lựa chọn (Màn hình 2)
     */
    fun confirmExecutionSuccessAndReturnToFilter() {
        val progress = _uiState.value.executionProgress
        val summaryMsg = if (progress != null) {
            "Đã ${progress.actionType.pastTenseLabel.lowercase()} thành công ${progress.successCount}/${progress.totalSelected} tài khoản!"
        } else {
            "Thực thi hoàn tất!"
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.FILTER_SELECT,
                executionProgress = null,
                filteredFriends = emptyList(),
                lastSuccessNotice = summaryMsg
            )
        }
    }

    fun backToFilterScreen() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.FILTER_SELECT,
                errorMessage = null
            )
        }
    }
}
