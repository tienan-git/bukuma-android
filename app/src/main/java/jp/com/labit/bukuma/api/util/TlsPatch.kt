package jp.com.labit.bukuma.api.util

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.util.Arrays

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * TLS code from Omise for fixing cannot connect to them in lower android version
 */
object TlsPatch {

  @Throws(GeneralSecurityException::class)
  fun systemDefaultTrustManager(): X509TrustManager {
    val trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm())
    @Suppress("CAST_NEVER_SUCCEEDS")
    trustManagerFactory.init(null as? KeyStore)
    val trustManagers = trustManagerFactory.trustManagers
    if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
      throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
    }
    return trustManagers[0] as X509TrustManager
  }

  /**
   * SSLSocketFactory implementation that enforces TLSv1.1 or TLSv1.2 usage.

   * credit: https://gist.github.com/fkrauthan/ac8624466a4dee4fd02f
   */
  class TLSSocketFactory @Throws(KeyManagementException::class, NoSuchAlgorithmException::class)
  constructor() : SSLSocketFactory() {
    private val internalSSLSocketFactory: SSLSocketFactory

    init {
      val context = SSLContext.getInstance("TLS")
      context.init(null, null, null)
      internalSSLSocketFactory = context.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> {
      return internalSSLSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
      return internalSSLSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
      return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket): Socket {
      if (socket is SSLSocket) {
        socket.enabledProtocols = arrayOf("TLSv1.1", "TLSv1.2")
      }
      return socket
    }
  }
}