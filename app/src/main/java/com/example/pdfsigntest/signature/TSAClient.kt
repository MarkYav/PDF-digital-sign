package com.example.pdfsigntest.signature

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.tsp.TSPException
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponse
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * @param url      the URL of the TSA service
 * @param username user name of TSA - pass if the tsaURL need sign in
 * @param password password of TSA - pass if the tsaURL need sign in
 * @param digest   the message digest to use
 */
internal class TSAClient(
    private val url: URL,
    private val username: String,
    private val password: String,
    private val digest: MessageDigest
) {/*
    /**
     * @param messageImprint imprint of message contents
     * @return the encoded time stamp token
     * @throws IOException if there was an error with the connection or data from the TSA server,
     * or if the time stamp response could not be validated
     */
    fun getTimeStampToken(messageImprint: ByteArray?): ByteArray {
        digest.reset()
        val hash = digest.digest(messageImprint)

        // generate cryptographic nonce
        val random = SecureRandom()
        val nonce = random.nextInt()

        // generate TSA request
        val tsaGenerator = TimeStampRequestGenerator()
        tsaGenerator.setCertReq(true)
        val oid = ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.id)
        val request = tsaGenerator.generate(oid, hash, BigInteger.valueOf(nonce.toLong()))

        // get TSA response
        val tsaResponse = getTSAResponse(request.encoded)
        val response = TimeStampResponse(tsaResponse)
        response.validate(request)
        val token = response.timeStampToken
            ?: throw IOException("Response does not have a time stamp token")
        return token.encoded
    }

    @Throws(IOException::class)
    private fun getTSAResponse(request: ByteArray): ByteArray {
        val connection = url.openConnection()
        connection.doOutput = true
        connection.doInput = true
        connection.setRequestProperty("Content-Type", "application/timestamp-query")
        if (Strings.isNotBlank(username) && Strings.isNotBlank(password)) {
            connection.setRequestProperty(username, password)
        }

        // read response
        var output: OutputStream? = null
        try {
            output = connection.getOutputStream()
            output.write(request)
        } finally {
            IOUtils.closeQuietly(output)
        }
        var input: InputStream? = null
        val response: ByteArray
        try {
            input = connection.getInputStream()
            response = IOUtils.toByteArray(input)
        } finally {
            IOUtils.closeQuietly(input)
        }
        return response
    }*/
}