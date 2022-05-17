package com.example.pdfsigntest.certificate

import org.spongycastle.asn1.x500.X500NameBuilder
import org.spongycastle.asn1.x500.style.BCStyle
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.X509v3CertificateBuilder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.OutputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*

class CertificateService {

    /**
     * This function writes generated certificate to the destination.
     * The generated certificate will be protected by password.
     * Certificate is created based on certificateData.
     * @param certificateData
     * @param keyStoreDestination
     * @param keyStorePassword
     */
    fun generateCertificate(
        certificateData: CertificateData,
        keyStoreDestination: OutputStream,
        keyStorePassword: String,
        keyStoreType: String = "PKCS12",
    ) {
        val certificate = generateCertificate(certificateData)

        // how to create certificate pfx file: https://stackoverflow.com/questions/26311678/how-to-create-certificate-pfx-file-in-java
        // What are certificate formats and what is the difference between them: https://www.ssls.com/knowledgebase/what-are-certificate-formats-and-what-is-the-difference-between-them/
        // What are the merits of JKS vs PKCS12 for code signing: https://stackoverflow.com/questions/3867019/what-are-the-merits-of-jks-vs-pkcs12-for-code-signing
        // KeyStore: https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html#setCertificateEntry(java.lang.String,%20java.security.cert.Certificate)
        val keyStore= KeyStore.getInstance(keyStoreType)
        keyStore.load(null, keyStorePassword.toCharArray())
        keyStore.setCertificateEntry(KeyStore.getDefaultType(), certificate)

        keyStore.store(keyStoreDestination, keyStorePassword.toCharArray())
    }

    /**
     * This function returns generated certificate.
     * Certificate is created based on certificateData.
     * @param certificateData
     * @return generated certificate
     */
    fun generateCertificate(certificateData: CertificateData): X509Certificate {
        // creating self-sign certificate: https://medium.com/@bouhady/self-sign-certificate-creation-using-spongy-castle-for-android-app-61f1545dd63
        // if you want to use bouncyCastle: http://www.java2s.com/Tutorial/Java/0490__Security/CreatingaSelfSignedVersion3Certificate.htm
        // may be useful (but I'm not sure...): https://www.codegrepper.com/code-examples/shell/generate+pfx+certificate
        val certificateHolder = certificateSelfSign(certificateData)
        // how to get certificate from certificateHolder: https://stackoverflow.com/questions/6370368/bouncycastle-x509certificateholder-to-x509certificate
        val certificate: X509Certificate =
            JcaX509CertificateConverter().getCertificate(certificateHolder)

        return certificate
    }

    private fun generateRSAKeyPair(algorithm: String = "RSA", keySize: Int = 2048): KeyPair {
        val kpg = KeyPairGenerator.getInstance(algorithm);
        kpg.initialize(keySize)
        val keyPair = kpg.genKeyPair()
        return keyPair
    }

    /**
     * @param name name (for example, John Smith)
     * @param organization (for example, Axel)
     * @param organizationalUnitName (for example, Android team)
     * @param email (for example, john.smith@axel.org)
     * @param country (for example, UA)
     */
    data class CertificateData(
        val name: String,
        val organization: String,
        val organizationalUnitName: String,
        val email: String,
        val country: String
    )
    
    /**
     * @param keyPair
     * @param startDate the date when the certificate starts working
     * @param endDate the date when the certificate stops working
     * @param certificateData
     */
    private fun generateCertificateV3(
        keyPair: KeyPair,
        startDate: Date,
        endDate: Date,
        certificateData: CertificateData
    ): X509v3CertificateBuilder {
        val nameBuilder = X500NameBuilder(BCStyle.INSTANCE)
            .addRDN(BCStyle.CN, certificateData.name)
            .addRDN(BCStyle.O, certificateData.organization)
            .addRDN(BCStyle.OU, certificateData.organizationalUnitName)
            .addRDN(BCStyle.E, certificateData.email)
            .addRDN(BCStyle.C, certificateData.country)
        val x500Name = nameBuilder.build()

        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

        val v3CertGen = X509v3CertificateBuilder(
            x500Name,
            BigInteger.valueOf(SecureRandom().nextLong()),
            startDate,
            endDate,
            x500Name,
            subjectPublicKeyInfo
        )

        return v3CertGen
    }

    private fun certificateSelfSign(certificateData: CertificateData): X509CertificateHolder {
        val VALIDITY_IN_DAYS = 30
        
        val keyPair = generateRSAKeyPair()
        val certificateBuilder = generateCertificateV3(
            keyPair,
            startDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
            endDate = Date(System.currentTimeMillis() + VALIDITY_IN_DAYS * 24 * 60 * 60 * 1000),
            certificateData
        )

        Security.addProvider(BouncyCastleProvider())
        //val sigGen = JcaContentSignerBuilder("SHA256WithRSAEncryption")
        val sigGen = JcaContentSignerBuilder("SHA256WITHRSA")
            .setProvider("SC")
            .build(keyPair.private)
        val x509CertificateHolder = certificateBuilder.build(sigGen)

        return x509CertificateHolder
    }

}