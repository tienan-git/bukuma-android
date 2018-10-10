package jp.com.labit.bukuma.util

import android.content.Context
import android.support.v7.app.AlertDialog
import jp.com.labit.bukuma.R
import rx.Emitter
import rx.Observable
import rx.Single

/**
 * Created by zoonooz on 9/12/2016 AD.
 * Rx version of AlertDialog
 */
class RxAlertDialog {

  companion object {

    /**
     * Create alert dialog observable
     *
     * @param context context
     * @param title dialog title
     * @param message dialog message
     * @param button button title
     *
     * @return [Single] and will call [rx.Subscriber.onNext] only if button was clicked
     */
    fun alert(context: Context, title: String?, message: String?, button: String): Single<Unit> {
      return Observable.fromEmitter<Unit>({ e ->
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(button, { d, i ->
              e.onNext(null)
              e.onCompleted()
            })
            .show()
      }, Emitter.BackpressureMode.BUFFER).toSingle()
    }

    /**
     * Create alert dialog observable with two buttons
     *
     * @param context context
     * @param title dialog title
     * @param message dialog message
     * @param positive positive button title
     * @param negative negative button title
     *
     * @return [Single] and will call [rx.Subscriber.onNext] with true if positive was clicked,
     * false if negative was clicked.
     */
    fun alert2(
        context: Context, title: String?, message: String?,
        positive: String, negative: String): Observable<Boolean> {
      return Observable.fromEmitter<Boolean>({ e ->
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positive, { d, i ->
              e.onNext(true)
              e.onCompleted()
            })
            .setNegativeButton(negative, { d, i ->
              e.onNext(false)
              e.onCompleted()
            })
            .show()
      }, Emitter.BackpressureMode.BUFFER)
    }

    /**
     * Create alert dialog observable with N buttons
     *
     * @param context context
     * @param title dialog title
     * @param choices array of choice button title
     * @param negative negative button title
     *
     * @return [Single] and will call [rx.Subscriber.onNext] with choice index
     * if choice item was clicked, -1 if negative was clicked.
     */
    fun alertN(
        context: Context, title: String?,
        choices: Array<String>, negative: String): Single<Int> {
      return Observable.fromEmitter<Int>({ e ->
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(choices, { d, i ->
              e.onNext(i)
              e.onCompleted()
            })
            .setNegativeButton(negative, { d, i ->
              e.onNext(-1)
              e.onCompleted()
            })
            .show()
      }, Emitter.BackpressureMode.BUFFER).toSingle()
    }

    /**
     * Create alert dialog observable default
     *
     * @param context context
     * @param title dialog title
     * @param choices array of choice button title
     *
     * @return [Single] and will call [rx.Subscriber.onNext] with choice index
     *
     */
    fun alert3(
        context: Context, title: String?,
        choices: Array<String>): Single<Int> {
      return Observable.fromEmitter<Int>({ e ->
       AlertDialog.Builder(context)
           .setTitle(title)
           .setItems(choices, { d, i ->
             e.onNext(i)
             e.onCompleted()
             })
           .show()
      }, Emitter.BackpressureMode.BUFFER).toSingle()
    }
  }
}
