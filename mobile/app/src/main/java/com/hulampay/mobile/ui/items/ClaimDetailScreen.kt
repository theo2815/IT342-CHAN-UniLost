package com.hulampay.mobile.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
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
fun ClaimDetailScreen(navController: NavController, claimId: String) {
    val claim = remember { MockClaims.getClaimById(claimId) }
    val context = LocalContext.current

    if (claim == null) {
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
                    Text("Claim not found", fontWeight = FontWeight.Medium)
                }
            }
        }
        return
    }

    val isClaimant = claim.claimantId == "u1"
    val isPoster = claim.posterId == "u1"

    val statusColor = when (claim.status) {
        "PENDING" -> Color(0xFFf59e0b)
        "APPROVED" -> Color(0xFF22c55e)
        "REJECTED" -> ErrorRed
        "HANDED_OVER" -> Sage
        else -> Slate400
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claim Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Claim Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(claim.itemType, if (claim.itemType == "LOST") ErrorRed else Sage)
                        StatusChip(claim.status.replace("_", " "), statusColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(claim.itemTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Claimed ${MockItems.timeAgo(claim.createdAt)}",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                }
            }

            // Parties
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Poster
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Slate600),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                claim.posterName.take(1).uppercase(),
                                color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Posted by", fontSize = 10.sp, color = Slate400)
                            Text(claim.posterName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                        }
                    }
                }

                // Claimant
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFa855f7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                claim.claimantName.take(1).uppercase(),
                                color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Claimed by", fontSize = 10.sp, color = Slate400)
                            Text(claim.claimantName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                        }
                    }
                }
            }

            // Claim Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (claim.secretDetailAnswer.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SECRET DETAIL ANSWER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(claim.secretDetailAnswer, fontSize = 14.sp, color = Slate800, lineHeight = 20.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MESSAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.5.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(claim.message, fontSize = 14.sp, color = Slate800, lineHeight = 20.sp)
                }
            }

            // Status-specific content
            when (claim.status) {
                "PENDING" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFf59e0b).copy(alpha = 0.08f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFd97706), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Waiting for Review", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFFd97706))
                                Text("The poster has not reviewed your claim yet.", fontSize = 13.sp, color = Color(0xFFd97706).copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                "REJECTED" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.08f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Claim Rejected", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = ErrorRed)
                                Text("The poster did not approve this claim.", fontSize = 13.sp, color = ErrorRed.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                "APPROVED", "HANDED_OVER" -> {
                    HandoverStepper(
                        claim = claim,
                        isClaimant = isClaimant,
                        isPoster = isPoster,
                        onConfirm = {
                            Toast.makeText(context, "Handover confirmed! (Mock action)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HandoverStepper(
    claim: com.hulampay.mobile.data.mock.MockClaim,
    isClaimant: Boolean,
    isPoster: Boolean,
    onConfirm: () -> Unit
) {
    val steps = listOf("Claim Approved", "Poster Confirms", "Claimant Confirms", "Handed Over")

    val currentStep = when {
        claim.status == "HANDED_OVER" || (claim.posterConfirmed && claim.claimantConfirmed) -> 4
        claim.posterConfirmed -> 2
        claim.claimantConfirmed -> 1
        else -> 1
    }

    val canConfirm = (isClaimant && !claim.claimantConfirmed) || (isPoster && !claim.posterConfirmed)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Handover Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate800)
            Spacer(modifier = Modifier.height(16.dp))

            // Steps
            steps.forEachIndexed { index, step ->
                val stepNum = index + 1
                val isCompleted = stepNum < currentStep || currentStep == 4
                val isActive = stepNum == currentStep && currentStep < 4

                Row(verticalAlignment = Alignment.Top) {
                    // Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Sage
                                    isActive -> Slate600
                                    else -> Color.LightGray
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("$stepNum", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            step,
                            fontWeight = if (isCompleted || isActive) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isCompleted || isActive) Slate800 else Slate400
                        )
                        if (index < steps.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Connecting line
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .width(2.dp)
                            .height(16.dp)
                            .background(if (isCompleted) Sage else Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm button
            if (canConfirm) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Sage)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Handover", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Confirm that you have ${if (isClaimant) "received" else "handed over"} the item.",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Status text
            Spacer(modifier = Modifier.height(8.dp))
            if (currentStep == 4) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Sage.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Item successfully handed over!", fontSize = 13.sp, color = Sage, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Text(
                    when {
                        claim.posterConfirmed && !claim.claimantConfirmed && isClaimant -> "The poster has confirmed. Please confirm once you receive the item."
                        claim.claimantConfirmed && !claim.posterConfirmed && isPoster -> "The claimant has confirmed. Please confirm the handover."
                        else -> "Both parties need to confirm the handover."
                    },
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )
            }

            // Location hint
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3b82f6).copy(alpha = 0.06f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3b82f6), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Suggested meetup: Campus Security Office", fontSize = 12.sp, color = Color(0xFF3b82f6))
                }
            }
        }
    }
}
