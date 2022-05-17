package com.example.pdfsigntest.signature

import com.tom_roush.pdfbox.io.IOUtils
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers
import org.bouncycastle.cms.CMSTypedData
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class CMSProcessableInputStream private constructor(
    private val contentType: ASN1ObjectIdentifier,
    private val inputStream: InputStream
) : CMSTypedData {

    internal constructor(inputStream: InputStream) : this(
        ASN1ObjectIdentifier(CMSObjectIdentifiers.data.id), inputStream
    )

    override fun getContent(): Any {
        return inputStream
    }

    override fun write(out: OutputStream) {
        // read the content only one time
        IOUtils.copy(inputStream, out)
        inputStream.close()
    }

    override fun getContentType(): ASN1ObjectIdentifier {
        return contentType
    }
}