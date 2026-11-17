package com.lcn.fbfilter.model

enum class AppScreen {
    LOGIN,          // Màn hình 1: Bắt buộc đăng nhập Facebook
    FILTER_SELECT,  // Màn hình 2: Lựa chọn tiêu chí (Không tương tác, Bỏ theo dõi) + Nút Quét
    FRIEND_LIST,    // Màn hình 3: Danh sách bạn bè đã lọc (Tối đa 5000/trang, 1 dòng Avatar + Tên, Chọn tất cả, 3 nút hành động)
    EXECUTING       // Màn hình 4: Tiến trình thực thi & thông báo hoàn tất
}

enum class FilterCriterion(val title: String, val description: String) {
    NO_INTERACTION(
        title = "Không tương tác",
        description = "Lọc danh sách bạn bè có 0 lượt Like, Reaction, Comment hoặc Share trên bài viết của bạn trong 90 ngày qua"
    ),
    UNFOLLOWED(
        title = "Bỏ theo dõi",
        description = "Lọc danh sách bạn bè đang ở trạng thái Bỏ theo dõi (Unfollowed) hoặc không hiển thị bảng tin hai chiều"
    )
}

enum class ActionType(val label: String, val pastTenseLabel: String) {
    UNFOLLOW("Bỏ theo dõi", "Đặt chế độ bỏ theo dõi"),
    UNFRIEND("Huỷ kết bạn", "Huỷ kết bạn"),
    BLOCK("Chặn", "Chặn tài khoản")
}

data class FbUserSession(
    val userId: String,
    val fullName: String,
    val avatarUrl: String,
    val accessToken: String,
    val fbDtsg: String = ""
)

data class FriendAccount(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val reactionsCount: Int = 0,
    val commentsCount: Int = 0,
    val isUnfollowed: Boolean = false,
    val mutualFriends: Int = 0,
    val isSelected: Boolean = false
)

data class ExecutionProgress(
    val actionType: ActionType,
    val totalSelected: Int,
    val completedCount: Int,
    val currentFriendName: String,
    val successCount: Int,
    val failedCount: Int,
    val isFinished: Boolean = false
)
