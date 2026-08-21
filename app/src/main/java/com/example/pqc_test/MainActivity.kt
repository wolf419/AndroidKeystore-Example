package com.example.pqc_test

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pqc_test.ui.theme.PQCtestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPairGenerator
import java.security.Signature


import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Code generated using ChatGPT 5.6 Sol
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PQCtestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        Text("Android Keystore PQC example")
                        GenerateSignAndVerify()
                    }
                }
            }
        }
    }
}


@Composable
fun GenerateSignAndVerify() {
    val context = LocalContext.current

    val keystoreVersion = remember {
        context.packageManager.systemAvailableFeatures
            .firstOrNull {
                it.name == PackageManager.FEATURE_HARDWARE_KEYSTORE
            }
            ?.version ?: 0
    }

    val result by produceState("Generating key...") {
        if (keystoreVersion < 500) {
            value = "ML-DSA unsupported. Keystore version: $keystoreVersion"
            return@produceState
        }

        value = withContext(Dispatchers.Default) {
            try {
                val generator = KeyPairGenerator.getInstance(
                    "ML-DSA-65",
                    "AndroidKeyStore"
                )

                generator.initialize(
                    KeyGenParameterSpec.Builder(
                        "mldsa-key",
                        KeyProperties.PURPOSE_SIGN or
                                KeyProperties.PURPOSE_VERIFY
                    ).build()
                )

                val keyPair = generator.generateKeyPair()
                val data = "Hello".encodeToByteArray()

                val signer = Signature.getInstance("ML-DSA-65")
                signer.initSign(keyPair.private)
                signer.update(data)
                val signatureBytes = signer.sign()

                val verifier = Signature.getInstance("ML-DSA-65")
                verifier.initVerify(keyPair.public)
                verifier.update(data)

                "Signature valid: ${verifier.verify(signatureBytes)}"
            } catch (exception: Exception) {
                Log.e("PQC", "ML-DSA failed", exception)

                val rootCause = generateSequence(exception as Throwable) {
                    it.cause
                }.last()

                "Error: ${rootCause.javaClass.simpleName}: ${rootCause.message}"
            }
        }
    }

    Text(result)
}