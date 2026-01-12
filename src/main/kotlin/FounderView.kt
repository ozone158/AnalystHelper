package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FounderView(onBack: () -> Unit, onEnterProcess: () -> Unit = {}) {
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
            text = "Submit your startup idea to BMO",
            style = MaterialTheme.typography.h4,
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
        
        // Enter process button
        Button(
            onClick = onEnterProcess,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
        ) {
            Text("Enter the process", style = MaterialTheme.typography.body1)
        }
    }
}
