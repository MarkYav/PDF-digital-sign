package com.example.pdfsigntest.signature

import android.os.FileUtils
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import java.io.*
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.*


class SigningService(
    private val keyStoreType: String = "PKCS12",
    private val keyStoreFile: File,
    private val keyStorePassword: String,
    private val certificateAlias: String,
    private val tsaUrl: String,
) {

    fun signPdf(pdfToSign: File, signedPdf: File) {
        try {
            val keyStore = getKeyStore()
            val signature = Signature(keyStore, keyStorePassword.toCharArray(), certificateAlias, tsaUrl)

            this.signDetached(signature, pdfToSign, signedPdf)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(this.javaClass.name,"Cannot obtain proper KeyStore or Certificate", e)
        } catch (e: CertificateException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: UnrecoverableKeyException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: KeyStoreException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: IOException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        }
    }

    /*fun signPdf(pdfToSign: ByteArray?): ByteArray? {
        try {
            val keyStore = getKeyStore()
            val signature = Signature(keyStore, keyStorePassword.toCharArray(), certificateAlias, tsaUrl)
            //create temporary pdf file
            //val temporaryPdfFile = byteArrayOf(pdfToSign)
            val pdfFile = File.createTempFile("pdf", "")
            //write bytes to created pdf file
            FileUtils.writeByteArrayToFile(pdfFile, pdfToSign)

            //create empty pdf file which will be signed
            val signedPdf = File.createTempFile("signedPdf", "")
            //sign pdfFile and write bytes to signedPdf
            this.signDetached(signature, pdfFile, signedPdf)
            val signedPdfBytes: ByteArray = Files.readAllBytes(signedPdf.toPath())

            //remove temporary files
            pdfFile.deleteOnExit()
            signedPdf.deleteOnExit()

            return signedPdfBytes
        } catch (e: NoSuchAlgorithmException) {
            Log.e(this.javaClass.name,"Cannot obtain proper KeyStore or Certificate", e)
        } catch (e: CertificateException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: UnrecoverableKeyException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: KeyStoreException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        } catch (e: IOException) {
            Log.e(this.javaClass.name,"Cannot obtain proper file", e)
        }

        //if pdf cannot be signed, then return plain, not signed pdf
        return pdfToSign
    }*/

    private fun getKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(keyStoreFile.inputStream(), keyStorePassword.toCharArray())
        return keyStore
    }

    private fun signDetached(signature: SignatureInterface, inFile: File, outFile: File) {
        if (!inFile.exists()) {
            throw FileNotFoundException("Document for signing does not exist")
        }
        FileOutputStream(outFile).use { fos ->
            PDDocument.load(inFile).use { doc ->
                signDetached(
                    signature,
                    doc,
                    fos
                )
                doc.close()
            }
        }
    }

    private fun signDetached(
        signature: SignatureInterface,
        document: PDDocument,
        output: OutputStream
    ) {
        val pdSignature = PDSignature()
        pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
        pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
        pdSignature.name = "Example User"
        pdSignature.location = "Kyiv, UA"
        pdSignature.reason = "Learn how to sign pdf!"

        // the signing date, needed for valid signature
        pdSignature.signDate = Calendar.getInstance()

        // register signature dictionary and sign interface
        document.addSignature(pdSignature, signature)

        // write incremental (only for signing purpose)
        // use saveIncremental to add signature, using plain save method may break up a document
        document.saveIncremental(output)
    }

}