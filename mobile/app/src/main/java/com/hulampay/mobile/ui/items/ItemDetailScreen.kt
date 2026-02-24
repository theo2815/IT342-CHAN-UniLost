package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hulampay.mobile.data.mock.MockClaims
import com.hulampay.mobile.data.mock.MockItems
import com.hulampay.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(navController: NavController, itemId: String) {
    val item = remember { MockItems.items.find { it.id == itemId } }

    if (item == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Not Found") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Slate400)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Item not found", fontWeight = FontWeight.Medium)
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val isFound = item.type == "FOUND"
    val isPoster = item.postedByName == "Juan D."
    var showClaimSheet by remember { mutableStateOf(false) }
    var claimSubmitted by remember { mutableStateOf(false) }
    var secretAnswer by remember { mutableStateOf("") }
    var claimMessage by remember { mutableStateOf("") }

    val incomingClaims = remember { MockClaims.getClaimsForItem(itemId) }
    val relatedItems = remember {
        MockItems.items.filter { it.id != item.id && it.status == "ACTIVE" && (it.category == item.category || it.schoolShortName == item.schoolShortName) }.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showClaimSheet = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                    ) {
                        Icon(
                            if (isFound) Icons.Default.PanTool else Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFound) "This Is Mine" else "I Found This")
                    }
                    OutlinedButton(
                        onClick = { /* report */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isFound) Modifier.blur(20.dp) else Modifier)
                        .background(Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Slate400)
                }

                // Type badge
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(20.dp),
                    color = if (item.type == "LOST") ErrorRed else Sage
                ) {
                    Text(
                        item.type, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                if (isFound) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Text(
                            "Image blurred to protect the owner",
                            color = White, fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Status chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(item.type, if (item.type == "LOST") ErrorRed else Sage)
                    StatusChip(item.status, when (item.status) {
                        "ACTIVE" -> Color(0xFF3b82f6)
                        "CLAIMED" -> Color(0xFFa855f7)
                        "HANDED_OVER" -> Sage
                        else -> Slate400
                    })
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate800)

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(item.description, fontSize = 15.sp, color = Slate600, lineHeight = 22.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Meta info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetaRow(Icons.Default.Category, "Category", item.category)
                        MetaRow(Icons.Default.LocationOn, "Location", item.locationDescription)
                        MetaRow(Icons.Default.CalendarToday, "Posted", MockItems.timeAgo(item.createdAt))
                        MetaRow(Icons.Default.School, "School", item.schoolName)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Poster info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Slate600),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                item.postedByName.take(1).uppercase(),
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.postedByName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate800)
                            Text(item.schoolName, fontSize = 12.sp, color = Slate400)
                        }
                    }
                }

                // Related items
                if (relatedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Related Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate800)
                    Spacer(modifier = Modifier.height(8.dp))
                    relatedItems.forEach { relItem ->
                        ItemCard(
                            item = relItem,
                            onClick = { navController.navigate("item_detail_screen/${relItem.id}") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Incoming Claims section (visible only for poster)
                if (isPoster && incomingClaims.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Incoming Claims", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate800)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFa855f7).copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${incomingClaims.size}",
                                color = Color(0xFFa855f7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Only one claim can be approved per item",
                        fontSize = 12.sp,
                        color = Color(0xFFd97706)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    incomingClaims.forEach { claim ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("claim_detail_screen/${claim.id}") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Claimant info
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFa855f7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            claim.claimantName.take(1).uppercase(),
                                            color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(claim.claimantName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate800)
                                        Text(claim.claimantSchool, fontSize = 11.sp, color = Slate400)
                                    }
                                    StatusChip(claim.status.replace("_", " "), when (claim.status) {
                                        "PENDING" -> Color(0xFFf59e0b)
                                        "APPROVED" -> Color(0xFF22c55e)
                                        "REJECTED" -> ErrorRed
                                        "HANDED_OVER" -> Sage
                                        else -> Slate400
                                    })
                                }

                                // Secret detail comparison (for FOUND items)
                                if (isFound && claim.secretDetailAnswer.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Sage.copy(alpha = 0.08f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("YOUR DETAIL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Sage, letterSpacing = 0.5.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(item.secretDetail ?: "N/A", fontSize = 12.sp, color = Slate600, lineHeight = 16.sp)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFa855f7).copy(alpha = 0.08f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("THEIR ANSWER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFa855f7), letterSpacing = 0.5.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(claim.secretDetailAnswer, fontSize = 12.sp, color = Slate600, lineHeight = 16.sp)
                                            }
                                        }
                                    }
                                }

                                // Message
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(claim.message, fontSize = 13.sp, color = Slate600, maxLines = 2)

                                // Approve / Reject buttons (only for PENDING)
                                if (claim.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Claim rejected (Mock action)", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 13.sp)
                                        }
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Claim approved (Mock action)", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Sage)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Claim Bottom Sheet
    if (showClaimSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showClaimSheet = false
                claimSubmitted = false
                secretAnswer = ""
                claimMessage = ""
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                if (claimSubmitted) {
                    // Success state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Sage.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Claim Submitted!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate800)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "The poster will review your claim and get back to you.",
                            fontSize = 14.sp, color = Slate400,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                showClaimSheet = false
                                claimSubmitted = false
                                navController.navigate("my_claims_screen")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate600)
                        ) {
                            Text("View My Claims")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            showClaimSheet = false
                            claimSubmitted = false
                            secretAnswer = ""
                            claimMessage = ""
                        }) {
                            Text("Close", color = Slate400)
                        }
                    }
                } else {
                    // Claim form
                    Text("Submit a Claim", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Provide details to prove this item is yours.", fontSize = 14.sp, color = Slate400)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFound) Sage.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isFound) Icons.Default.Inventory2 else Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = if (isFound) Sage else ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate800, maxLines = 1)
                                Text(item.type, fontSize = 11.sp, color = if (isFound) Sage else ErrorRed, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secret detail answer (FOUND items only)
                    if (isFound) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Secret Detail Answer", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Describe a unique feature only the owner would know.",
                            fontSize = 12.sp, color = Slate400
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = secretAnswer,
                            onValueChange = { secretAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., There's a scratch on the back...") },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Message
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Message", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate800)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Why do you think this item is yours?",
                        fontSize = 12.sp, color = Slate400
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = claimMessage,
                        onValueChange = { if (it.length <= 500) claimMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Describe when and where you lost it...") },
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        maxLines = 5,
                        supportingText = { Text("${claimMessage.length}/500", fontSize = 11.sp, color = Slate400) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit
                    Button(
                        onClick = {
                            claimSubmitted = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate600),
                        enabled = claimMessage.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Claim", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label.replace("_", " "),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate400)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Slate400, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
    }
}
