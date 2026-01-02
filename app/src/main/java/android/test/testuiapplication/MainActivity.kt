package android.test.testuiapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import android.test.testuiapplication.ui.theme.TestUIApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestUIApplicationTheme {
                CarInterfaceExample()
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun HorizontalCircularButtonLayoutScreenPreview() {
    TestUIApplicationTheme {
        CarInterfaceExample()
    }
}