package com.example.sam.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveRouteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
){
    val appTurquoise = Color(0xFF374151)

    var routeName by remember { mutableStateOf("") }
    var routeGrade by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val grades = listOf(
        "3A", "3B", "3C", "4A", "4B", "4C", "5A", "5B", "5C",
        "6A", "6A+", "6B", "6B+", "6C", "6C+", "7A", "7A+", "7B", "7B+", "7C", "7C+",
        "8A", "8A+", "8B", "8B+", "8C", "8C+", "9A"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Route hochladen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = appTurquoise
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Name", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appTurquoise,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = appTurquoise,
                        cursorColor = appTurquoise
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = routeGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Schwierigkeit", color = Color.Gray) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appTurquoise,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = appTurquoise
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .heightIn(max = 220.dp)
                    ) {
                        grades.forEach { grade ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = grade,
                                        fontWeight = FontWeight.Medium,
                                        color = appTurquoise
                                    )
                                },
                                onClick = {
                                    routeGrade = grade
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (routeName.isNotBlank() && routeGrade.isNotBlank()) {
                        onSave(routeName, routeGrade) // übergibt Daten nach oben
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appTurquoise)
            ) {
                Text("Speichern", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    )
}