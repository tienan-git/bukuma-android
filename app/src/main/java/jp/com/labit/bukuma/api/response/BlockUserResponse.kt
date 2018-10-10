package jp.com.labit.bukuma.api.response

import jp.com.labit.bukuma.model.BlockUser

/**
 * Created by zoonooz on 10/3/2016 AD.
 * Response for blocked user
 */

class GetBlockUsersResponse : BaseResponse() {
  var blockUsers = emptyList<BlockUser>()
}