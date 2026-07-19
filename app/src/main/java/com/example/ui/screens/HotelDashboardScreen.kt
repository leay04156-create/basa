package com.example.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotelViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Enumeration for top-level pages
enum class HotelScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("لوحة التحكم", Icons.Outlined.Dashboard),
    ROOMS("إدارة الغرف", Icons.Outlined.MeetingRoom),
    RESERVATIONS("مكتب الحجوزات", Icons.Outlined.DateRange),
    GUESTS("سجل النزلاء", Icons.Outlined.People),
    INVOICES("الفواتير والمدفوعات", Icons.Outlined.ReceiptLong),
    EMPLOYEES("المناوبات والموظفين", Icons.Outlined.Badge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDashboardScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(HotelScreen.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    // UI state from database
    val roomsState by viewModel.rooms.collectAsState()
    val reservationsState by viewModel.reservations.collectAsState()
    val guestsState by viewModel.guests.collectAsState()
    val invoicesState by viewModel.invoices.collectAsState()
    val employeesState by viewModel.employees.collectAsState()
    
    // Responsive config
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 720

    // Enforce RTL Layout Direction for authentic Arabic experience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (isTablet) {
            // Wide Screen Layout: Permanent elegant Sidebar on the right, content pane on the left
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(LuxuryCreamBg)
            ) {
                // Persistent Luxury Sidebar
                SidebarContent(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                )

                // Divider line with gold touch
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(LuxuryGold.copy(alpha = 0.3f))
                )

                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TopHeaderBar(
                        title = currentScreen.title,
                        showMenuButton = false,
                        onMenuClick = {}
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { screen ->
                            MainContentSwitch(
                                screen = screen,
                                viewModel = viewModel,
                                rooms = roomsState,
                                reservations = reservationsState,
                                guests = guestsState,
                                invoices = invoicesState,
                                employees = employeesState
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile Screen Layout: Slide-out Drawer for navigation
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = LuxuryDarkBlue,
                        modifier = Modifier.width(280.dp)
                    ) {
                        SidebarContent(
                            currentScreen = currentScreen,
                            onScreenSelected = {
                                currentScreen = it
                                // Close drawer
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                gesturesEnabled = true
            ) {
                Scaffold(
                    topBar = {
                        TopHeaderBar(
                            title = currentScreen.title,
                            showMenuButton = true,
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        )
                    },
                    containerColor = LuxuryCreamBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition_mobile"
                        ) { screen ->
                            MainContentSwitch(
                                screen = screen,
                                viewModel = viewModel,
                                rooms = roomsState,
                                reservations = reservationsState,
                                guests = guestsState,
                                invoices = invoicesState,
                                employees = employeesState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderBar(
    title: String,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(LuxuryWhite)
            .drawBehind {
                drawLine(
                    color = LuxuryGold.copy(alpha = 0.2f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMenuButton) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "القائمة",
                    tint = LuxuryDarkBlue
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = LuxuryTextDark
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Luxury Hotel branding indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, LuxuryGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatusVacant)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "سيرفر نشط • قصر أوبال الفاخر",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = LuxuryTextDark,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun SidebarContent(
    currentScreen: HotelScreen,
    onScreenSelected: (HotelScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(LuxuryDarkBlue)
            .padding(vertical = 24.dp)
    ) {
        // Luxury Logo and Hotel Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Gold Crown Logo Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(1.5.dp, LuxuryGold, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco, // Elegant organic emblem
                    contentDescription = "شعار الفندق",
                    tint = LuxuryGold,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = "قصر أوبال الفاخر",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "OPAL LUXURY PALACE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LuxuryGoldLight.copy(alpha = 0.5f),
                    letterSpacing = 1.sp,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LuxuryGold.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HotelScreen.entries.forEach { screen ->
                val isSelected = currentScreen == screen
                val bgBrush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(LuxuryDarkBlueAlt, LuxuryDarkBlue)
                    )
                } else {
                    null
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (bgBrush != null) Modifier.background(bgBrush) else Modifier
                        )
                        .clickable { onScreenSelected(screen) }
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) LuxuryGold.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) LuxuryGold else LuxuryGoldLight.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) LuxuryGold else LuxuryWhite.copy(alpha = 0.8f),
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        // Footer Brand Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "الإصدار الفاخر v1.0",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LuxuryGoldLight.copy(alpha = 0.3f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun MainContentSwitch(
    screen: HotelScreen,
    viewModel: HotelViewModel,
    rooms: List<RoomEntity>,
    reservations: List<ReservationEntity>,
    guests: List<GuestEntity>,
    invoices: List<InvoiceEntity>,
    employees: List<EmployeeEntity>
) {
    when (screen) {
        HotelScreen.DASHBOARD -> DashboardSubScreen(rooms, reservations, invoices, employees)
        HotelScreen.ROOMS -> RoomsSubScreen(rooms, viewModel)
        HotelScreen.RESERVATIONS -> ReservationsSubScreen(reservations, rooms, viewModel)
        HotelScreen.GUESTS -> GuestsSubScreen(guests, reservations, viewModel)
        HotelScreen.INVOICES -> InvoicesSubScreen(invoices, viewModel)
        HotelScreen.EMPLOYEES -> EmployeesSubScreen(employees, viewModel)
    }
}

// FORMAT HELPER
fun formatMoney(amount: Double): String {
    val formatter = DecimalFormat("#,###.##")
    return "${formatter.format(amount)} ر.س"
}

// ==========================================
// 1. DASHBOARD SUB SCREEN
// ==========================================
@Composable
fun DashboardSubScreen(
    rooms: List<RoomEntity>,
    reservations: List<ReservationEntity>,
    invoices: List<InvoiceEntity>,
    employees: List<EmployeeEntity>
) {
    // Computations
    val totalRooms = rooms.size
    val bookedRooms = rooms.count { it.status == "محجوزة" }
    val vacantRooms = rooms.count { it.status == "شاغرة" }
    val cleaningRooms = rooms.count { it.status == "تنظيف" }
    val maintenanceRooms = rooms.count { it.status == "صيانة" }
    
    val occupancyPercent = if (totalRooms > 0) (bookedRooms.toFloat() / totalRooms * 100).toInt() else 0
    val activeReservationsCount = reservations.count { it.status == "مؤكد" }
    
    val totalRevenue = invoices.filter { it.status == "مدفوعة" }.sumOf { it.amount }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Premium Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(LuxuryDarkBlue, LuxuryDarkBlueAlt)
                        )
                    )
                    .border(1.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "أهلاً بك بك في بوابة الإدارة الفاخرة",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = LuxuryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "مرحباً بك في لوحة تحكم قصر أوبال اليوم. كل المؤشرات ممتازة ومعدلات الرضا للنزلاء في أعلى مستوياتها. يمكنك مراقبة الغرف والتحقق من الفواتير والعمليات التشغيلية كاملة.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = LuxuryWhite.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(LuxuryGold.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, LuxuryGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WorkspacePremium,
                            contentDescription = "نخبة",
                            tint = LuxuryGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Quick Stats Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Occupancy Rate
                StatCard(
                    title = "نسبة الإشغال",
                    value = "$occupancyPercent%",
                    subText = "$bookedRooms غرف من أصل $totalRooms",
                    icon = Icons.Outlined.Percent,
                    color = LuxuryGold,
                    modifier = Modifier.weight(1f)
                )

                // Today Bookings
                StatCard(
                    title = "الحجوزات النشطة",
                    value = activeReservationsCount.toString(),
                    subText = "نزلاء مسجلين حالياً",
                    icon = Icons.Outlined.BookOnline,
                    color = StatusVacant,
                    modifier = Modifier.weight(1f)
                )

                // Revenue Today
                StatCard(
                    title = "إجمالي الإيرادات",
                    value = formatMoney(totalRevenue),
                    subText = "المدفوعات المحصلة",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    color = LuxuryGold,
                    modifier = Modifier.weight(1f)
                )

                // Rooms Overview
                StatCard(
                    title = "الغرف والجاهزية",
                    value = vacantRooms.toString(),
                    subText = "$cleaningRooms تنظيف • $maintenanceRooms صيانة",
                    icon = Icons.Outlined.MeetingRoom,
                    color = StatusCleaning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Two Column Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Occupancy Breakdown Chart Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(340.dp)
                        .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "تحليل حالة الغرف حالياً",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuxuryTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Beautiful linear bar chart with details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatusRowItem(title = "شاغرة ومتاحة للدخول", count = vacantRooms, total = totalRooms, color = StatusVacant)
                            StatusRowItem(title = "محجوزة ومسكونة حالياً", count = bookedRooms, total = totalRooms, color = StatusBooked)
                            StatusRowItem(title = "قيد خدمات التنظيف والتعقيم", count = cleaningRooms, total = totalRooms, color = StatusCleaning)
                            StatusRowItem(title = "تحت الصيانة الفنية والتشغيلية", count = maintenanceRooms, total = totalRooms, color = StatusMaintenance)
                        }
                    }
                }

                // Recent Arrivals
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
                    modifier = Modifier
                        .weight(1.8f)
                        .height(340.dp)
                        .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "النزلاء النشطون بالفندق اليوم",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuxuryTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val activeReservations = reservations.filter { it.status == "مؤكد" }.take(4)
                        if (activeReservations.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد حجوزات مؤكدة نشطة اليوم",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = LuxuryTextMuted)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(activeReservations) { res ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LuxuryCreamBg, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(LuxuryGold.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = res.roomNumber,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = LuxuryGoldDark
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = res.guestName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = LuxuryTextDark
                                                )
                                            )
                                            Text(
                                                text = "مغادرة: ${res.checkOutDate}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = LuxuryTextMuted
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatMoney(res.totalPrice),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = LuxuryGoldDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = modifier
            .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = LuxuryTextMuted,
                        fontSize = 13.sp
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = LuxuryTextDark,
                    fontFamily = FontFamily.Serif
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LuxuryTextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun StatusRowItem(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LuxuryTextDark,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Text(
                text = "$count غرفة (${(progress * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuxuryTextDark,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = LuxuryGoldLight.copy(alpha = 0.3f)
        )
    }
}


// ==========================================
// 2. ROOMS SUB SCREEN
// ==========================================
@Composable
fun RoomsSubScreen(
    rooms: List<RoomEntity>,
    viewModel: HotelViewModel
) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "شاغرة", "محجوزة", "تنظيف", "صيانة")
    
    // filtered list
    val filteredRooms = if (selectedFilter == "الكل") rooms else rooms.filter { it.status == selectedFilter }
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Status Changer Menu States
    var showStatusDialogForRoom by remember { mutableStateOf<RoomEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rooms_screen")
    ) {
        // Control Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filters Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val color = when (filter) {
                        "شاغرة" -> StatusVacant
                        "محجوزة" -> StatusBooked
                        "تنظيف" -> StatusCleaning
                        "صيانة" -> StatusMaintenance
                        else -> LuxuryGoldDark
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = LuxuryWhite,
                            containerColor = LuxuryWhite,
                            labelColor = LuxuryTextDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = LuxuryGoldLight
                        )
                    )
                }
            }

            // Add Room Button
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                modifier = Modifier
                    .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp))
                    .testTag("add_room_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة غرفة جديدة", color = LuxuryGold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد غرف تطابق هذا التصنيف", style = MaterialTheme.typography.bodyLarge.copy(color = LuxuryTextMuted))
            }
        } else {
            // Grid of Rooms
            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRooms) { room ->
                    RoomCardItem(
                        room = room,
                        onStatusClick = { showStatusDialogForRoom = room }
                    )
                }
            }
        }
    }

    // Add Room Dialog
    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { number, type, price, floor, beds, features ->
                viewModel.addRoom(number, type, price, floor, beds, features)
                showAddDialog = false
            }
        )
    }

    // Room Status changer Dialog
    if (showStatusDialogForRoom != null) {
        RoomStatusChangerDialog(
            room = showStatusDialogForRoom!!,
            onDismiss = { showStatusDialogForRoom = null },
            onStatusSelected = { newStatus ->
                viewModel.updateRoomStatus(showStatusDialogForRoom!!.roomNumber, newStatus)
                showStatusDialogForRoom = null
            }
        )
    }
}

@Composable
fun RoomCardItem(
    room: RoomEntity,
    onStatusClick: () -> Unit
) {
    val statusColor = when (room.status) {
        "شاغرة" -> StatusVacant
        "محجوزة" -> StatusBooked
        "تنظيف" -> StatusCleaning
        "صيانة" -> StatusMaintenance
        else -> LuxuryGold
    }
    
    val isRoyal = room.type == "جناح ملكي"

    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isRoyal) {
                    Modifier.border(1.5.dp, LuxuryGold, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp))
                }
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Number + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Number Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isRoyal) LuxuryGold.copy(alpha = 0.15f) else LuxuryCreamBg,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "غرفة ${room.roomNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isRoyal) LuxuryGoldDark else LuxuryTextDark
                        )
                    )
                }

                // Status Badge Clickable to change state
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .clickable { onStatusClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = room.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Type
            Text(
                text = room.type,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryTextDark
                )
            )

            // Floor & Beds
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الطابق ${room.floor} • عدد الأسرّة: ${room.bedCount}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LuxuryTextMuted
                )
            )

            // Features (Ellipsis)
            if (room.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = room.features,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = LuxuryGoldDark,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuxuryGoldLight))
            Spacer(modifier = Modifier.height(12.dp))

            // Price night
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "سعر الليلة",
                    style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted)
                )
                Text(
                    text = formatMoney(room.pricePerNight),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )
            }
        }
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int, Int, String) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("غرفة فاخرة") }
    var pricePerNight by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("1") }
    var bedCount by remember { mutableStateOf("1") }
    var features by remember { mutableStateOf("") }

    val typesList = listOf("غرفة فاخرة", "جناح ديلوكس", "جناح تنفيذي", "جناح ملكي")
    var expandDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "إضافة غرفة جديدة للأنظمة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("رقم الغرفة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown mock for type
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = {},
                        label = { Text("نوع الغرفة") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandDropdown = !expandDropdown }) {
                                Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandDropdown,
                        onDismissRequest = { expandDropdown = false },
                        modifier = Modifier.background(LuxuryWhite)
                    ) {
                        typesList.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    roomType = t
                                    expandDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pricePerNight,
                        onValueChange = { pricePerNight = it },
                        label = { Text("السعر لليلة (ر.س)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text("الطابق") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.weight(0.5f)
                    )
                }

                OutlinedTextField(
                    value = bedCount,
                    onValueChange = { bedCount = it },
                    label = { Text("عدد الأسرة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = features,
                    onValueChange = { features = it },
                    label = { Text("المميزات (تفصل بفاصلة)") },
                    placeholder = { Text("مثال: إطلالة بحرية، جاكوزي") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = LuxuryTextMuted)
                    }

                    Button(
                        onClick = {
                            val price = pricePerNight.toDoubleOrNull() ?: 500.0
                            val floorNum = floor.toIntOrNull() ?: 1
                            val beds = bedCount.toIntOrNull() ?: 1
                            onConfirm(roomNumber, roomType, price, floorNum, beds, features)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = roomNumber.isNotEmpty()
                    ) {
                        Text("حفظ الغرفة", color = LuxuryGold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomStatusChangerDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("شاغرة", "محجوزة", "تنظيف", "صيانة")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "تعديل حالة الغرفة ${room.roomNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                statuses.forEach { status ->
                    val color = when (status) {
                        "شاغرة" -> StatusVacant
                        "محجوزة" -> StatusBooked
                        "تنظيف" -> StatusCleaning
                        "صيانة" -> StatusMaintenance
                        else -> LuxuryGoldDark
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (room.status == status) color else LuxuryGoldLight,
                                RoundedCornerShape(8.dp)
                            )
                            .background(if (room.status == status) color.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable { onStatusSelected(status) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (room.status == status) FontWeight.Bold else FontWeight.Normal,
                                color = LuxuryTextDark
                            )
                        )
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("إغلاق", color = LuxuryTextMuted)
                }
            }
        }
    }
}


// ==========================================
// 3. RESERVATIONS SUB SCREEN
// ==========================================
@Composable
fun ReservationsSubScreen(
    reservations: List<ReservationEntity>,
    rooms: List<RoomEntity>,
    viewModel: HotelViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredReservations = if (searchQuery.isEmpty()) {
        reservations
    } else {
        reservations.filter {
            it.guestName.contains(searchQuery, ignoreCase = true) ||
            it.roomNumber.contains(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reservations_screen")
    ) {
        // Search & Add Desk Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث باسم النزيل أو رقم الغرفة...") },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = LuxuryGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuxuryWhite,
                    unfocusedContainerColor = LuxuryWhite,
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = LuxuryGoldLight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(360.dp)
                    .height(52.dp)
            )

            // Add Booking Button
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                modifier = Modifier
                    .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp))
                    .testTag("add_booking_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("حجز جديد وغرفة فورية", color = LuxuryGold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (filteredReservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد سجلات حجز مسجلة حالياً", style = MaterialTheme.typography.bodyLarge.copy(color = LuxuryTextMuted))
            }
        } else {
            // Reservation elegant table / list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredReservations) { res ->
                    ReservationCardItem(
                        res = res,
                        onStatusChange = { newStatus ->
                            viewModel.updateReservationStatus(res, newStatus)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReservationDialog(
            rooms = rooms.filter { it.status == "شاغرة" },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, roomNum, checkIn, checkOut, price, notes ->
                viewModel.addReservation(name, phone, roomNum, checkIn, checkOut, price, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReservationCardItem(
    res: ReservationEntity,
    onStatusChange: (String) -> Unit
) {
    val statusColor = when (res.status) {
        "مؤكد" -> StatusVacant
        "مكتمل" -> StatusCleaning
        "ملغي" -> StatusBooked
        else -> LuxuryGold
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = res.guestName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuxuryTextDark
                        )
                    )
                    Text(
                        text = "الهاتف: ${res.guestPhone}",
                        style = MaterialTheme.typography.bodySmall.copy(color = LuxuryTextMuted)
                    )
                }

                // Check-out / actions and badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = res.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuxuryGoldLight))
            Spacer(modifier = Modifier.height(16.dp))

            // Stay details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Room info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.MeetingRoom, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "رقم الغرفة", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    }
                    Text(text = "الغرفة ${res.roomNumber}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LuxuryTextDark))
                }

                // Check in
                Column(modifier = Modifier.weight(1.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Login, contentDescription = null, tint = StatusVacant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تاريخ الدخول", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    }
                    Text(text = res.checkInDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = LuxuryTextDark))
                }

                // Check out
                Column(modifier = Modifier.weight(1.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Logout, contentDescription = null, tint = StatusBooked, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تاريخ المغادرة", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    }
                    Text(text = res.checkOutDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = LuxuryTextDark))
                }

                // Total price
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(text = "إجمالي التكلفة", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    Text(
                        text = formatMoney(res.totalPrice),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGoldDark
                        )
                    )
                }
            }

            // Notes
            if (res.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxuryCreamBg, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ملاحظات وتفضيلات: ${res.notes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LuxuryTextDark,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            }

            // Interactive Actions if Reservation is active ("مؤكد")
            if (res.status == "مؤكد") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Booking Button
                    TextButton(
                        onClick = { onStatusChange("ملغي") }
                    ) {
                        Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null, tint = StatusBooked, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إلغاء الحجز والبطاقة", color = StatusBooked)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Checkout / Complete Button
                    Button(
                        onClick = { onStatusChange("مكتمل") },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusVacant),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = LuxuryWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إجراء تسجيل الخروج", color = LuxuryWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun AddReservationDialog(
    rooms: List<RoomEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Double, String) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }
    var selectedRoomNumber by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedRoomPrice by remember { mutableStateOf(0.0) }
    var expandDropdown by remember { mutableStateOf(false) }

    // Use current date for pre-population
    LaunchedEffect(Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        checkInDate = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 3) // 3 nights default
        checkOutDate = dateFormat.format(cal.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "إنشاء حجز فندقي جديد فوري",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("اسم النزيل الثلاثي") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = guestPhone,
                    onValueChange = { guestPhone = it },
                    label = { Text("رقم جوال النزيل (مع مفتاح الدولة)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Room Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedRoomNumber.isEmpty()) "اختر غرفة شاغرة للنزيل" else "غرفة $selectedRoomNumber (${selectedRoomPrice} ر.س/ليلة)",
                        onValueChange = {},
                        label = { Text("الغرفة المخصصة") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandDropdown = !expandDropdown }) {
                                Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandDropdown,
                        onDismissRequest = { expandDropdown = false },
                        modifier = Modifier.background(LuxuryWhite)
                    ) {
                        if (rooms.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("لا توجد غرف شاغرة حالياً!") },
                                onClick = { expandDropdown = false }
                            )
                        } else {
                            rooms.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text("غرفة ${r.roomNumber} - ${r.type} (${r.pricePerNight} ر.س)") },
                                    onClick = {
                                        selectedRoomNumber = r.roomNumber
                                        selectedRoomPrice = r.pricePerNight
                                        expandDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("تاريخ الدخول") },
                        placeholder = { Text("YYYY-MM-DD") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = checkOutDate,
                        onValueChange = { checkOutDate = it },
                        label = { Text("تاريخ المغادرة") },
                        placeholder = { Text("YYYY-MM-DD") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("طلبات وتفضيلات النزيل الخاصة") },
                    placeholder = { Text("مثال: سرير إضافي، وسائد ريش، إطلالة هادئة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Total calculation preview
                if (selectedRoomPrice > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuxuryGoldLight.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, LuxuryGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تكلفة الإقامة التقريبية (3 ليالي):", style = MaterialTheme.typography.bodyMedium, color = LuxuryTextDark)
                            Text(
                                text = formatMoney(selectedRoomPrice * 3),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LuxuryGoldDark)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = LuxuryTextMuted)
                    }

                    Button(
                        onClick = {
                            val price = selectedRoomPrice * 3
                            onConfirm(guestName, guestPhone, selectedRoomNumber, checkInDate, checkOutDate, price, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = guestName.isNotEmpty() && guestPhone.isNotEmpty() && selectedRoomNumber.isNotEmpty()
                    ) {
                        Text("إتمام الحجز والدخول", color = LuxuryGold)
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. GUESTS SUB SCREEN
// ==========================================
@Composable
fun GuestsSubScreen(
    guests: List<GuestEntity>,
    reservations: List<ReservationEntity>,
    viewModel: HotelViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredGuests = if (searchQuery.isEmpty()) {
        guests
    } else {
        guests.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guests_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن النزلاء بالاسم أو الهاتف...") },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = LuxuryGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LuxuryWhite,
                    unfocusedContainerColor = LuxuryWhite,
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = LuxuryGoldLight
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(360.dp)
                    .height(52.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                modifier = Modifier
                    .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp))
                    .testTag("add_guest_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل ضيف جديد", color = LuxuryGold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (filteredGuests.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد ملفات نزلاء مسجلة تطابق بحثك", style = MaterialTheme.typography.bodyLarge.copy(color = LuxuryTextMuted))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredGuests) { guest ->
                    GuestCardItem(guest = guest)
                }
            }
        }
    }

    if (showAddDialog) {
        AddGuestDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, email, country, status ->
                viewModel.addGuest(name, phone, email, country, status)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GuestCardItem(guest: GuestEntity) {
    val tierColor = when (guest.status) {
        "نخبة VIP" -> StatusVacant
        "ذهبي" -> LuxuryGold
        "فضي" -> StatusCleaning
        else -> LuxuryTextMuted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (guest.status == "نخبة VIP") LuxuryGold else LuxuryGoldLight,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(LuxuryGold.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, LuxuryGold.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = LuxuryGoldDark)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = guest.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuxuryTextDark
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Public, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = guest.country,
                                style = MaterialTheme.typography.bodySmall.copy(color = LuxuryTextMuted)
                            )
                        }
                    }
                }

                // Tier badge with gold touch
                Box(
                    modifier = Modifier
                        .border(1.dp, tierColor, RoundedCornerShape(12.dp))
                        .background(tierColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = guest.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (guest.status == "ذهبي") LuxuryGoldDark else tierColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuxuryGoldLight))
            Spacer(modifier = Modifier.height(16.dp))

            // Guest Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info item 1
                Column {
                    Text(text = "رقم الجوال", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Phone, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = guest.phone, style = MaterialTheme.typography.bodyMedium.copy(color = LuxuryTextDark))
                    }
                }

                // Info item 2
                Column {
                    Text(text = "البريد الإلكتروني", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = guest.email, style = MaterialTheme.typography.bodyMedium.copy(color = LuxuryTextDark))
                    }
                }

                // Info item 3
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "سجل الإقامات السابقة", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    Text(text = "${guest.previousStays} مرات إقامة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LuxuryTextDark))
                }

                // Info item 4
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "إجمالي الإنفاق بالفندق", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                    Text(text = formatMoney(guest.totalSpent), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LuxuryGoldDark))
                }
            }
        }
    }
}

@Composable
fun AddGuestDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("المملكة العربية السعودية") }
    var selectedTier by remember { mutableStateOf("كلاسيك") }

    val tiers = listOf("كلاسيك", "فضي", "ذهبي", "نخبة VIP")
    var expandDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تسجيل نزيل جديد في الدفاتر",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم النزيل بالكامل") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("الجنسية / بلد الإقامة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown for tier
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTier,
                        onValueChange = {},
                        label = { Text("تصنيف عضوية الضيف") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandDropdown = !expandDropdown }) {
                                Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandDropdown,
                        onDismissRequest = { expandDropdown = false },
                        modifier = Modifier.background(LuxuryWhite)
                    ) {
                        tiers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    selectedTier = t
                                    expandDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = LuxuryTextMuted)
                    }

                    Button(
                        onClick = {
                            onConfirm(name, phone, email, country, selectedTier)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = name.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        Text("حفظ وتسجيل النزيل", color = LuxuryGold)
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. INVOICES SUB SCREEN
// ==========================================
@Composable
fun InvoicesSubScreen(
    invoices: List<InvoiceEntity>,
    viewModel: HotelViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val totalPaid = invoices.filter { it.status == "مدفوعة" }.sumOf { it.amount }
    val totalPending = invoices.filter { it.status == "معلقة" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("invoices_screen")
    ) {
        // Quick summary card for Accounting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "الإيرادات المحصلة (المدفوعة)", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                        Text(text = formatMoney(totalPaid), style = MaterialTheme.typography.titleLarge.copy(color = StatusVacant, fontWeight = FontWeight.Bold))
                    }
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = StatusVacant, modifier = Modifier.size(28.dp))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "الفواتير المعلقة والمستحقة", style = MaterialTheme.typography.labelSmall.copy(color = LuxuryTextMuted))
                        Text(text = formatMoney(totalPending), style = MaterialTheme.typography.titleLarge.copy(color = StatusBooked, fontWeight = FontWeight.Bold))
                    }
                    Icon(imageVector = Icons.Outlined.HourglassEmpty, contentDescription = null, tint = StatusBooked, modifier = Modifier.size(28.dp))
                }
            }

            // Create Invoice button
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                modifier = Modifier
                    .height(68.dp)
                    .align(Alignment.CenterVertically)
                    .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp))
                    .testTag("add_invoice_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إصدار فاتورة جديدة", color = LuxuryGold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("سجل الفواتير خالٍ تماماً", style = MaterialTheme.typography.bodyLarge.copy(color = LuxuryTextMuted))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(invoices) { invoice ->
                    InvoiceCardItem(
                        invoice = invoice,
                        onMarkAsPaid = {
                            // Quick pay logic
                            viewModel.addInvoice(
                                guestName = invoice.guestName,
                                roomNumber = invoice.roomNumber,
                                amount = invoice.amount,
                                status = "مدفوعة",
                                method = "بطاقة ائتمان"
                            )
                            viewModel.deleteInvoice(invoice)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddInvoiceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { guest, room, price, status, method ->
                viewModel.addInvoice(guest, room, price, status, method)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InvoiceCardItem(
    invoice: InvoiceEntity,
    onMarkAsPaid: () -> Unit
) {
    val statusColor = when (invoice.status) {
        "مدفوعة" -> StatusVacant
        "معلقة" -> StatusBooked
        else -> LuxuryTextMuted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt style details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(LuxuryGold.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (invoice.status == "مدفوعة") Icons.Outlined.CheckCircle else Icons.Outlined.Payments,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = invoice.guestName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = LuxuryTextDark)
                    )
                    Text(
                        text = "الغرفة ${invoice.roomNumber} • تاريخ الإصدار: ${invoice.date}",
                        style = MaterialTheme.typography.bodySmall.copy(color = LuxuryTextMuted)
                    )
                }
            }

            // Payment details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Payment Method Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (invoice.method) {
                            "بطاقة ائتمان" -> Icons.Outlined.CreditCard
                            "نقداً" -> Icons.Outlined.Payments
                            else -> Icons.Outlined.AccountBalance
                        },
                        contentDescription = null,
                        tint = LuxuryGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = invoice.method, style = MaterialTheme.typography.bodySmall, color = LuxuryTextDark)
                }

                // Amount
                Text(
                    text = formatMoney(invoice.amount),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = LuxuryGoldDark)
                )

                // Status Indicator
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = invoice.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Dynamic Action Button if Pending
                if (invoice.status == "معلقة") {
                    Button(
                        onClick = onMarkAsPaid,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusVacant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("تسديد الفاتورة", color = LuxuryWhite, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun AddInvoiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("معلقة") }
    var method by remember { mutableStateOf("بطاقة ائتمان") }

    val methods = listOf("بطاقة ائتمان", "نقداً", "تحويل بنكي")
    var expandDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "إصدار وتعمير فاتورة حسابية",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("اسم النزيل الثلاثي") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("رقم الغرفة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("قيمة المطالبة المالية (ر.س)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Method drop
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        label = { Text("طريقة الدفع المقترحة") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandDropdown = !expandDropdown }) {
                                Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandDropdown,
                        onDismissRequest = { expandDropdown = false },
                        modifier = Modifier.background(LuxuryWhite)
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    method = m
                                    expandDropdown = false
                                }
                            )
                        }
                    }
                }

                // Billing Status Changer (Paid vs Pending)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { status = "مدفوعة" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == "مدفوعة") StatusVacant else LuxuryCreamBg,
                            contentColor = if (status == "مدفوعة") LuxuryWhite else LuxuryTextDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (status == "مدفوعة") Color.Transparent else LuxuryGoldLight, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("خالصة ومسددة")
                    }

                    Button(
                        onClick = { status = "معلقة" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == "معلقة") StatusBooked else LuxuryCreamBg,
                            contentColor = if (status == "معلقة") LuxuryWhite else LuxuryTextDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (status == "معلقة") Color.Transparent else LuxuryGoldLight, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("معلقة للدفع")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = LuxuryTextMuted)
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 100.0
                            onConfirm(guestName, roomNumber, amt, status, method)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = guestName.isNotEmpty() && roomNumber.isNotEmpty() && amountText.isNotEmpty()
                    ) {
                        Text("ترحيل الفاتورة للنزيل", color = LuxuryGold)
                    }
                }
            }
        }
    }
}


// ==========================================
// 6. EMPLOYEES SUB SCREEN (OPTIONAL - PROVIDED)
// ==========================================
@Composable
fun EmployeesSubScreen(
    employees: List<EmployeeEntity>,
    viewModel: HotelViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("employees_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "جدول توزيع المناوبات وتكليف الكوادر اليومية",
                style = MaterialTheme.typography.titleMedium.copy(color = LuxuryTextMuted)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                modifier = Modifier
                    .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp))
                    .testTag("add_employee_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تكليف موظف جديد", color = LuxuryGold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (employees.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("سجل الموظفين غير نشط اليوم", style = MaterialTheme.typography.bodyLarge.copy(color = LuxuryTextMuted))
            }
        } else {
            // Group employees by shift
            val morningStaff = employees.filter { it.shift == "صباحية" }
            val eveningStaff = employees.filter { it.shift == "مسائية" }
            val nightStaff = employees.filter { it.shift == "ليلية" }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    ShiftSectionHeader(shiftName = "المناوبة الصباحية (06:00 ص - 02:00 م)")
                }
                items(morningStaff) { emp ->
                    EmployeeCardItem(employee = emp, onStatusSwitch = { newStatus ->
                        viewModel.updateEmployeeStatus(emp, newStatus)
                    })
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ShiftSectionHeader(shiftName = "المناوبة المسائية (02:00 م - 10:00 م)")
                }
                items(eveningStaff) { emp ->
                    EmployeeCardItem(employee = emp, onStatusSwitch = { newStatus ->
                        viewModel.updateEmployeeStatus(emp, newStatus)
                    })
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ShiftSectionHeader(shiftName = "المناوبة الليلية (10:00 م - 06:00 ص)")
                }
                items(nightStaff) { emp ->
                    EmployeeCardItem(employee = emp, onStatusSwitch = { newStatus ->
                        viewModel.updateEmployeeStatus(emp, newStatus)
                    })
                }
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, role, shift, phone ->
                viewModel.addEmployee(name, role, shift, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ShiftSectionHeader(shiftName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LuxuryGoldLight.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .border(1.dp, LuxuryGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Outlined.WatchLater, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = shiftName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LuxuryGoldDark)
        )
    }
}

@Composable
fun EmployeeCardItem(
    employee: EmployeeEntity,
    onStatusSwitch: (String) -> Unit
) {
    val isDuty = employee.status == "في الخدمة"
    val color = if (isDuty) StatusVacant else StatusBooked

    Card(
        colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LuxuryGoldLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(LuxuryGold.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Engineering, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = employee.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = LuxuryTextDark))
                    Text(text = "الدور: ${employee.role} • هاتف: ${employee.phone}", style = MaterialTheme.typography.bodySmall, color = LuxuryTextMuted)
                }
            }

            // Duty switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = employee.status, style = MaterialTheme.typography.bodyMedium, color = color)
                }

                // Switch action
                TextButton(
                    onClick = {
                        onStatusSwitch(if (isDuty) "خارج الخدمة" else "في الخدمة")
                    }
                ) {
                    Text(
                        text = if (isDuty) "تعيين خارج الخدمة" else "تسجيل دخول للخدمة",
                        color = LuxuryGoldDark,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("صباحية") }
    var phone by remember { mutableStateOf("") }

    val shifts = listOf("صباحية", "مسائية", "ليلية")
    var expandDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LuxuryWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تكليف كادر جديد للعمل",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextDark
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الموظف الثلاثي") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("الدور الوظيفي (استقبال، صيانة، خدمة غرف...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف الموظف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGoldLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Shift selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "مناوبة $shift",
                        onValueChange = {},
                        label = { Text("المناوبة المخصصة") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = LuxuryGoldLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandDropdown = !expandDropdown }) {
                                Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandDropdown,
                        onDismissRequest = { expandDropdown = false },
                        modifier = Modifier.background(LuxuryWhite)
                    ) {
                        shifts.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("مناوبة $s") },
                                onClick = {
                                    shift = s
                                    expandDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = LuxuryTextMuted)
                    }

                    Button(
                        onClick = {
                            onConfirm(name, role, shift, phone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryDarkBlue),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = name.isNotEmpty() && role.isNotEmpty() && phone.isNotEmpty()
                    ) {
                        Text("تكليف الموظف", color = LuxuryGold)
                    }
                }
            }
        }
    }
}
