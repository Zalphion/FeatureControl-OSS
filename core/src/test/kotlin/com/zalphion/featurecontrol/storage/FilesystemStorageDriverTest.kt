package com.zalphion.featurecontrol.storage

import java.nio.file.Files

class FilesystemStorageDriverTest: StorageDriverContract({ pageSize ->
    StorageDriver.filesystem(
        root = Files.createTempDirectory("foo").also { it.toFile().deleteOnExit() },
        pageSize = pageSize
    )
})