package jp.com.labit.bukuma.custom

import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * Created by tani on 2017/06/20.
 */

/**
 * [AsyncQueryHandler] だと Worker内での Exception が catch できないので、
 * それを可能にするために作成.
 */
abstract class SafeAsyncQueryHandler(cr: ContentResolver): AsyncQueryHandler(cr) {

  protected inner class SafeWorkerHandler(looper: Looper): WorkerHandler(looper) {
    override fun handleMessage(msg: Message) {
      try {
        super.handleMessage(msg)
      } catch (ex: RuntimeException) {
        val args = msg.obj as? WorkerArgs
        val reply = args?.handler?.obtainMessage(msg.what)
        args?.result = ex
        reply?.obj = args
        reply?.arg1 = msg.arg1
        reply?.sendToTarget()
      }
    }
  }

  override fun createHandler(looper: Looper): Handler {
    return SafeWorkerHandler(looper)
  }

  override fun handleMessage(msg: Message) {
    (msg.obj as? WorkerArgs)?.let { args ->
      (args.result as? RuntimeException)?.let {
        onError(msg.what, args.cookie, it)
        return
      }
    }
    super.handleMessage(msg)
  }

  /**
   * Exception に対処したい場合は、ここを override して行う.
   */
  open fun onError(token:Int, cookie: Any?, error: RuntimeException) {
    throw error
  }
}