package com.example.pdfsigntest.signature

import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*


class Signature(
    keyStore: KeyStore,
    keyStorePassword: CharArray,
    appCertificateAlias: String,
    tsaUrl: String
) : SignatureInterface {
    private val privateKey: PrivateKey
    private val certificateChain: Array<Certificate>
    private val tsaUrl: String

    init {
        certificateChain = Optional.ofNullable(keyStore.getCertificateChain(appCertificateAlias))
            .orElseThrow { IOException("Could not find a proper certificate chain") }
        privateKey = keyStore.getKey(appCertificateAlias, keyStorePassword) as PrivateKey
        val certificate = certificateChain[0]
        if (certificate is X509Certificate) {
            certificate.checkValidity()
        }
        this.tsaUrl = tsaUrl
    }

    override fun sign(content: InputStream): ByteArray {
        val gen = CMSSignedDataGenerator()
        val cert = certificateChain[0] as X509Certificate
        val sha1Signer = JcaContentSignerBuilder("SHA256WithRSA")
            .build(privateKey)
        gen.addSignerInfoGenerator(JcaSignerInfoGeneratorBuilder(
            JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert)
        )
        gen.addCertificates(JcaCertStore(Arrays.asList(*certificateChain)))
        val msg = CMSProcessableInputStream(content)
        var signedData = gen.generate(msg, false)

        //add timestamp if TSA is available
        /*if (tsaUrl.isNotBlank()) {
            val timeStampManager = TimeStampManager(tsaUrl)
            signedData = timeStampManager.addSignedTimeStamp(signedData)
        }*/
        return signedData.encoded
    }
}