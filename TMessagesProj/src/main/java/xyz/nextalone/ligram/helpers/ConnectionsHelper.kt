/*
 * Copyright (C) 2019-2023 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this software.
 *  If not, see
 * <https://www.gnu.org/licenses/>
 */

package xyz.nextalone.ligram.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.messenger.AccountInstance
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import java.util.concurrent.CountDownLatch

class ConnectionsHelper(instance: Int) : AccountInstance(instance) {
    companion object {
        private val Instance by lazy {
            Array(UserConfig.MAX_ACCOUNT_COUNT) {
                ConnectionsHelper(it)
            }
        }

        @JvmStatic
        fun getInstance(num: Int): ConnectionsHelper {
            return Instance[num]
        }
    }

    suspend fun <T> sendRequestAndDo(req: TLObject, flags: Int = 0, action: (TLObject?, TLRPC.TL_error?) -> T?): T? {
        lateinit var result: Pair<TLObject?, TLRPC.TL_error?>
        val latch = CountDownLatch(1)
        return withContext(Dispatchers.IO) {
            connectionsManager.sendRequest(req, { response: TLObject?, error: TLRPC.TL_error? ->
                result = Pair(response, error)
                latch.countDown()
            }, flags)
            latch.await()
            action(result.first, result.second)
        }
    }
}
