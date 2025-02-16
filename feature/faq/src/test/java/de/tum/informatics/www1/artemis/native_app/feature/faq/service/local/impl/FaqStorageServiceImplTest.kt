package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.impl

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.faq_test.FaqDatabaseProviderMock
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class FaqStorageServiceImplTest {

    private val databaseProviderMock = FaqDatabaseProviderMock(InstrumentationRegistry.getInstrumentation().context)
    internal val sut = FaqStorageServiceImpl(databaseProviderMock, databaseProviderMock)

    // TODO: tests
    /*
    - store and get single faq
      - faq is correct
      - categories are correct
    - store and getById
    - store to update faq

     */
}