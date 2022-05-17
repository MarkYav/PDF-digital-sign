package com.example.pdfsigntest

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.pdfsigntest.certificate.CertificateService
import com.example.pdfsigntest.signature.SigningService
import com.example.pdfsigntest.ui.theme.PDFSignTestTheme
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PDFSignTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainApp()
                }
            }
        }
    }
}

val PASSWORD = "1234".toCharArray()
val PASSWORD_FOR_MARKYAV = "123456Aa".toCharArray()
var part: FileDescriptor? = null
var IS: InputStream? = null

@Composable
fun MainApp() {
    val t = LocalContext.current
    var selectedPDF by remember { mutableStateOf<Uri?>(null) }
    val launcherPDF = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedPDF = uri }
    val launcherCertificate = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {

            IS = t.getContentResolver().openInputStream(uri)
            signCertificate(selectedPDF!!, uri)
            val tt = t.getContentResolver().openFileDescriptor(uri, "r")?.getFileDescriptor()
            //tt
        }
    }

    MainContent(selectedPDF, launcherCertificate) {
        launcherPDF.launch("*/*")
    }
}

@Composable
private fun MainContent(
    selectedPDF: Uri? = null,
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
            if (selectedPDF != null) {
                Text(text = "PDF has been chosen!")
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
        certificateData,
        File(Environment.getExternalStorageDirectory(), "keystore1.pfx"),
        "1234"
    )

    // it was useful: https://stackoverflow.com/questions/65637610/saving-files-in-android-11-to-external-storagesdk-30
    // getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val finalFile = File(Environment.getExternalStorageDirectory(), "keystore1.pfx")
}

private fun signCertificate(selectedPDF: Uri, selectedCertificate: Uri) {
    // code from (look at main method) here: https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/CreateSignature.java?view=markup
    // very good doc: https://jvmfy.com/2018/11/17/how-to-digitally-sign-pdf-files/
    /*//val keystore = KeyStore.getInstance("PKCS12")
    //val fileCertificate: File = File(selectedCertificate.path)
    //val fileCertificate: File = File(Environment.
        //getExternalStorageDirectory(), "MarkYav.pfx") // TODO: use selectedCertificate
    //val fileCertificate: File = File(URI.create(selectedCertificate.toString()))
    //val fileCertificate: File = File(part)
    //try {
        //val fis = FileInputStream(fileCertificate) // вылетает только с файлом MarkYav.pfx
        //keystore.load(fis, PASSWORD_FOR_MARKYAV)
    //} catch (e: Exception) {
        //Log.d("TAG", e.cause.toString())
    //}

    //val t = keystore.aliases().nextElement()
    //val signing = CreateSignature(keystore, PASSWORD)
    //val certificate = keystore.getCertificate("e44024b1ddf65195cc98feeff4f90c7b2252b982"/*KeyStore.getDefaultType()*/) as X509Certificate
    //val name = "just name"



    //val sha1Signer: ContentSigner = JcaContentSignerBuilder("SHA256WithRSA")
    //    .build(certificate.subjectX500Principal.) todo


    //val sign = CreateSignature(keystore, PASSWORD_FOR_MARKYAV)*/

    val inFile = File(Environment.getExternalStorageDirectory(), "2Skladnist02.pdf")
    val outFile = File(Environment.getExternalStorageDirectory(), "2Skladnist02_signed.pdf")

    SigningService(
        keyStoreType = "PKCS12",
        keyStoreFile = File(Environment.getExternalStorageDirectory(), "MarkYav.pfx"),
        keyStorePassword = "123456Aa",
        certificateAlias = "e44024b1ddf65195cc98feeff4f90c7b2252b982",
        tsaUrl = "I don't know what it is"
    ).signPdf(inFile, outFile)
}