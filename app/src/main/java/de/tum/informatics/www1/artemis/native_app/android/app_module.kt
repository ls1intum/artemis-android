package de.tum.informatics.www1.artemis.native_app.android

import de.tum.informatics.www1.artemis.native_app.android.db.dbModule
import de.tum.informatics.www1.artemis.native_app.core.data.dataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.courseRegistrationModule
import de.tum.informatics.www1.artemis.native_app.feature.courseview.courseViewModule
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboardModule
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.exerciseModule
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lectureModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.communicationModule
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import de.tum.informatics.www1.artemis.native_app.feature.quiz.quizParticipationModule
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsModule
import org.koin.dsl.module

val appModule = module { includes(
    dataModule,
    uiModule,
    datastoreModule,
    deviceModule,
    websocketModule,
    courseRegistrationModule,
    courseViewModule,
    dashboardModule,
    loginModule,
    exerciseModule,
    communicationModule,
    quizParticipationModule,
    settingsModule,
    lectureModule,
    pushModule,
    dbModule
)}