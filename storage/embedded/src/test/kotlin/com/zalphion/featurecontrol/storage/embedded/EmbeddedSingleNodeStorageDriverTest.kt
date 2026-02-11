package com.zalphion.featurecontrol.storage.embedded

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract
import java.nio.file.Files

class EmbeddedSingleNodeStorageDriverTest: StorageDriverContract({ pageSize ->
    val file = Files.createTempFile("storage", "db")
        .also { it.toFile().deleteOnExit() }
    StorageDriver.embeddedSingleNode(file, pageSize)
})