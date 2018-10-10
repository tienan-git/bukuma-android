package jp.com.labit.bukuma.api.request

/**
 * Created by zoonooz on 2017/01/16.
 * Delete account request body model
 */
class DeleteAccountRequest(val choice: Int, val comment: String? = null)