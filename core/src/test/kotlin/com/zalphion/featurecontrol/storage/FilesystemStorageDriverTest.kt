package com.zalphion.featurecontrol.storage

import java.nio.file.Files

class FilesystemStorageDriverTest: StorageDriverContract(
    StorageDriver.filesystem(
        Files.createTempDirectory("foo").also { it.toFile().deleteOnExit() }
    )
)