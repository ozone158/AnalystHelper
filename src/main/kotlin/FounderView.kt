package org.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FounderView(
    onBack: () -> Unit, 
    onEnterProcess: () -> Unit = {},
    onViewAnalyses: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = "Submit Your Startup Idea",
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Bank of Montreal Startup Evaluation",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Clarification points
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Clarification:",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. This is an AI-assisted evaluation",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = "2. No guarantee of funding",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = "3. Human review involved",
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enter process button
            Button(
                onClick = onEnterProcess,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    "Start Submission",
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // View analyses button
            OutlinedButton(
                onClick = onViewAnalyses,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colors.primary
                ),
                border = BorderStroke(
                    2.dp,
                    MaterialTheme.colors.primary
                )
            ) {
                Text(
                    "View My Analyses",
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
