package com.zalphion.featurecontrol.storage

import java.nio.file.Files

class FilesystemStorageTest: StorageContract(
    Storage.filesystem(
        Files.createTempDirectory("foo").also { it.toFile().deleteOnExit() }
    )
)