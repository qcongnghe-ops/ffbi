package com.lcn.fbfilter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lcn.fbfilter.model.*
import com.lcn.fbfilter.viewmodel.FbFilterUiState
import com.lcn.fbfilter.viewmodel.FbFilterViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FbFilterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1877F2),
                    onPrimary = Color.White,
                    secondary = Color(0xFF00A400),
                    surface = Color(0xFFFFFFFF),
                    background = Color(0xFFF0F2F5)
                )
            ) {
                val state by viewModel.uiState.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (state.currentScreen) {
                        AppScreen.LOGIN -> LoginScreen(
                            state = state,
                            onLogin = { token -> viewModel.loginWithFacebook(token) }
                        )
                        AppScreen.FILTER_SELECT -> FilterSelectScreen(
                            state = state,
                            onToggleCriterion = { viewModel.toggleCriterion(it) },
                            onScan = { viewModel.startScanFriends() },
                            onLogout = { viewModel.logout() }
                        )
                        AppScreen.FRIEND_LIST -> FriendListScreen(
                            state = state,
                            onToggleFriend = { viewModel.toggleFriendSelection(it) },
                            onToggleSelectAll = { viewModel.toggleSelectAll() },
                            onExecuteAction = { viewModel.executeBatchAction(it) },
                            onBack = { viewModel.backToFilterScreen() }
                        )
                        AppScreen.EXECUTING -> ExecutionProgressScreen(
                            state = state,
                            onConfirmComplete = { viewModel.confirmExecutionSuccessAndReturnToFilter() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    state: FbFilterUiState,
    onLogin: (String) -> Unit
) {
    var accessToken by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color(0xFF1877F2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "f",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lọc Tương Tác Facebook",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1E21)
        )

        Text(
            text = "Bắt buộc đăng nhập tài khoản Facebook qua Graph API để tiếp tục",
            fontSize = 14.sp,
            color = Color(0xFF65676B),
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = { Text("Facebook Access Token (EAAG... / EAAAAU...)") },
                    placeholder = { Text("Nhập token hoặc bấm Đăng nhập OAuth...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = Color(0xFFD93025),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val tokenToUse = accessToken.ifBlank { "DEMO_OAUTH_FB_TOKEN" }
                        onLogin(tokenToUse)
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Đang xác thực Facebook API...")
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng nhập bằng Facebook", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSelectScreen(
    state: FbFilterUiState,
    onToggleCriterion: (FilterCriterion) -> Unit,
    onScan: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Thanh thông tin tài khoản đã đăng nhập
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1877F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.session?.fullName?.take(1) ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.session?.fullName ?: "Tài khoản Facebook",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "UID: ${state.session?.userId ?: "---"} • Đã kết nối Graph API",
                        fontSize = 12.sp,
                        color = Color(0xFF65676B)
                    )
                }
                TextButton(onClick = onLogout) {
                    Text("Đăng xuất", color = Color(0xFFD93025))
                }
            }
        }

        // Thông báo hoàn tất từ lần thực thi trước (nếu có)
        if (state.lastSuccessNotice != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF137333)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.lastSuccessNotice,
                        color = Color(0xFF137333),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Lựa chọn bộ lọc bạn bè",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1E21)
        )
        Text(
            text = "Chọn tiêu chí cần lọc trước khi nhấn Quét (Tối đa 5.000 bạn bè/trang)",
            fontSize = 13.sp,
            color = Color(0xFF65676B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        FilterCriterion.values().forEach { criterion ->
            val isChecked = criterion in state.selectedCriteria
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = if (isChecked) 2.dp else 1.dp,
                        color = if (isChecked) Color(0xFF1877F2) else Color(0xFFDADDE1),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggleCriterion(criterion) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) Color(0xFFE7F3FF) else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleCriterion(criterion) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = criterion.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1E21)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = criterion.description,
                            fontSize = 13.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onScan,
            enabled = !state.isLoading && state.selectedCriteria.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đang quét dữ liệu Facebook API...")
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUÉT DANH SÁCH BẠN BÈ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FriendListScreen(
    state: FbFilterUiState,
    onToggleFriend: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onExecuteAction: (ActionType) -> Unit,
    onBack: () -> Unit
) {
    val totalFriends = state.filteredFriends.size
    val selectedCount = state.filteredFriends.count { it.isSelected }
    val allSelected = totalFriends > 0 && selectedCount == totalFriends

    Column(modifier = Modifier.fillMaxSize()) {
        // Thanh tiêu đề & nút Chọn tất cả
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kết quả lọc ($totalFriends / 5000)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Trang ${state.currentPage} • Giới hạn 5.000 tài khoản/trang",
                            fontSize = 12.sp,
                            color = Color(0xFF65676B)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSelectAll() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { onToggleSelectAll() }
                        )
                        Text(
                            text = "Chọn tất cả",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Danh sách bạn bè: Mỗi bạn bè 1 dòng gồm Avatar và Tên bạn bè
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredFriends, key = { it.id }) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleFriend(friend.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (friend.isSelected) Color(0xFFE7F3FF) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = friend.isSelected,
                            onCheckedChange = { onToggleFriend(friend.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Avatar bạn bè
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1877F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.name.split(" ").lastOrNull()?.take(1) ?: "F",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // Tên bạn bè trên 1 dòng
                        Text(
                            text = friend.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Thanh 3 nút chức năng: Bỏ theo dõi, Huỷ kết bạn, Chặn
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Đã chọn: $selectedCount / $totalFriends tài khoản",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF65676B),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nút 1: Bỏ theo dõi
                    Button(
                        onClick = { onExecuteAction(ActionType.UNFOLLOW) },
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Bỏ theo dõi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Nút 2: Huỷ kết bạn
                    Button(
                        onClick = { onExecuteAction(ActionType.UNFRIEND) },
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Huỷ kết bạn", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Nút 3: Chặn
                    Button(
                        onClick = { onExecuteAction(ActionType.BLOCK) },
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Chặn", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExecutionProgressScreen(
    state: FbFilterUiState,
    onConfirmComplete: () -> Unit
) {
    val progress = state.executionProgress ?: return
    val ratio = if (progress.totalSelected > 0) {
        progress.completedCount.toFloat() / progress.totalSelected.toFloat()
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!progress.isFinished) {
                    CircularProgressIndicator(
                        progress = ratio,
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 6.dp,
                        color = Color(0xFF1877F2)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Đang thực thi: ${progress.actionType.label}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đang xử lý: ${progress.currentFriendName}",
                        fontSize = 14.sp,
                        color = Color(0xFF65676B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = ratio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${progress.completedCount} / ${progress.totalSelected} tài khoản (${(ratio * 100).toInt()}%)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Thực thi hoàn tất!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đã ${progress.actionType.pastTenseLabel.lowercase()} thành công ${progress.successCount}/${progress.totalSelected} tài khoản bạn bè.",
                        fontSize = 15.sp,
                        color = Color(0xFF1C1E21)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onConfirmComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text(
                            text = "Quay về màn hình Lựa chọn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
