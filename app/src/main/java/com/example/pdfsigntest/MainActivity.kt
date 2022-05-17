package com.example.pdfsigntest

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.example.pdfsigntest.certificate.CertificateService
import com.example.pdfsigntest.signature.SigningService
import com.example.pdfsigntest.ui.theme.PDFSignTestTheme
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PDFSignTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainApp(contentResolver)
                }
            }
        }
    }
}

@Composable
fun MainApp(contentResolver: ContentResolver) {
    var selectedPDF by remember { mutableStateOf<InputStream?>(null) }
    val launcherPDF = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPDF = contentResolver.openInputStream(uri)
        }
    }
    val launcherCertificate = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val selectedCertificate = contentResolver.openInputStream(uri)

            if (selectedCertificate != null && selectedPDF != null) {
                signCertificate(selectedPDF!!, selectedCertificate)

                selectedPDF = null // clearing the user input
            }
        }
    }

    MainContent(selectedPDF, launcherCertificate) {
        launcherPDF.launch("*/*")
    }
}

@Composable
private fun MainContent(
    selectedPDF: InputStream? = null,
    launcherPDF: ManagedActivityResultLauncher<String, Uri?>,
    onImageClick: () -> Unit
) {
    Scaffold {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = { writeCertificateToExternalStorage() }) {
                Text(text = "Generate certificate")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (selectedPDF != null) {
                Text(text = "PDF has been chosen!")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { launcherPDF.launch("*/*") }) {
                    Text(text = "Choose certificate to sign")
                }
            } else {
                OutlinedButton(onClick = onImageClick) {
                    Text(text = "Choose PDF")
                }
            }
        }
    }
}

private fun writeCertificateToExternalStorage() {
    val certificateData = CertificateService.CertificateData(
        name = "Mark Yav",
        organization = "Axel",
        organizationalUnitName = "Android team",
        email = "mark.yav@axel.org",
        country = "UA"
    )
    CertificateService().generateCertificate(
        certificateData = certificateData,
        keyStoreDestination = File(Environment.getExternalStorageDirectory(), "keystore1.pfx").outputStream(),
        keyStorePassword = "1234",
        keyStoreType = "PKCS12"
    )

    // it was useful: https://stackoverflow.com/questions/65637610/saving-files-in-android-11-to-external-storagesdk-30
    // getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    //val finalFile = File(Environment.getExternalStorageDirectory(), "PDF_generated_certificate.pfx")
}

private fun signCertificate(selectedPDF: InputStream, selectedCertificate: InputStream) {
    val outFile = File(Environment.getExternalStorageDirectory(), "PDF_signed.pdf")

    // very good doc: https://jvmfy.com/2018/11/17/how-to-digitally-sign-pdf-files/
    SigningService(
        keyStoreType = "PKCS12",
        keyStoreFile = selectedCertificate,//File(Environment.getExternalStorageDirectory(), "MarkYav.pfx"),
        keyStorePassword = "123456Aa",
        certificateAlias = "e44024b1ddf65195cc98feeff4f90c7b2252b982",
        tsaUrl = "I don't know what it is"
    ).signPdf(selectedPDF, outFile.outputStream())
}