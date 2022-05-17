package com.example.pdfsigntest.signature

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.AttributeTable
import org.bouncycastle.asn1.cms.Attributes
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.SignerInformation
import org.bouncycastle.cms.SignerInformationStore
import org.bouncycastle.tsp.TSPException
import java.io.IOException
import java.net.URL
import java.security.MessageDigest
import java.util.ArrayList


/**
 * Class responsible for deal with Time Stamps.
 * Time Stamp can be added when Time Stamp Authority URL is available.
 */
internal class TimeStampManager(
    private val tsaUrl: String?,
    private var tsaClient: TSAClient? = null,
) {/*
    /**
     * @param tsaUrl The url where request for Time Stamp will be done.
     * @throws NoSuchAlgorithmException
     * @throws MalformedURLException
     */
    init {
        if (tsaUrl != null) {
            val digest = MessageDigest.getInstance("SHA-256")
            tsaClient = TSAClient(URL(tsaUrl), null, null, digest)
        }
    }

    /**
     * Extend cms signed data with TimeStamp first or to all signers
     *
     * @param signedData Generated CMS signed data
     * @return CMSSignedData Extended CMS signed data
     * @throws IOException
     */
    @Throws(IOException::class, TSPException::class)
    fun addSignedTimeStamp(signedData: CMSSignedData): CMSSignedData {
        val signerStore = signedData.signerInfos
        val signersWithTimeStamp: MutableList<SignerInformation> = ArrayList()
        for (signer in signerStore.signers) {
            // This adds a timestamp to every signer (into his unsigned attributes) in the signature.
            signersWithTimeStamp.add(signTimeStamp(signer))
        }

        // new SignerInformationStore have to be created cause new SignerInformation instance
        // also SignerInformationStore have to be replaced in a signedData
        return CMSSignedData.replaceSigners(
            signedData,
            SignerInformationStore(signersWithTimeStamp)
        )
    }

    /**
     * Extend CMS Signer Information with the TimeStampToken into the unsigned Attributes.
     *
     * @param signer information about signer
     * @return information about SignerInformation
     * @throws IOException
     */
    @Throws(IOException::class, TSPException::class)
    private fun signTimeStamp(signer: SignerInformation): SignerInformation {
        val unsignedAttributes = signer.unsignedAttributes
        var vector = ASN1EncodableVector()
        if (unsignedAttributes != null) {
            vector = unsignedAttributes.toASN1EncodableVector()
        }
        val token = tsaClient!!.getTimeStampToken(signer.signature)
        val oid = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken
        val signatureTimeStamp: ASN1Encodable =
            Attribute(oid, DERSet(ASN1Primitive.fromByteArray(token)))
        vector.add(signatureTimeStamp)
        val signedAttributes = Attributes(vector)

        // replace unsignedAttributes with the signed once
        return SignerInformation.replaceUnsignedAttributes(signer, AttributeTable(signedAttributes))
    }*/
}