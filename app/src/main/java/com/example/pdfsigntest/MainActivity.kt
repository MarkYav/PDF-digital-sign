package com.example.pdfsigntest

//import org.apache.pdfbox.examples.signature.CreateSignature
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.example.pdfsigntest.signature.SigningService
import com.example.pdfsigntest.ui.theme.PDFSignTestTheme
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions
import org.bouncycastle.operator.ContentSigner
//import org.apache.pdfbox.examples.signature.CreateSignature
import org.spongycastle.asn1.x500.X500NameBuilder
import org.spongycastle.asn1.x500.style.BCStyle
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.X509v1CertificateBuilder
import org.spongycastle.cert.X509v3CertificateBuilder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.*
import java.lang.Exception
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
//import kotlin.math.sign

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

private fun generateRSAKeyPair(): KeyPair {
    val kpg = KeyPairGenerator.getInstance("RSA");
    //kpg.initialize(1024)
    kpg.initialize(2048)
    val keyPair = kpg.genKeyPair()
    return keyPair
}

private fun generateCertificateV1(): Pair<KeyPair, X509v1CertificateBuilder> {
    val VALIDITY_IN_DAYS = 30

    val startDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val endDate = Date(System.currentTimeMillis() + VALIDITY_IN_DAYS * 24 * 60 * 60 * 1000)

    val nameBuilder = X500NameBuilder(BCStyle.INSTANCE)
        .addRDN(BCStyle.O, "oTAG")
        .addRDN(BCStyle.OU, "ouTAG")
        .addRDN(BCStyle.L, "lTAG")
        .addRDN(BCStyle.T, "tTAG")
        .addRDN(BCStyle.CN, "cnTAG")
        .addRDN(BCStyle.NAME, "nameTAG")
    val x500Name = nameBuilder.build()

    val keyPair = generateRSAKeyPair()
    val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

    val v1CertGen = X509v1CertificateBuilder(
        x500Name,
        BigInteger.valueOf(Random().nextLong()), //SecureRandom()...
        startDate,
        endDate,
        x500Name,
        subjectPublicKeyInfo
    )

    return Pair(keyPair, v1CertGen)
}

private fun generateCertificateV3(): Pair<KeyPair, X509v3CertificateBuilder> {
    val VALIDITY_IN_DAYS = 30

    val startDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val endDate = Date(System.currentTimeMillis() + VALIDITY_IN_DAYS * 24 * 60 * 60 * 1000)

    val nameBuilder = X500NameBuilder(BCStyle.INSTANCE)
        .addRDN(BCStyle.CN, "Mark Yav")
        .addRDN(BCStyle.O, "Axel")
        .addRDN(BCStyle.OU, "Android team")
        .addRDN(BCStyle.E, "yav.mar@axel.com")
        .addRDN(BCStyle.C, "UA")
    val x500Name = nameBuilder.build()

    val keyPair = generateRSAKeyPair()
    val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

    val v3CertGen = X509v3CertificateBuilder(
        x500Name,
        BigInteger.valueOf(SecureRandom().nextLong()),
        startDate,
        endDate,
        x500Name,
        subjectPublicKeyInfo
    )

    return Pair(keyPair, v3CertGen)
}
/*
private fun generateX509V3Certificate(): X509Certificate? {
    val generator = X509V3CertificateGenerator()
    generator.setSerialNumber(serialNumber)
    generator.setIssuerDN(issuer)
    generator.setSubjectDN(subject)
    generator.setNotBefore(notBefore)
    generator.setNotAfter(notAfter)
    generator.setPublicKey(keyPair.public)
    generator.setSignatureAlgorithm("SHA256WithRSAEncryption")
    generator.addExtension(X509Extensions.BasicConstraints, true, BasicConstraints(isCA))
    generator.addExtension(X509Extensions.KeyUsage, true, KeyUsage(160))
    generator.addExtension(
        X509Extensions.ExtendedKeyUsage,
        true,
        ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth)
    )
    if (generalNames != null) {
        generator.addExtension(X509Extensions.SubjectAlternativeName, false, generalNames)
    }
    return generator.generateX509Certificate(keyPair.private, SecurityUtil.getSecurityProvider())
}
*/
private fun certificateSelfSign(): X509CertificateHolder {
    //val pair = generateCertificateV1()
    val pair = generateCertificateV3()

    Security.addProvider(BouncyCastleProvider())
    //val sigGen = JcaContentSignerBuilder("SHA256WithRSAEncryption")
    val sigGen = JcaContentSignerBuilder("SHA256WITHRSA")
        .setProvider("SC")
        .build(pair.first.private)
    val x509CertificateHolder = pair.second.build(sigGen)

    return x509CertificateHolder
}

private fun writeCertificateToExternalStorage() {
    // creating self-sign certificate: https://medium.com/@bouhady/self-sign-certificate-creation-using-spongy-castle-for-android-app-61f1545dd63
    // if you want to use bouncyCastle: http://www.java2s.com/Tutorial/Java/0490__Security/CreatingaSelfSignedVersion3Certificate.htm
    // may be useful (but I'm not sure...): https://www.codegrepper.com/code-examples/shell/generate+pfx+certificate
    val certificateHolder = certificateSelfSign()
    // how to get certificate from certificateHolder: https://stackoverflow.com/questions/6370368/bouncycastle-x509certificateholder-to-x509certificate
    val certificate: X509Certificate =
        JcaX509CertificateConverter().getCertificate(certificateHolder)

    // how to create certificate pfx file: https://stackoverflow.com/questions/26311678/how-to-create-certificate-pfx-file-in-java
    // What are certificate formats and what is the difference between them: https://www.ssls.com/knowledgebase/what-are-certificate-formats-and-what-is-the-difference-between-them/
    // What are the merits of JKS vs PKCS12 for code signing: https://stackoverflow.com/questions/3867019/what-are-the-merits-of-jks-vs-pkcs12-for-code-signing
    // KeyStore: https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html#setCertificateEntry(java.lang.String,%20java.security.cert.Certificate)
    val ks= KeyStore.getInstance("PKCS12")
    ks.load(null, PASSWORD)
    ks.setCertificateEntry(KeyStore.getDefaultType()/*"ca-certificate"*/, certificate)

    // it was useful: https://stackoverflow.com/questions/65637610/saving-files-in-android-11-to-external-storagesdk-30
    // getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val finalFile = File(Environment.getExternalStorageDirectory(), "keystore1.pfx")
    val fos = FileOutputStream(finalFile)
    ks.store(fos, PASSWORD)
}

private fun signCertificate(selectedPDF: Uri, selectedCertificate: Uri) {
    /*// code from (look at main method) here: https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/CreateSignature.java?view=markup
    //val keystore = KeyStore.getInstance("PKCS12")
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

    /*
    val doc = PDDocument.load(inFile)
    //val pdfSignature = PDSignature()

    // create signature dictionary
    val signature = PDSignature()
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
    signature.name = "Example User"
    signature.location = "Los Angeles, CA"
    signature.reason = "Testing"

    signature.signDate = Calendar.getInstance()
    doc.addSignature(signature, SignatureOptions())
    doc.saveIncremental(outFile.outputStream())

    doc.close()
    //sign.signDetached(fileOut, fileTo)
    */
}