package com.zalphion.featurecontrol.storage.h2db

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract
import java.nio.file.Files

class H2StorageDriverTest: StorageDriverContract({ pageSize ->
    val file = Files.createTempFile("storage", "db")
        .also { it.toFile().deleteOnExit() }
    StorageDriver.h2Db(file, pageSize)
})