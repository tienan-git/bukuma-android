package jp.com.labit.bukuma.model

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*

/**
 * Created by zukkey on 2017/05/15.
 * This is test class to verify expired credit card
 *
 */

@RunWith(PowerMockRunner::class)
@PrepareForTest(CreditCard::class)
class CreditCardUnitTest {
  @Test
  fun creditCardExpired_isCorrect() {
    val calender = Calendar.getInstance()
    calender.set(2021, Calendar.MARCH, 2, 0, 0, 0)
    PowerMockito.mockStatic(Calendar::class.java)
    Mockito.`when`(Calendar.getInstance()).thenReturn(calender)

    // normal example(before a year & after a month)
    val creditCard = CreditCard()
    creditCard.expYear = 2020
    creditCard.expMonth = 4

    Assert.assertEquals(true, creditCard.isExpired())

    // year equal example
    val creditCard2 = CreditCard()
    creditCard2.expYear = 2021
    creditCard2.expMonth = 1

    Assert.assertEquals(true, creditCard2.isExpired())

    // before month example
    val creditCard3 = CreditCard()
    creditCard3.expYear = 2021
    creditCard3.expMonth = 2

    Assert.assertEquals(true, creditCard3.isExpired())

    // After month example
    val creditCard4 = CreditCard()
    creditCard4.expYear = 2020
    creditCard4.expMonth = 4

    Assert.assertEquals(true, creditCard4.isExpired())
  }

  @Test
  fun creditCardExpired_isNotCorrect() {
    val calender = Calendar.getInstance()
    calender.set(2017, Calendar.MAY, 2, 0, 0, 0)
    PowerMockito.mockStatic(Calendar::class.java)
    Mockito.`when`(Calendar.getInstance()).thenReturn(calender)

    // normal example
    val creditCard = CreditCard()
    creditCard.expYear = 2020
    creditCard.expMonth = 1

    Assert.assertEquals(false, creditCard.isExpired())

    // After month example
    val creditCard2 = CreditCard()
    creditCard2.expYear = 2017
    creditCard2.expMonth = 6

    Assert.assertEquals(false, creditCard2.isExpired())

    // After year example
    val creditCard3 = CreditCard()
    creditCard3.expYear = 2018
    creditCard3.expMonth = 3

    Assert.assertEquals(false, creditCard3.isExpired())

    // Before month example
    val creditCard4 = CreditCard()
    creditCard4.expYear = 2018
    creditCard4.expMonth = 4

    Assert.assertEquals(false, creditCard4.isExpired())

    // year & month equal example
    val creditCard5 = CreditCard()
    creditCard5.expYear = 2017
    creditCard5.expMonth = 5

    Assert.assertEquals(false, creditCard5.isExpired())
  }
}
